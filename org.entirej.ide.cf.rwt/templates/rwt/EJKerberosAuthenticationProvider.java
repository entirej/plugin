package org.entirej;
import org.entirej.applicationframework.rwt.spring.ext.EJSpringSecurityAuthenticationProvider;
import org.springframework.core.io.FileSystemResource;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.kerberos.authentication.KerberosServiceAuthenticationProvider;
import org.springframework.security.kerberos.authentication.sun.SunJaasKerberosTicketValidator;
import org.springframework.security.kerberos.client.config.SunJaasKrb5LoginConfig;
import org.springframework.security.kerberos.client.ldap.KerberosLdapContextSource;
import org.springframework.security.ldap.authentication.ad.ActiveDirectoryLdapAuthenticationProvider;
import org.springframework.security.ldap.search.FilterBasedLdapUserSearch;
import org.springframework.security.ldap.userdetails.LdapUserDetailsMapper;
import org.springframework.security.ldap.userdetails.LdapUserDetailsService;

public class EJKerberosAuthenticationProvider implements EJSpringSecurityAuthenticationProvider
{

    private String adDomain         = "EXAMPLE.LOCAL";//

    private String adServer         = "ldap://SERVER.EXAMPLE.LOCAL:389";

    private String servicePrincipal = "HTTP/application.example.com@EXAMPLE.LOCAL";//service principal 

    private String keytabLocation   = "PATH/SPN_KEYTAB.keytab";//Path to keytab file

    private String ldapSearchBase   = "dc=EXAMPLE,dc=LOCAL";

    private String ldapSearchFilter = "(| (userPrincipalName={0}) (sAMAccountName={0}))";

    @Override
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception
    {
        auth.authenticationProvider(createActiveDirectoryLdapAuthenticationProvider()).authenticationProvider(kerberosServiceAuthenticationProvider());
    }

    ActiveDirectoryLdapAuthenticationProvider createActiveDirectoryLdapAuthenticationProvider()
    {
        ActiveDirectoryLdapAuthenticationProvider provider = new ActiveDirectoryLdapAuthenticationProvider(adDomain, adServer);

        provider.setUseAuthenticationRequestCredentials(true);
        
        
        return provider;
    }

    public KerberosServiceAuthenticationProvider kerberosServiceAuthenticationProvider()
    {
        KerberosServiceAuthenticationProvider provider = new KerberosServiceAuthenticationProvider();
        provider.setTicketValidator(sunJaasKerberosTicketValidator());
        provider.setUserDetailsService(ldapUserDetailsService());
     
        try
        {
            provider.afterPropertiesSet();
        }
        catch (Exception e)
        {
            
            e.printStackTrace();
        }
        return provider;
    }

    public SunJaasKerberosTicketValidator sunJaasKerberosTicketValidator()
    {
        SunJaasKerberosTicketValidator ticketValidator = new SunJaasKerberosTicketValidator();
        ticketValidator.setServicePrincipal(servicePrincipal);
        ticketValidator.setKeyTabLocation(new FileSystemResource(keytabLocation));
       
        ticketValidator.setDebug(true);
        try
        {
            ticketValidator.afterPropertiesSet();
        }
        catch (Exception e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return ticketValidator;
    }

    public KerberosLdapContextSource kerberosLdapContextSource()
    {
        KerberosLdapContextSource contextSource = new KerberosLdapContextSource(adServer);
       
        contextSource.setLoginConfig(loginConfig());
        try
        {
            contextSource.afterPropertiesSet();
        }
        catch (Exception e)
        {
            
            e.printStackTrace();
        }
        return contextSource;
    }

    public SunJaasKrb5LoginConfig loginConfig()
    {
        SunJaasKrb5LoginConfig loginConfig = new SunJaasKrb5LoginConfig();
        loginConfig.setKeyTabLocation(new FileSystemResource(keytabLocation));
        loginConfig.setServicePrincipal(servicePrincipal);
        loginConfig.setDebug(true);
        loginConfig.setIsInitiator(true);
        try
        {
            loginConfig.afterPropertiesSet();
        }
        catch (Exception e)
        {
           
            e.printStackTrace();
        }
        return loginConfig;
    }

    public LdapUserDetailsService ldapUserDetailsService()
    {
        FilterBasedLdapUserSearch userSearch = new FilterBasedLdapUserSearch(ldapSearchBase, ldapSearchFilter, kerberosLdapContextSource());
        LdapUserDetailsService service = new LdapUserDetailsService(userSearch);
        service.setUserDetailsMapper(new LdapUserDetailsMapper());
        
        return service;
    }

}