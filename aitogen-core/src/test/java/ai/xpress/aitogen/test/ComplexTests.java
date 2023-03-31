package ai.xpress.aitogen.test;

import ai.xpress.aitogen.AIService;
import ai.xpress.aitogen.ContextPrompt;
import ai.xpress.aitogen.Prompt;
import ai.xpress.aitogen.factory.AIServiceFactoryBean;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

class Result {
    ContactInformation value;

    public ContactInformation getValue() {
        return value;
    }
}

class ConfidenceValue<T> {
    float confidence;
    T value;

    public float getConfidence() {
        return confidence;
    }

    public T getValue() {
        return value;
    }
}

class Location {
    ConfidenceValue<String> city;
    ConfidenceValue<String> country;

    public ConfidenceValue<String> getCity() {
        return city;
    }

    public ConfidenceValue<String> getCountry() {
        return country;
    }
}

class ContactInformation {
    ConfidenceValue<String> name;
    ConfidenceValue<String> phoneNumber;
    ConfidenceValue<String> email;
    Location location;


    public ConfidenceValue<String> getName() {
        return name;
    }

    public ConfidenceValue<String> getPhoneNumber() {
        return phoneNumber;
    }


    public ConfidenceValue<String> getEmail() {
        return email;
    }

    public Location getLocation() {
        return location;
    }
}

@AIService(provider = "openai", model = "text-davinci-003")
@ContextPrompt("""
       $text
       ---
       Extract the contact information from the above text. Information that is not available should be left at `null`.
       
       Give your output as a JSON object literal satisfying this jackson-compatible java bean interface definition:
       ```
       interface Result {
        ContactInformation getValue();
       }
       
       interface ConfidenceValue<T>{
          float getConfidence();
          T getValue();
       }
       
       interface Location {
         ConfidenceValue<String> getCity();
         ConfidenceValue<String> getCountry();
       }
       
       interface ContactInformation {
         ConfidenceValue<String> getName();
         ConfidenceValue<String> getPhoneNumber();
         ConfidenceValue<String> getEmail();
         Location getLocation();
       }
       ```
       
       You must start with { "value":, respect proper camel case and not respond with any other text.
        """)
interface Extractor {
    @Prompt("")
    Result extractContactInfo(String text);
}

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {AppContext.class})
public class ComplexTests {

    @Autowired
    Extractor extractor;

    @Test
    public void test() {
        Assert.assertNotNull(extractor);

        var input = "I'm John Doe. My email address is john@example.com. My height is 180cm. You can reach me at 090-5555-6666. I live in Japan.";

        var res = extractor.extractContactInfo(input);
        Assert.assertNotNull(res);

        Assert.assertEquals(res.getValue().getName().getValue().toLowerCase(), "john doe");
        Assert.assertTrue(res.getValue().getLocation().getCity().getConfidence() < 1.0);
    }
}
