package ai.xpress.aitogen.test;

import ai.xpress.aitogen.factory.AIServicePostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@ComponentScan(basePackages = "ai.xpress.aitogen.test")
@Configuration
public class AppContext {

    @Bean
    public static AIServicePostProcessor aiservicePostProcessor() {
        return new AIServicePostProcessor("ai.xpress.aitogen.test");
    }
}
