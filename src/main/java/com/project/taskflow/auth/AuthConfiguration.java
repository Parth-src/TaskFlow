package com.project.taskflow.auth;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AuthConfiguration {

    @Bean
    public FilterRegistrationBean<ApiKeyAuthenticationFilter>
    apiKeyAuthenticationFilterRegistration(
            ApiKeyAuthenticationFilter filter) {

        FilterRegistrationBean<ApiKeyAuthenticationFilter>
                registration =
                new FilterRegistrationBean<>();

        registration.setFilter(filter);

        registration.addUrlPatterns(
                "/api/sdk/*"
        );

        registration.setOrder(1);

        return registration;
    }

    @Bean
    public FilterRegistrationBean<SessionAuthenticationFilter>
    sessionAuthenticationFilterRegistration(
            SessionAuthenticationFilter filter) {

        FilterRegistrationBean<SessionAuthenticationFilter>
                registration =
                new FilterRegistrationBean<>();

        registration.setFilter(filter);

        registration.addUrlPatterns(
                "/api/dashboard/*",
                "/api/projects/*",
                "/api/auth/me",
                "/api/auth/logout"
        );

        registration.setOrder(2);

        return registration;
    }
}