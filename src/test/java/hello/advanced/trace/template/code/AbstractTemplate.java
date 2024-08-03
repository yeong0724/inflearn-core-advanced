package hello.advanced.trace.template.code;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractTemplate {
    public void execute() {
        // 비즈니스 로직 실행
        long startTime = System.currentTimeMillis();

        call(); // 상속

        // 비즈니스 로직 종료
        long endTime = System.currentTimeMillis();

        // 소요 시간 측정
        long resultTime = endTime - startTime;
        log.info("resultTime={}", resultTime);
    }

    protected abstract void call();
}
