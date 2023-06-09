package ai.xpress.aitogen.factory;

import ai.xpress.aitogen.*;

import java.io.StringWriter;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.MapperFeature;
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

    private final OpenAiService backendService;
    private final String provider;
    private final String model;
    private final double temperature;
    private final int maxTokens;

    public AIServiceInvocationHandler(String provider, String model, double temperature, int maxTokens) {
        this.provider = provider;
        this.model = model;
        this.temperature = temperature;
        this.maxTokens = maxTokens;

        if ("openai".equals(provider)) {
            // TODO: Make this configurable from multiple sources.
            this.backendService = new OpenAiService(System.getenv("OPENAI_API_KEY"));
        } else {
            throw new IllegalArgumentException("Unknown provider: " + provider);
        }
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // Assuming openai for now.
        var aiServiceAnnotation = method.getDeclaringClass().getAnnotation(AIService.class);
        var model = "text-davinci-003";


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
        if (defineAnnotations != null) {
            var defines = Stream.of(defineAnnotations)
                    .collect(Collectors.toMap(Define::name, d -> String.join(",", d.values())));
            contextData.putAll(defines);
        }

        var result = processTemplateFromString(fullPrompt, contextData);

        var completionRequest = CompletionRequest.builder()
                .prompt(result)
                .model(model)
                .temperature(temperature)
                .maxTokens(maxTokens)
                .echo(false)
                .build();

        // Call the OpenAiService with the request
        var completionResponse = backendService.createCompletion(completionRequest);

        // parse response as valid json:
        var response = completionResponse.getChoices().get(0).getText().trim().toLowerCase();

        // TODO: Debug log these.
        //System.out.println("Prompt:" + result);
        //System.out.println("Response: " + response);

        var mapper = new ObjectMapper().configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);

        try {
            return mapper.readValue(response, method.getReturnType());
        } catch (JsonParseException e) {
            // If the response has some extra text around it, try to parse the json inside it.
            var firstCurly = response.indexOf('{');
            var lastCurly = response.lastIndexOf('}');
            if (firstCurly != -1 && lastCurly != -1) {
                return mapper.readValue(response.substring(firstCurly, lastCurly + 1), method.getReturnType());
            } else {
                // Hope the user expects a string.
                return response;
            }
        }
    }

    private String processTemplateFromString(String fullPrompt, HashMap<String, Object> contextData) {
        // TODO: Cache the engine and prompt repository.
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