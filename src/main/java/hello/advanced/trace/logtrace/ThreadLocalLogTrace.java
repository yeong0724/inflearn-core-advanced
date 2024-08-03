package hello.advanced.trace.logtrace;

import hello.advanced.trace.PreFix;
import hello.advanced.trace.TraceId;
import hello.advanced.trace.TraceStatus;
import lombok.extern.slf4j.Slf4j;

import static hello.advanced.trace.PreFix.*;

@Slf4j
public class ThreadLocalLogTrace implements LogTrace {

    /**
     * >> 동시성 문제가 해결되어 Log 상으로 50c2fb77, c539b3e2 로그레벨 문제가 해결됨을 확인 할 수 있다.
     *     [50c2fb77] OrderController.request()
     *     [50c2fb77] |--> OrderService.orderItem()
     *     [50c2fb77] |   |--> OrderRepository.save()
     *     [50c2fb77] |   |<-- OrderRepository.save() time = 1005 ms
     *     [50c2fb77] |<-- OrderService.orderItem() time = 1006 ms
     *     [50c2fb77] OrderController.request() time = 1007 ms
     *
     *     [c539b3e2] OrderController.request()
     *     [c539b3e2] |--> OrderService.orderItem()
     *     [c539b3e2] |   |--> OrderRepository.save()
     *     [c539b3e2] |   |<-- OrderRepository.save() time = 1005 ms
     *     [c539b3e2] |<-- OrderService.orderItem() time = 1007 ms
     *     [c539b3e2] OrderController.request() time = 1007 ms
     */

    private final ThreadLocal<TraceId> traceIdHolder = new ThreadLocal<>();

    @Override
    public TraceStatus begin(String message) {
        syncTraceId();
        TraceId traceId = traceIdHolder.get();
        Long startTimeMs = System.currentTimeMillis();
        log.info("[{}] {}{}",
                traceId.getId(),
                addSpace(START_PREFIX, traceId.getLevel()),
                message
        );

        return new TraceStatus(traceId, startTimeMs, message);
    }

    @Override
    public void end(TraceStatus status) {
        complete(status, null);
    }

    @Override
    public void exception(TraceStatus status, Exception exception) {
        complete(status, exception);
    }

    private void complete(TraceStatus status, Exception exception) {
        Long stopTimeMs = System.currentTimeMillis();
        long resultTimeMs = stopTimeMs - status.getStartTimeMs();
        TraceId traceId = status.getTraceId();
        if (exception == null) {
            log.info("[{}] {}{} time = {} ms",
                    traceId.getId(),
                    addSpace(COMPLETE_PREFIX, traceId.getLevel()),
                    status.getMessage(),
                    resultTimeMs
            );
        } else {
            log.info("[{}] {}{} time = {} ms | exception = {}",
                    traceId.getId(),
                    addSpace(EX_PREFIX, traceId.getLevel()),
                    status.getMessage(),
                    resultTimeMs,
                    exception.toString()
            );
        }
        releaseTraceId();
    }

    private void syncTraceId() {
        TraceId traceId = traceIdHolder.get();
        if (traceId == null) {
            traceIdHolder.set(new TraceId());
        } else {
            traceIdHolder.set(traceId.createNextId());
        }
    }

    /**
     * 추가로 쓰레드 로컬을 모두 사용하고 나면 꼭 ThreadLocal.remove()를 호출해해서 쓰레드 로컬에 저장된 값을 제거해주어야 한다.
     * WAS 는 ThreadPool 에서 Thread 를 하나 조회한다.
     * WAS 는 Thread 생성 비용이 비싸기 때문에 사용이 끝난 Thread 를 따로 제거하지 않고 보통 ThreadPool 에 반환하여 이후에 재사용한다.
     * 그렇기 때문에 사용이 끝난 Thread 의 로컬값을 ThreadLocal.remove()를 통해 꼭 제거해줘야 한다.
     */
    private void releaseTraceId() {
        TraceId traceId = traceIdHolder.get();
        if (traceId.isFirstLevel()) {
            traceIdHolder.remove(); // destroy
        } else {
            traceIdHolder.set(traceId.createPreviousId());
        }
    }

    private static String addSpace(PreFix prefix, int level) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < level; i++) {
            stringBuilder.append( (i == level - 1) ? "|" + prefix : "|   ");
        }

        return stringBuilder.toString();
    }
}
