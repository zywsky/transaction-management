package com.banking.config;

import com.banking.interceptor.LogInterceptor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {


    @Autowired
    //To print base request and response info and to record the time consumed
    private LogInterceptor logInterceptor;

    //Note: since no need to verify user,  so I did not add the UserInterceptor here.
    //If we need to auth user, we can add and do the related auth logic there. Or use spring security is another choice.

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(logInterceptor)
                .addPathPatterns("/transaction/**");
    }
} 