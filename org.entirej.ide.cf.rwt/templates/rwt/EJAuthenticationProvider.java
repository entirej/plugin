package org.entirej;

import org.entirej.applicationframework.rwt.spring.ext.EJSpringSecurityAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;

public class EJAuthenticationProvider implements EJSpringSecurityAuthenticationProvider
{

    @Override
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception
    {
        // Basic In Memory Authentication 
        auth.inMemoryAuthentication().withUser("user").password("user").roles("USER");
        auth.inMemoryAuthentication().withUser("admin").password("admin").roles("ADMIN");

        
        //for JDBC Authentication
        /**
          auth.jdbcAuthentication()
                        .dataSource(dataSource)
                        .withDefaultSchema()
                        .withUser("user").password("password").roles("USER").and()
                        .withUser("admin").password("password").roles("USER", "ADMIN");
         */
            
        //for LDAP
        /**
          auth.ldapAuthentication()
                        .userDnPatterns("uid={0},ou=people")
                        .groupSearchBase("ou=groups");
         */ 
        
        
        //more information at http://docs.spring.io/spring-security/site/docs/current/reference/html/jc.html#jc-authentication-jdbc
    }

}
