package org.entirej;

import org.entirej.applicationframework.rwt.spring.ext.EJSpringSecurityAuthenticationProvider;
import org.springframework.core.io.FileSystemResource;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.kerberos.authentication.KerberosServiceAuthenticationProvider;
import org.springframework.security.kerberos.authentication.sun.SunJaasKerberosTicketValidator;

public class EJKerberosAuthenticationProvider implements EJSpringSecurityAuthenticationProvider
{
    private String servicePrincipal = "HTTP/application.example.com@EXAMPLE.LOCAL";
    private String keytabLocation = "PATH/SPN_KEYTAB.keytab";

    public KerberosServiceAuthenticationProvider kerberosServiceAuthenticationProvider() throws Exception
    {
        KerberosServiceAuthenticationProvider provider = new KerberosServiceAuthenticationProvider();
        provider.setTicketValidator(sunJaasKerberosTicketValidator());
        provider.setUserDetailsService(userDetailsService());
        provider.afterPropertiesSet();
        return provider;
    }

    private SunJaasKerberosTicketValidator sunJaasKerberosTicketValidator() throws Exception
    {
        SunJaasKerberosTicketValidator ticketValidator = new SunJaasKerberosTicketValidator();
        ticketValidator.setServicePrincipal(servicePrincipal);
        ticketValidator.setKeyTabLocation(new FileSystemResource(keytabLocation));
        ticketValidator.setDebug(false);
        ticketValidator.afterPropertiesSet();
        return ticketValidator;
    }

    private UserDetailsService userDetailsService()
    {
        return username -> User.withUsername(username)
                .password("{noop}kerberos")
                .roles("USER")
                .build();
    }
}
