package hello.advanced;

import hello.advanced.trace.logtrace.LogTrace;
import hello.advanced.trace.logtrace.ThreadLocalLogTrace;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LogTraceConfig {
    @Bean
    public LogTrace logTrace() {
        /**
         * 동시성 문제가 있는 FieldLogTrace 대신에 문제를 해결한 ThreadLocalLogTrace 를 스프링 빈으로 등록
         */
        // return new FieldLogTrace();
        return new ThreadLocalLogTrace();
    }
}
