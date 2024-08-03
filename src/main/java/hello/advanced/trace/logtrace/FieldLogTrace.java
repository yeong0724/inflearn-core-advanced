package hello.advanced.trace.logtrace;

import hello.advanced.trace.PreFix;
import hello.advanced.trace.TraceId;
import hello.advanced.trace.TraceStatus;
import lombok.extern.slf4j.Slf4j;

import static hello.advanced.trace.PreFix.*;

@Slf4j
public class FieldLogTrace implements LogTrace {
    /**
     * [동시성 문제]
     * @FieldLogTrace 는 싱글톤으로 등로고딘 스프링 빈이다.
     * 쉽게 얘기해서 이 객체 인스턴스가 JVM(애플리케이션)에 딴 1개 존재하는 뜻이다.
     * 이렇게 하나만 있는 인스턴스 FieldLogTrace 의 TraceId 필드를 여러 쓰레드가 동시에 접근하기
     * 때문에 동시성 문제가 발생하게 된다.
     */

    private TraceId traceIdHolder; // traceId 동기화, 동시성 이슈 발생

    @Override
    public TraceStatus begin(String message) {
        syncTraceId();
        TraceId traceId = traceIdHolder;
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
            log.info("[{}] {}{} time = {} ms ex = {}",
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
        if (traceIdHolder == null) {
            traceIdHolder = new TraceId();
        } else {
            traceIdHolder = traceIdHolder.createNextId();
        }
    }

    private void releaseTraceId() {
        if (traceIdHolder.isFirstLevel()) {
            traceIdHolder = null; // destroy
        } else {
            traceIdHolder = traceIdHolder.createPreviousId();
        }
    }

    private static String addSpace(PreFix prefix, int level) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < level; i++) {
            stringBuilder.append((i == level - 1) ? "|" + prefix : "|   ");
        }
        return stringBuilder.toString();
    }
}
