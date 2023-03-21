# AItoGEN

Inject AI into your Java code with annotations. By leveraging large language 
models to help do hard-to-program things like classificaiton, summarization, 
translation, and extraction.

Note:  Still very early and not everything shown below works yet.  Stay tuned.  Join [Discord](https://discord.com/invite/vgEg2ZtxCw) if it looks interesting.

## Example


```java
import ai.xpress.aitogen.AIService;
import ai.xpress.aitogen.ContextPrompt;
import ai.xpress.aitogen.Prompt;
import ai.xpress.aitogen.Define;

@AIService(provider="openai", model="text-davinci-003")
@ContextPrompt(
    """You are a bot helps a program to understand text.  You should only 
    respond with JSON with the format $alternatives.  You should not respond 
    with any other text.
""")
public interface ClassifierService {
    @Prompt("Is the following text a question? $text")
    @Define(name="alternatives", values={"true", "false"})
    boolean isQuestion(String text);
    
    @Prompt("Is this comment overly mean or abusive? $text")
    @Define(name="alternatives", values={"true", "false"})
    boolean isAbusive(String text);
    
    @Prompt("Given the text: $text")
    @Prompt("Does the text contain any contact info?")
    @Define(name="alternatives", values={"true", "false"})
    boolean containsContactInfo(String text);
    
    @Prompt("Given the text: \"$text\"")
    @Prompt("Extract the contact info. Leaving null for missing values.")
    @Define(name="alternatives", values={"""
            {
                "name": "John Doe",
                "email": "test@example.com",
                "phone": "+1 555 555 5555",
            }
    """})
    ContactInfo extractContactInfo(String text);
}

```

Then in your code you can inject the class with standard dependency injection.

```java
public class SomeController {
    @Autowired // Or @Inject
    private ClassifierService classifierservice;
    
    public void doSomething() {
        boolean isQuestion = classifierservice.isQuestion("Is this a question?");
        boolean isAbusive = classifierservice.isAbusive("You are a jerk!");
        boolean containsContactInfo = classifierservice.containsContactInfo("Call me at 555-555-5555");
        ContactInfo contactInfo = classifierservice.extractContactInfo("Call me at 555-555-5555");
    }
}
```

## Installation

### Maven

```xml

<dependencies>
    <dependency>
        <groupId>ai.xpress.aitogen</groupId>
        <artifactId>aitogen-core</artifactId>
        <version>0.0.1</version>
    </dependency>
    
    <dependency>
        <groupId>ai.xpress.aitogen</groupId>
        <artifactId>aitogen-openai</artifactId>
        <version>0.0.1</version>
    </dependency>
</dependencies>

```

### Gradle

```groovy
implementation 'ai.xpress.aitogen:aitogen-core:0.0.1'
implementation 'ai.xpress.aitogen:aitogen-openai:0.0.1'
```

## Configuration

When using AItoGEN with OpenAI you can set the following environment variables.

```bash
export OPENAI_TOKEN="sk-XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX"
export OPENAI_ORG="XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX"
```

## License

AItoGEN is published under the Apache 2.0 License.







