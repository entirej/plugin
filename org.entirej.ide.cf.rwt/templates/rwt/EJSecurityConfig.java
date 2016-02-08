package org.entirej;

import org.entirej.applicationframework.rwt.spring.ext.EJDefaultSpringSecurityConfigProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

public class EJSecurityConfig extends EJDefaultSpringSecurityConfigProvider
{

    @Override
    public void configure(HttpSecurity http) throws Exception
    {
       
        super.configure(http);
        
        http.authorizeRequests().antMatchers("/resources/**", "/login.html/**").
        permitAll().anyRequest().hasAuthority("CRESOFT_DEV").antMatchers("/**").authenticated().and().formLogin().loginPage("/login.html").usernameParameter("username").passwordParameter("password")
        .permitAll()
        .and()
        .logout()
        .permitAll()
        .logoutUrl("/logout")
        .logoutSuccessUrl("/login.html?logout");


        
      
        
    }

}
