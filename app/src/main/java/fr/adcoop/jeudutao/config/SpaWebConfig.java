package fr.adcoop.jeudutao.config;

import org.jspecify.annotations.NonNull;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

import java.io.IOException;

@Configuration
public class SpaWebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/")
                .resourceChain(true)
                .addResolver(
                        new PathResourceResolver() {
                            @Override
                            protected Resource getResource(
                                    @NonNull String resourcePath,
                                    @NonNull Resource location
                            ) throws IOException {
                                var resource = super.getResource(resourcePath, location);
                                if (resource != null && resource.exists()) {
                                    return resource;
                                }
                                return location.createRelative("index.html");
                            }
                        });
    }
}
