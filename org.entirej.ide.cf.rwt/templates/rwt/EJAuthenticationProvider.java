package org.entirej;

import java.util.Collection;
import java.util.Collections;

import org.entirej.applicationframework.rwt.spring.ext.EJSpringSecurityAuthenticationProvider;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.ldap.DefaultSpringSecurityContextSource;
import org.springframework.security.ldap.authentication.BindAuthenticator;
import org.springframework.security.ldap.authentication.LdapAuthenticationProvider;
import org.springframework.security.ldap.userdetails.LdapAuthoritiesPopulator;

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
         * All user passwords are password.
            see test LDAP server info at http://www.forumsys.com/tutorials/integration-how-to/ldap/online-ldap-test-server/
			#mathematicians
			riemann
			gauss
			euler
			euclid
			
			#scientists
			einstein
			newton
			galieleo
			tesla        
 
        */
        //auth.authenticationProvider(createLdapAuthenticationProvider());
         
        
        
        //more information at http://docs.spring.io/spring-security/site/docs/current/reference/html/jc.html#jc-authentication-jdbc
    }
    
    
    private LdapAuthenticationProvider createLdapAuthenticationProvider() throws Exception
    {
        DefaultSpringSecurityContextSource context = new DefaultSpringSecurityContextSource("ldap://ldap.forumsys.com:389");
        context.setUserDn("cn=read-only-admin,dc=example,dc=com");
        context.setPassword("password");
        context.afterPropertiesSet();
        

        
        BindAuthenticator bindAuthenticator = new BindAuthenticator(context);
        bindAuthenticator.setUserDnPatterns(new String []{"uid={0},dc=example,dc=com"});
        bindAuthenticator.afterPropertiesSet();
        LdapAuthenticationProvider ldapAuthenticationProvider = new LdapAuthenticationProvider(bindAuthenticator, new LdapAuthoritiesPopulator()
        {
            
            @Override
            public Collection<? extends GrantedAuthority> getGrantedAuthorities( DirContextOperations userData,
                    String username)
            {
                // TODO build GrantedAuthority
                return Collections.emptyList();
            }
        });
        return ldapAuthenticationProvider;
    }

}
