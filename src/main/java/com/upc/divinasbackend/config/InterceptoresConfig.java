package com.upc.divinasbackend.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class InterceptoresConfig implements WebMvcConfigurer {

    @Autowired
    private OperacionesInterceptor operaciones;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(operaciones).addPathPatterns("/api/**");
    }
}
