package org.entirej;

import org.entirej.applicationframework.rwt.spring.ext.EJDefaultSpringSecurityConfigProvider;
import org.entirej.applicationframework.rwt.spring.ext.EJSpringSecurityContext;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.authentication.rememberme.InMemoryTokenRepositoryImpl;

public class EJSecurityConfig extends EJDefaultSpringSecurityConfigProvider
{

    @Override
    public void configure(HttpSecurity http,EJSpringSecurityContext context) throws Exception
    {
       
        super.configure(http);
        
        http.authorizeRequests().antMatchers("/resources/**", "/login.html/**").
        permitAll().anyRequest()/*.hasAuthority("USER").antMatchers("/**")*/.authenticated().and().formLogin().loginPage("/login.html").usernameParameter("username").passwordParameter("password")
        .permitAll()
        .and()
        .logout()
        .permitAll()
        .logoutUrl("/logout")
        .logoutSuccessUrl("/login.html?logout")
        .and()
        .exceptionHandling().accessDeniedPage("/403.html")
        /*for remember me option note:  InMemoryTokenRepositoryImpl use only for testing, On production replace with PersistentTokenRepository*/
        .and().rememberMe().rememberMeParameter("rememberme").tokenRepository(new InMemoryTokenRepositoryImpl()).useSecureCookie(true).tokenValiditySeconds(60*60*24);

       

        
      
        
    }

}
