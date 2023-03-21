package ai.xpress.aitogen;


import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Prompts {
    Prompt[] value();
}
