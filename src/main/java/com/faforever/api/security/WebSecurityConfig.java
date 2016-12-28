package com.faforever.api.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

import javax.inject.Inject;

@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

  @Inject
  @Profile("dev")
  public void globalUserDetails(AuthenticationManagerBuilder auth) throws Exception {
    auth.inMemoryAuthentication()
        .withUser("user").password("password").roles("USER");
  }

  @Bean
  @Override
  public AuthenticationManager authenticationManagerBean() throws Exception {
    return super.authenticationManagerBean();
  }

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http.headers().cacheControl().disable();

    http.csrf().disable()
        .authorizeRequests()
        .antMatchers(HttpMethod.OPTIONS).permitAll()
        .antMatchers(HttpMethod.POST, "/login").permitAll()
        .antMatchers("/v2/api-docs/**").permitAll()
        .antMatchers("/springfox-swagger-ui/**").permitAll()
        .antMatchers("/swagger-ui.html").permitAll()
        .antMatchers("/**").authenticated()
        .and().httpBasic()
        .and().formLogin();
  }
}
