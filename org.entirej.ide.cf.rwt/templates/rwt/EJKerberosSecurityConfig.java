package org.entirej;

import org.entirej.applicationframework.rwt.spring.ext.EJDefaultSpringSecurityConfigProvider;
import org.entirej.applicationframework.rwt.spring.ext.EJSpringSecurityContext;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.kerberos.web.authentication.SpnegoAuthenticationProcessingFilter;
import org.springframework.security.kerberos.web.authentication.SpnegoEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

public class EJKerberosSecurityConfig extends EJDefaultSpringSecurityConfigProvider
{
    @Override
    public SecurityFilterChain configure(HttpSecurity http, EJSpringSecurityContext context) throws Exception
    {
        EJKerberosAuthenticationProvider authenticationProvider = new EJKerberosAuthenticationProvider();

        http.csrf(csrf -> csrf.disable())
                .authenticationProvider(authenticationProvider.kerberosServiceAuthenticationProvider())
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/resources/**", "/403").permitAll()
                        .anyRequest().authenticated())
                .logout(logout -> logout
                        .permitAll()
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/"))
                .exceptionHandling(exception -> exception
                        .accessDeniedPage("/403")
                        .authenticationEntryPoint(new SpnegoEntryPoint()))
                .addFilterBefore(spnegoAuthenticationProcessingFilter(context.authenticationManagerBean()),
                        BasicAuthenticationFilter.class);

        return http.build();
    }

    private SpnegoAuthenticationProcessingFilter spnegoAuthenticationProcessingFilter(
            AuthenticationManager authenticationManager)
    {
        SpnegoAuthenticationProcessingFilter filter = new SpnegoAuthenticationProcessingFilter();
        filter.setAuthenticationManager(authenticationManager);
        return filter;
    }
}
