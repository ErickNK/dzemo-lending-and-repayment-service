package com.flycode.lendingandrepaymentservice.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;


@Profile("!integration-tests")
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    @Autowired
    UserDetailsService userDetailsService;

    @Autowired
    Environment environment;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                .antMatchers("/actuator/**", "/api/auth/populate-records", "/api-docs/**", "/swagger-ui.html", "/swagger-ui/**").permitAll()
                .antMatchers(POST, "/api/auth/login/**", "/api/auth/token/refresh").permitAll()
                .antMatchers(POST, "/api/auth/register", "/api/auth/role", "/api/auth/user/role", "/api/auth/users", "/api/loans/data-dump").hasAnyAuthority("ROLE_ADMIN")
                .antMatchers(POST, "/api/loans/request-loan", "/api/loans/pay-loan").hasAnyAuthority("ROLE_USER")
                .anyRequest().authenticated();


        JWTAuthenticationFilter customAuthenticationFilter = new JWTAuthenticationFilter(authenticationManagerBean(), environment);
        customAuthenticationFilter.setFilterProcessesUrl("/api/auth/login");
        customAuthenticationFilter.setPostOnly(Boolean.TRUE);
        http
                .addFilter(customAuthenticationFilter)
                .addFilterBefore(new JWTAuthorizationFilter(environment), UsernamePasswordAuthenticationFilter.class);

        http
                .csrf()
                    .disable()
                .sessionManagement()
                    .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                    .and()
                .headers()
                    .frameOptions().disable();
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }
}
