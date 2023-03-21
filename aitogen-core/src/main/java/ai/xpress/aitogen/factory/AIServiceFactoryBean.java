package ai.xpress.aitogen.factory;

import org.springframework.beans.factory.FactoryBean;

import java.lang.reflect.Proxy;

public class AIServiceFactoryBean<T> implements FactoryBean<T> {

    private final Class<T> aiServiceInterface;

    public AIServiceFactoryBean(Class<T> aiServiceInterface) {
        this.aiServiceInterface = aiServiceInterface;
    }

    @Override
    public T getObject() {
        // Use reflection, proxying, and code generation to create an instance of the AIService implementation
        return (T) Proxy.newProxyInstance(
                aiServiceInterface.getClassLoader(),
                new Class<?>[]{aiServiceInterface},
                new AIServiceInvocationHandler()
        );
    }

    @Override
    public Class<?> getObjectType() {
        return aiServiceInterface;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }
}