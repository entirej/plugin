package org.entirej;

import org.entirej.applicationframework.rwt.spring.ext.EJSpringSecurityAuthenticationProvider;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

public class EJAuthenticationProvider implements EJSpringSecurityAuthenticationProvider
{
    public UserDetailsService userDetailsService()
    {
        return new InMemoryUserDetailsManager(
                User.withUsername("user").password("{noop}user").roles("USER").build(),
                User.withUsername("admin").password("{noop}admin").roles("USER", "ADMIN").build());
    }
}
