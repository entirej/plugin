package org.entirej.ide.cf.rwt;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.entirej.framework.core.extensions.properties.EJCoreFrameworkExtensionProperties;
import org.entirej.framework.core.extensions.properties.EJCoreFrameworkExtensionProperty;
import org.entirej.framework.core.properties.definitions.EJPropertyDefinitionType;
import org.entirej.framework.core.properties.definitions.interfaces.EJFrameworkExtensionProperties;
import org.entirej.framework.plugin.framework.properties.EntirejPluginPropertiesEnterpriseEdition;
import org.entirej.ide.core.cf.CFProjectHelper;
import org.osgi.framework.Bundle;

final class RWTProjectConfiguration
{
    private static final String ENTIREJ_GROUP_ID             = "org.entirej";
    private static final String RWT_SPRING_ARTIFACT_ID       = "entirej-rwt-spring";
    private static final String SPRING_KERBEROS_GROUP_ID     = "org.springframework.security.kerberos";
    private static final String SPRING_KERBEROS_CORE         = "spring-security-kerberos-core";
    private static final String SPRING_KERBEROS_WEB          = "spring-security-kerberos-web";
    private static final String SPRING_KERBEROS_VERSION      = "2.1.0";
    private static final String SPRING_SECURITY              = "SPRING_SECURITY";
    private static final String SPRING_SECURITY_CONFIG       = "SPRING_SECURITY_CONFIG";
    private static final String SPRING_SECURITY_AUTH         = "SPRING_SECURITY_AUTH";

    private RWTProjectConfiguration()
    {
    }

    static void ensureSpringDependency(IJavaProject project, IProgressMonitor monitor) throws CoreException, IOException
    {
        CFProjectHelper.ensureMavenDependency(project, ENTIREJ_GROUP_ID, RWT_SPRING_ARTIFACT_ID, null, null, monitor);
    }

    static void ensureKerberosDependencies(IJavaProject project, IProgressMonitor monitor)
            throws CoreException, IOException
    {
        CFProjectHelper.ensureMavenDependency(project, SPRING_KERBEROS_GROUP_ID, SPRING_KERBEROS_CORE,
                SPRING_KERBEROS_VERSION, null, monitor);
        CFProjectHelper.ensureMavenDependency(project, SPRING_KERBEROS_GROUP_ID, SPRING_KERBEROS_WEB,
                SPRING_KERBEROS_VERSION, null, monitor);
    }

    static boolean configureSpringSecurity(EntirejPluginPropertiesEnterpriseEdition entirejProperties,
            String configurationClass, String authenticationProviderClass)
    {
        EJFrameworkExtensionProperties definedProperties = entirejProperties.getApplicationDefinedProperties();
        if (!(definedProperties instanceof EJCoreFrameworkExtensionProperties root))
        {
            throw new IllegalStateException("The project has no editable application-defined properties.");
        }

        boolean changed = false;
        EJCoreFrameworkExtensionProperties settings = root.getPropertyGroup(SPRING_SECURITY);
        if (settings == null)
        {
            settings = new EJCoreFrameworkExtensionProperties(root.getFormProperties(), root.getBlockProperties(),
                    SPRING_SECURITY, root);
            root.addPropertyGroup(settings);
            changed = true;
        }

        changed |= setProjectClassProperty(settings, SPRING_SECURITY_CONFIG, configurationClass);
        changed |= setProjectClassProperty(settings, SPRING_SECURITY_AUTH, authenticationProviderClass);
        return changed;
    }

    static void addFileIfMissing(IJavaProject project, Bundle bundle, String sourceFile, String targetFileName)
            throws IOException
    {
        if (!exists(project, targetFileName))
        {
            CFProjectHelper.addFile(project, bundle, sourceFile, targetFileName);
        }
    }

    static void addFileIfMissing(IJavaProject project, Bundle bundle, String sourceFile, String targetFileName,
            Map<String, String> parameters) throws IOException
    {
        if (!exists(project, targetFileName))
        {
            CFProjectHelper.addFile(project, bundle, sourceFile, targetFileName, parameters);
        }
    }

    private static boolean setProjectClassProperty(EJCoreFrameworkExtensionProperties settings, String name,
            String value)
    {
        EJCoreFrameworkExtensionProperty property = settings.getAllProperties().get(name);
        if (property == null || property.getPropertyType() != EJPropertyDefinitionType.PROJECT_CLASS_FILE)
        {
            property = new EJCoreFrameworkExtensionProperty(EJPropertyDefinitionType.PROJECT_CLASS_FILE, name);
            property.setValue(value);
            settings.addProperty(property);
            return true;
        }
        if (!Objects.equals(property.getValue(), value))
        {
            property.setValue(value);
            return true;
        }
        return false;
    }

    private static boolean exists(IJavaProject project, String targetFileName)
    {
        IFile target = project.getProject().getFile(targetFileName);
        return target.exists() || target.getLocation() != null && target.getLocation().toFile().exists();
    }
}
