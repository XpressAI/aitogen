package ai.xpress.aitogen.factory;

import ai.xpress.aitogen.*;

import java.io.StringWriter;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.theokanning.openai.service.OpenAiService;
import com.theokanning.openai.completion.CompletionRequest;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.resource.loader.StringResourceLoader;
import org.apache.velocity.runtime.resource.util.StringResourceRepository;

public class AIServiceInvocationHandler implements InvocationHandler {

    private final OpenAiService openAiService;

    public AIServiceInvocationHandler() {
        // TODO: Make this configurable from multiple sources.
        this.openAiService = new OpenAiService(System.getenv("OPENAI_API_KEY"));
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        AIService aiServiceAnnotation = method.getDeclaringClass().getAnnotation(AIService.class);
        String model = aiServiceAnnotation.model();

        var contextPrompt = method.getDeclaringClass().getAnnotation(ContextPrompt.class);
        var context = (contextPrompt != null) ? contextPrompt.value().trim() : "";

        var promptAnnotation = method.getAnnotation(Prompt.class);
        Stream<Prompt> promptsStream;
        if (promptAnnotation == null) {
            var promptsAnnotation = method.getAnnotation(Prompts.class);
            promptsStream = Stream.of(promptsAnnotation.value());
        } else {
            promptsStream = Stream.of(promptAnnotation);
        }
        var promptStrings = promptsStream.map(Prompt::value).map(String::trim).toArray(String[]::new);
        var mainPrompt = String.join(" ", promptStrings);
        var fullPrompt = context + "\n" + mainPrompt;

        var contextData = new HashMap<String, Object>();
        for (var i = 0; i < method.getParameters().length; i++) {
            contextData.put(method.getParameters()[i].getName(), args[i]);
        }
        var defineAnnotations = method.getAnnotation(Define.class);
        var defines = Stream.of(defineAnnotations)
                .collect(Collectors.toMap(Define::name, d -> String.join(",", d.values())));
        contextData.putAll(defines);

        var result = processTemplateFromString(fullPrompt, contextData);

        var completionRequest = CompletionRequest.builder()
                .prompt(result)
                .model(model)
                .temperature(0.7)
                .maxTokens(256)
                .echo(false)
                .build();

        // Call the OpenAiService with the request
        var completionResponse = openAiService.createCompletion(completionRequest);

        // parse response as valid json:
        var response = completionResponse.getChoices().get(0).getText().trim().toLowerCase();
        System.out.println("Prompt:" + result);
        System.out.println("Response: " + response);
        var mapper = new ObjectMapper();
        return mapper.readValue(response, method.getReturnType());
    }

    private String processTemplateFromString(String fullPrompt, HashMap<String, Object> contextData) {
        VelocityEngine engine = new VelocityEngine();
        engine.setProperty(Velocity.RESOURCE_LOADER, "string");
        engine.addProperty("string.resource.loader.class", StringResourceLoader.class.getName());
        engine.addProperty("string.resource.loader.repository.static", "false");
        //  engine.addProperty("string.resource.loader.modificationCheckInterval", "1");
        engine.init();

        // Initialize my template repository. You can replace the "Hello $w" with your String.
        StringResourceRepository repo = (StringResourceRepository) engine.getApplicationAttribute(StringResourceLoader.REPOSITORY_NAME_DEFAULT);
        repo.putStringResource("template", fullPrompt);

        // Set parameters for my template.
        VelocityContext context = new VelocityContext();
        contextData.forEach(context::put);

        // Get and merge the template with my parameters.
        Template template = engine.getTemplate("template");
        StringWriter writer = new StringWriter();
        template.merge(context, writer);
        return writer.toString();
    }
}