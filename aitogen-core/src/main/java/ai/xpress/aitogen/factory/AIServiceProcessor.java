package ai.xpress.aitogen.factory;

import ai.xpress.aitogen.AIService;
import ai.xpress.aitogen.ContextPrompt;
import ai.xpress.aitogen.Define;
import ai.xpress.aitogen.Prompt;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@SupportedAnnotationTypes({"AIService", "ContextPrompt", "Prompt", "Responses"})
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class AIServiceProcessor extends AbstractProcessor {
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        // Handle annotations and generate client implementation using templates
        // ...
        return true;
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return new HashSet<>(Arrays.asList(
                AIService.class.getCanonicalName(),
                ContextPrompt.class.getCanonicalName(),
                Prompt.class.getCanonicalName(),
                Define.class.getCanonicalName()));
    }
}