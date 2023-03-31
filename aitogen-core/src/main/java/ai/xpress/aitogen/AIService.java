package ai.xpress.aitogen;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface AIService {
    String provider();

    String model();

    double temperature() default 0.7;

    int maxTokens() default 256;
}
