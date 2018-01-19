package org.entirej.ide.cf.rwt;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IAccessRule;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.entirej.framework.core.properties.definitions.interfaces.EJFrameworkExtensionProperties;
import org.entirej.framework.plugin.framework.properties.EntirejPluginPropertiesEnterpriseEdition;
import org.entirej.framework.plugin.framework.properties.EntirejPropertiesUtils;
import org.entirej.framework.plugin.framework.properties.writer.EntireJPropertiesWriter;
import org.entirej.ide.cf.rwt.lib.spring.RWTSpringRuntimeClasspathContainer;
import org.entirej.ide.core.cf.CFProjectHelper;
import org.entirej.ide.core.project.EJProject;
import org.entirej.ide.core.spi.FeatureConfigProvider;

public class SpringFeatureConfigProvider implements FeatureConfigProvider {

	public SpringFeatureConfigProvider() {
	}

	
	public String getProviderName() {
		return "Add Spring Security Configuration";
	}

	
	public String getDescription() {
		return "Configure project with Spring Security.";
	}

	
	public String getProviderId() {
		return "org.entirej.ide.cf.rwt.SpringFeatureConfig";
	}

	
	public boolean isSupport(IJavaProject project) {

		try {

			if (project.findType("org.eclipse.rwt.EJ_RWT") != null
					&& project.findType("org.entirej.applicationframework.rwt.spring.EJSpringSupport") == null) {
				return true;
			}
		} catch (JavaModelException e) {
			e.printStackTrace();
		}

		return false;
	}


	public void config(IJavaProject project, IProgressMonitor monitor) {

		try {
			CFProjectHelper.addFile(project, EJCFRwtPlugin.getDefault().getBundle(), RWT_APP_AUTH,
					"src/org/entirej/EJAuthenticationProvider.java");
			CFProjectHelper.addFile(project, EJCFRwtPlugin.getDefault().getBundle(), RWT_APP_AUTH_CONFIG,
					"src/org/entirej/EJSecurityConfig.java");

            CFProjectHelper.addFile(project, EJCFRwtPlugin.getDefault().getBundle(), RWT_APP_ACCESSDENIEDSERVLET, "src/org/entirej/AccessDeniedServlet.java");
            CFProjectHelper.addFile(project, EJCFRwtPlugin.getDefault().getBundle(), RWT_APP_LOGINSERVLET, "src/org/entirej/LoginServlet.java");
			CFProjectHelper.addFile(project, EJCFRwtPlugin.getDefault().getBundle(), RWT_WEB_BANNER,
					"WebContent/resources/banner.png");
			Map<String, String> params = new HashMap<String, String>();
			params.put("%WEB_CONTEXT%", project.getElementName());
			CFProjectHelper.addFile(project, EJCFRwtPlugin.getDefault().getBundle(), RWT_WEB_LOGIN,
					"WebContent/login.html", params);
			CFProjectHelper.addFile(project, EJCFRwtPlugin.getDefault().getBundle(), RWT_WEB_403, "WebContent/403.html",
					params);

			IClasspathAttribute[] attributes = getClasspathAttributes();

			CFProjectHelper.addToClasspath(project, JavaCore.newContainerEntry(RWTSpringRuntimeClasspathContainer.ID,
					new IAccessRule[0], attributes, true));

			EntirejPluginPropertiesEnterpriseEdition entirejProperties = EntirejPropertiesUtils
					.retrieveEntirejProperties(project);
			if (entirejProperties != null) {
				EJFrameworkExtensionProperties definedProperties = entirejProperties.getApplicationDefinedProperties();
				if (definedProperties != null) {
					EJFrameworkExtensionProperties settings = definedProperties.getPropertyGroup(SPRING_SECURITY);
					if (settings != null) {
						settings.setPropertyValue(SPRING_SECURITY_CONFIG, "org.entirej.EJSecurityConfig");
						settings.setPropertyValue(SPRING_SECURITY_AUTH, "org.entirej.EJAuthenticationProvider");
					}
				}

				EntireJPropertiesWriter saver = new EntireJPropertiesWriter();
				saver.saveEntireJProperitesFile(entirejProperties, EJProject.getPropertiesFile(project.getProject()),
						monitor);
			}
			project.getProject().refreshLocal(IResource.DEPTH_INFINITE, monitor);

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public IClasspathAttribute[] getClasspathAttributes() {

		return new IClasspathAttribute[] { new IClasspathAttribute() {

			public String getValue() {
				return "/WEB-INF/lib";
			}

			public String getName() {
				return "org.eclipse.jst.component.dependency";
			}
		} };
	}

	public static final String SPRING_SECURITY = "SPRING_SECURITY";
	public static final String SPRING_SECURITY_CONFIG = "SPRING_SECURITY_CONFIG";

	public static final String SPRING_SECURITY_AUTH = "SPRING_SECURITY_AUTH";

	private static final String RWT_APP_AUTH = "/templates/rwt/EJAuthenticationProvider.java";
	private static final String RWT_APP_AUTH_CONFIG = "/templates/rwt/EJSecurityConfig.java";
	private static final String RWT_WEB_LOGIN = "/templates/rwt/login.html";
	private static final String RWT_WEB_403 = "/templates/rwt/403.html";
	private static final String RWT_WEB_BANNER = "/templates/rwt/banner.png";
	

    private static final String RWT_APP_ACCESSDENIEDSERVLET          = "/templates/rwt/AccessDeniedServlet.java";
    private static final String RWT_APP_LOGINSERVLET           = "/templates/rwt/LoginServlet.java";

}
