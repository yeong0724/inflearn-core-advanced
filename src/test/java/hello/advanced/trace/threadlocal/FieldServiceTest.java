package hello.advanced.trace.threadlocal;

import hello.advanced.trace.threadlocal.code.FieldService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
public class FieldServiceTest {
    private final FieldService fieldService = new FieldService();

    @Test
    void field() {
        log.info("main start");

        Runnable userA = () -> fieldService.logic("userA");
        Runnable userB = () -> fieldService.logic("userB");

        Thread threadA = new Thread(userA);
        threadA.setName("thread-A");

        Thread threadB = new Thread(userB);
        threadB.setName("thread-B");

        /**
         * [Java 동시성 문제 - ThreadLocal 의 필요성]
         * A 를 실행하고 2초쉬고 B를 동작시키면 겹치지 않아 동시성 문제가 발생하지 않지만 0.1초 쉬고 B를 동작시키면 동시성 문제가 발생한다.
         * 결과적으로 Thread-A의 nameStore 는 동시성 문제로 userA가 아니라 userB가 출력된다.
         *
         * 해당 동시성 문제는 같은 인스턴스의 필드(ex. Singleton Pattern) or static 같은 공용 필드에 접근할 때 발생한다.
         * 지역변수(멤버변수)는 Thread 마다 각각 다른 메모리 영역에 할당되기 때문이다.
         * 값을 읽기만 한다면 동시성문제는 발생하지 않고, 어디선가 값을 변경하기에 발생한다.
         * 즉, 스프링빈 처럼 싱글톤 객체의 필드를 변경하며 사용할 때는 이러한 동시성 문제를 매우 조심해야 한다.
         */
        threadA.start(); // A실행
        // sleep(2000); // 동시성 문제 발생X
        sleep(100); // 동시성 문제 발생O

        threadB.start(); // B실행
        sleep(3000); // 메인 쓰레드 종료 대기

        log.info("main exit");
    }

    private void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
