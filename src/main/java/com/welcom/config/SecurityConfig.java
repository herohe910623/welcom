package com.welcom.config;


import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests().
                mvcMatchers("/", "/sign-up","/check-email-token",
                        "/email-login","/check-email-lgoin","login-link").permitAll()
                        .mvcMatchers(HttpMethod.GET,"/profile/*").permitAll()
                        .anyRequest().authenticated();
    }

}
