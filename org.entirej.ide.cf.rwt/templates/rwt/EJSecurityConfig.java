package org.entirej;

import org.entirej.applicationframework.rwt.spring.ext.EJDefaultSpringSecurityConfigProvider;
import org.entirej.applicationframework.rwt.spring.ext.EJSpringSecurityContext;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.rememberme.InMemoryTokenRepositoryImpl;

public class EJSecurityConfig extends EJDefaultSpringSecurityConfigProvider
{
    @Override
    public SecurityFilterChain configure(HttpSecurity http, EJSpringSecurityContext context) throws Exception
    {
        http.csrf(csrf -> csrf.disable())
                .userDetailsService(new EJAuthenticationProvider().userDetailsService())
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/resources/**", "/login/**").permitAll()
                        .anyRequest().authenticated())
                .formLogin(form -> form
                        .loginPage("/login")
                        .usernameParameter("username")
                        .passwordParameter("password")
                        .permitAll())
                .logout(logout -> logout
                        .permitAll()
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout"))
                .exceptionHandling(exception -> exception.accessDeniedPage("/403"))
                .rememberMe(remember -> remember
                        .rememberMeParameter("rememberme")
                        .tokenRepository(new InMemoryTokenRepositoryImpl())
                        .useSecureCookie(true)
                        .tokenValiditySeconds(60 * 60 * 24));

        return http.build();
    }
}
