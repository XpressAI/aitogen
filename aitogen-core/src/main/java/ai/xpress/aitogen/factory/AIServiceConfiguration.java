package ai.xpress.aitogen.factory;

import ai.xpress.aitogen.AIService;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.stereotype.Component;

@Component
public class AIServiceConfiguration implements BeanDefinitionRegistryPostProcessor {

    public static final String BASE_PACKAGE = "ai.xpress.aitogen";

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry beanDefinitionRegistry) {
        var scanner = new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(AIService.class));

        for (var bd : scanner.findCandidateComponents(BASE_PACKAGE)) {
            var beanDefinition = new GenericBeanDefinition();
            beanDefinition.setBeanClass(AIServiceFactoryBean.class);
            beanDefinition.getConstructorArgumentValues().addGenericArgumentValue(bd.getBeanClassName());
            beanDefinition.setAutowireCandidate(true);
            beanDefinitionRegistry.registerBeanDefinition(bd.getBeanClassName(), beanDefinition);
        }
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {

    }
}