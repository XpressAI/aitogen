package ai.xpress.aitogen.factory;

import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.springframework.context.annotation.Configuration;

import java.lang.reflect.Proxy;

@Configuration
public class AIServiceFactoryBean extends AbstractFactoryBean<Object> {

    private Class<?> objectType;

    public void setObjectType(Class<?> objectType) {
        this.objectType = objectType;
    }

    @Override
    public Class<?> getObjectType() {
        return objectType;
    }

    @Override
    protected Object createInstance() throws Exception {
        try {
            return Proxy.newProxyInstance(
                    AIServiceFactoryBean.class.getClassLoader(),
                    new Class<?>[]{objectType},
                    new AIServiceInvocationHandler()
            );
        } catch (Exception e) {
            throw e;
        }
    }
}
