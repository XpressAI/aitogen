package ai.xpress.aitogen.factory;

import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.springframework.context.annotation.Configuration;

import java.lang.reflect.Proxy;

@Configuration
public class AIServiceFactoryBean extends AbstractFactoryBean<Object> {

    private Class<?> objectType;
    private String provider;
    private String model;
    private double temperature = 0.7;
    private int maxTokens = 256;

    @Override
    public Class<?> getObjectType() {
        return objectType;
    }

    public void setObjectType(Class<?> objectType) {
        this.objectType = objectType;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public int getMaxTokens() {
        return maxTokens;
    }

    public void setMaxTokens(int maxTokens) {
        this.maxTokens = maxTokens;
    }

    @Override
    protected Object createInstance() throws Exception {
        return Proxy.newProxyInstance(
                AIServiceFactoryBean.class.getClassLoader(),
                new Class<?>[]{objectType},
                new AIServiceInvocationHandler(provider, model, temperature, maxTokens)
        );
    }
}
