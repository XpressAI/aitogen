package ai.xpress.aitogen.test;

import ai.xpress.aitogen.*;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

class ContactInfo {
    public String name;
    public String email;
    public String phone;
}

@AIService(provider = "openai", model = "text-davinci-003")
@ContextPrompt("""
You are a bot helps a program to understand text.  You should only
respond with valid JSON identifiers such as: $alternatives.  You should
respect capitalization and not respond with any other text.
""")
interface ClassifierService extends AIServiceClient {
    @Prompt("Given the text: \"$text\"")
    @Prompt("Is the text a question?")
    @Define(name="alternatives", values={"true", "false"})
    boolean isQuestion(String text);

    @Prompt("Given the text: \"$text\"")
    @Prompt("Is the text appropriate for children? ")
    @Define(name="alternatives", values={"true", "false"})
    boolean isAppropriateForChildren(String text);

    @Prompt("Given the text: $text")
    @Prompt("Does the text contain any contact info?")
    @Define(name="alternatives", values={"true", "false"})
    boolean containsContactInfo(String text);

    @Prompt("Given the text: \"$text\"")
    @Prompt("Extract the contact info. Leaving null for missing values.")
    @Define(name="alternatives", values={"""
            valid JSON of the format:
            {
                "name": "John Doe",
                "email": "test@example.com",
                "phone": "+1 555 555 5555"
            }
    """})
    ContactInfo extractContactInfo(String text);
}

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {AppContext.class})
public class InjectTests {


    @Autowired
    private ClassifierService classifierService;

    @Test
    public void test() {
        //var classifierService = new AIServiceFactoryBean<ClassifierService>(ClassifierService.class).getObject();
        Assert.assertNotNull(classifierService);

        var res1 = classifierService.isQuestion("Is the sky blue?");
        Assert.assertTrue(res1);
        var res2 = classifierService.isAppropriateForChildren("You are an asshole!");
        Assert.assertFalse(res2);
        var res3 = classifierService.extractContactInfo("My name is John Doe, my email is test@example.com. Contact me at 555-555-5555");
        Assert.assertEquals(res3.name.toLowerCase(), "john doe");
        Assert.assertEquals(res3.email.toLowerCase(), "test@example.com");
    }
}
