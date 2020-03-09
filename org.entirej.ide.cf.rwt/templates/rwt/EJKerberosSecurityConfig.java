package org.entirej;

import org.entirej.applicationframework.rwt.spring.ext.EJDefaultSpringSecurityConfigProvider;
import org.entirej.applicationframework.rwt.spring.ext.EJSpringSecurityContext;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.kerberos.web.authentication.SpnegoAuthenticationProcessingFilter;
import org.springframework.security.kerberos.web.authentication.SpnegoEntryPoint;
import org.springframework.security.web.authentication.rememberme.InMemoryTokenRepositoryImpl;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

public class EJKerberosSecurityConfig extends EJDefaultSpringSecurityConfigProvider
{

    @Override
    public void configure(HttpSecurity http,EJSpringSecurityContext context) throws Exception
    {
       
        super.configure(http,context);
        
        http.authorizeRequests().antMatchers("/resources/**", "/login/**").
        permitAll().anyRequest()/*.hasAuthority("USER").antMatchers("/**")*/.authenticated().and().formLogin().loginPage("/login").usernameParameter("username").passwordParameter("password")
        .permitAll()
        .and()
        .logout()
        .permitAll()

        
        .logoutUrl("/logout")
        .logoutSuccessUrl("/login?logout")
        .and()
        .exceptionHandling().accessDeniedPage("/403").authenticationEntryPoint(new SpnegoEntryPoint("/login"))
        /*for remember me option note:  InMemoryTokenRepositoryImpl use only for testing, On production replace with PersistentTokenRepository*/
        .and().rememberMe().rememberMeParameter("rememberme").tokenRepository(new InMemoryTokenRepositoryImpl()).useSecureCookie(true).tokenValiditySeconds(60*60*24)
        //;/*kerberos */
        .and()
        .addFilterBefore(
                        spnegoAuthenticationProcessingFilter(context.authenticationManagerBean()),
                        BasicAuthenticationFilter.class);

       

        
      
        
    }
    
    
    public SpnegoAuthenticationProcessingFilter spnegoAuthenticationProcessingFilter(AuthenticationManager authenticationManager)
    {
        SpnegoAuthenticationProcessingFilter filter = new SpnegoAuthenticationProcessingFilter();
        filter.setAuthenticationManager(authenticationManager);
        return filter;
    }

}
