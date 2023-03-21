package ai.xpress.aitogen;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Repeatable(Prompts.class)
@Target(ElementType.METHOD)
public @interface Prompt {
    String value();
}