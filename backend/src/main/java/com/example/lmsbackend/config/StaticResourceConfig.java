package com.example.lmsbackend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import java.nio.file.Paths;

@Configuration
public class StaticResourceConfig implements WebMvcConfigurer {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Configure both relative and absolute paths for uploads
        String absolutePath = Paths.get("./uploads").toAbsolutePath().normalize().toString();
        
        registry.addResourceHandler("/uploads/**")
            .addResourceLocations("file:./uploads/", "file:" + absolutePath + "/")
            .setCachePeriod(0);
    }
}