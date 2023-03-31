package ai.xpress.aitogen.factory;

import ai.xpress.aitogen.AIService;
import org.apache.commons.collections.IterableMap;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.classreading.MetadataReader;

import java.io.IOException;

public class AIServicePostProcessor implements BeanFactoryPostProcessor {
    private final String basePackage;

    public AIServicePostProcessor(String basePackage) {
        this.basePackage = basePackage;
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        var scanner = new ClassPathScanningCandidateComponentProvider(false) {
            @Override
            protected boolean isCandidateComponent(MetadataReader metadataReader) throws IOException {
                var hit = metadataReader.getAnnotationMetadata().hasAnnotation(AIService.class.getName());
                if (hit) {
                    try {
                        var interfaceType = Class.forName(metadataReader.getClassMetadata().getClassName());
                        var annotation = metadataReader.getAnnotationMetadata().getAnnotations().get(AIService.class.getName());
                        var provider = annotation.getString("provider");
                        var model = annotation.getString("model");
                        double temperature = annotation.getDouble("temperature");
                        int maxTokens = annotation.getInt("maxTokens");

                        GenericBeanDefinition beanDefinition = new GenericBeanDefinition();
                        beanDefinition.setBeanClass(AIServiceFactoryBean.class);
                        beanDefinition.getPropertyValues().add("objectType", interfaceType);
                        beanDefinition.getPropertyValues().add("provider", provider);
                        beanDefinition.getPropertyValues().add("model", model);
                        beanDefinition.getPropertyValues().add("temperature", temperature);
                        beanDefinition.getPropertyValues().add("maxTokens", maxTokens);

                        beanDefinition.setAutowireCandidate(true);

                        ((BeanDefinitionRegistry) beanFactory).registerBeanDefinition(interfaceType.getSimpleName(), beanDefinition);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                return false;
            }
        };
        // TODO: Find out why this returns the empty set even if we return true above.
        scanner.findCandidateComponents(basePackage);
    }
}
