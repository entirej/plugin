package org.entirej.ide.cf.rwt;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.entirej.framework.plugin.framework.properties.EntirejPluginPropertiesEnterpriseEdition;
import org.entirej.framework.plugin.framework.properties.EntirejPropertiesUtils;
import org.entirej.framework.plugin.framework.properties.writer.EntireJPropertiesWriter;
import org.entirej.ide.core.EJCoreLog;
import org.entirej.ide.core.project.EJProject;
import org.entirej.ide.core.spi.FeatureConfigProvider;
import org.osgi.framework.Bundle;

public class SpringFeatureConfigProvider implements FeatureConfigProvider
{
    @Override
    public String getProviderName()
    {
        return "Add Spring Security Configuration";
    }

    @Override
    public String getDescription()
    {
        return "Configure project with Spring Security.";
    }

    @Override
    public String getProviderId()
    {
        return "org.entirej.ide.cf.rwt.SpringFeatureConfig";
    }

    @Override
    public boolean isSupport(IJavaProject project)
    {
        try
        {
            return project.findType("org.eclipse.rwt.EJ_RWT") != null
                    && project.findType("org.entirej.applicationframework.rwt.spring.EJSpringSupport") == null;
        }
        catch (JavaModelException e)
        {
            EJCoreLog.logException(e);
            return false;
        }
    }

    @Override
    public void config(IJavaProject project, IProgressMonitor monitor)
    {
        try
        {
            RWTProjectConfiguration.ensureSpringDependency(project, monitor);

            Bundle bundle = EJCFRwtPlugin.getDefault().getBundle();
            RWTProjectConfiguration.addFileIfMissing(project, bundle, RWT_APP_AUTH,
                    "src/org/entirej/EJAuthenticationProvider.java");
            RWTProjectConfiguration.addFileIfMissing(project, bundle, RWT_APP_AUTH_CONFIG,
                    "src/org/entirej/EJSecurityConfig.java");
            RWTProjectConfiguration.addFileIfMissing(project, bundle, RWT_APP_ACCESSDENIEDSERVLET,
                    "src/org/entirej/AccessDeniedServlet.java");
            RWTProjectConfiguration.addFileIfMissing(project, bundle, RWT_APP_LOGINSERVLET,
                    "src/org/entirej/LoginServlet.java");
            RWTProjectConfiguration.addFileIfMissing(project, bundle, RWT_WEB_BANNER,
                    "WebContent/resources/banner.png");

            Map<String, String> parameters = new HashMap<>();
            parameters.put("%WEB_CONTEXT%", project.getElementName());
            RWTProjectConfiguration.addFileIfMissing(project, bundle, RWT_WEB_LOGIN, "WebContent/login.html",
                    parameters);
            RWTProjectConfiguration.addFileIfMissing(project, bundle, RWT_WEB_403, "WebContent/403.html",
                    parameters);

            saveSecurityProperties(project, monitor, "org.entirej.EJSecurityConfig",
                    "org.entirej.EJAuthenticationProvider");
            project.getProject().refreshLocal(IResource.DEPTH_INFINITE, monitor);
        }
        catch (Exception e)
        {
            EJCoreLog.logException(e);
        }
    }

    private void saveSecurityProperties(IJavaProject project, IProgressMonitor monitor, String configurationClass,
            String authenticationProviderClass) throws Exception
    {
        EntirejPluginPropertiesEnterpriseEdition entirejProperties = EntirejPropertiesUtils.retrieveEntirejProperties(project);
        if (entirejProperties != null && RWTProjectConfiguration.configureSpringSecurity(entirejProperties,
                configurationClass, authenticationProviderClass))
        {
            EntireJPropertiesWriter saver = new EntireJPropertiesWriter();
            saver.saveEntireJProperitesFile(entirejProperties, EJProject.getPropertiesFile(project.getProject()), monitor);
        }
    }

    private static final String RWT_APP_AUTH                = "/templates/rwt/EJAuthenticationProvider.java";
    private static final String RWT_APP_AUTH_CONFIG         = "/templates/rwt/EJSecurityConfig.java";
    private static final String RWT_APP_ACCESSDENIEDSERVLET = "/templates/rwt/AccessDeniedServlet.java";
    private static final String RWT_APP_LOGINSERVLET        = "/templates/rwt/LoginServlet.java";
    private static final String RWT_WEB_LOGIN               = "/templates/rwt/login.html";
    private static final String RWT_WEB_403                 = "/templates/rwt/403.html";
    private static final String RWT_WEB_BANNER              = "/templates/rwt/banner.png";
}
