package com.chatapp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // Disable security for /api/users/register endpoint and allow all requests
        http
            .csrf().disable()
            .authorizeRequests()
                .antMatchers("/api/users/**", "/sendTestEmail", "/sendTemplateEmail").permitAll()  // Allow access to register endpoint
                .anyRequest().authenticated();  // Authenticate other requests
    }
}
