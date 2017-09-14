/*******************************************************************************
 * Copyright 2013 CRESOFT AG
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * Contributors:
 *     CRESOFT AG - initial API and implementation
 ******************************************************************************/
package org.entirej.ide.core;

import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.equinox.internal.p2.ui.ProvUI;
import org.eclipse.equinox.internal.p2.ui.ProvUIActivator;
import org.eclipse.equinox.p2.repository.artifact.IArtifactRepositoryManager;
import org.eclipse.equinox.p2.repository.metadata.IMetadataRepositoryManager;
import org.eclipse.equinox.p2.ui.ProvisioningUI;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

public class EJCorePlugin extends AbstractUIPlugin implements IStartup
{
    private BundleContext       bundleContext;
    public static final String  ID = "org.entirej.ide.core";
    private static final String UPDATE_SITE_URL = "http://entirej.org/entirej/plugin/updatesite/releases/r5.x";

    private static EJCorePlugin plugin;

    public EJCorePlugin()
    {
        plugin = this;
    }

    public void start(BundleContext context) throws Exception
    {

        super.start(context);
        this.bundleContext = context;
        setupUpdatSite();
    }

    public void stop(BundleContext context) throws Exception
    {
        super.stop(context);
        plugin = null;
    }

    public BundleContext getBundleContext()
    {
        return bundleContext;
    }

    /**
     * Returns the shared instance.
     */
    public static EJCorePlugin getDefault()
    {
        return plugin;
    }

    public void earlyStartup()
    {
        // ignore
    }

    public static String getID()
    {
        return getDefault().getBundle().getSymbolicName();
    }

    public static Display getStandardDisplay()
    {
        Display display;
        display = Display.getCurrent();
        if (display == null)
            display = Display.getDefault();
        return display;
    }

    public static IWorkspace getWorkspace()
    {
        return ResourcesPlugin.getWorkspace();
    }

    public static IWorkbenchWindow getActiveWorkbenchWindow()
    {
        return getDefault().getWorkbench().getActiveWorkbenchWindow();
    }
    
    
    @SuppressWarnings("restriction")
    void setupUpdatSite()
    {
       
        try
        {
            final ProvisioningUI ui = ProvUIActivator.getDefault().getProvisioningUI();
            IArtifactRepositoryManager artifactManager = ProvUI.getArtifactRepositoryManager(ui.getSession());
            artifactManager.addRepository(new URI(UPDATE_SITE_URL));

            IMetadataRepositoryManager metadataManager = ProvUI.getMetadataRepositoryManager(ui.getSession());
            metadataManager.addRepository(new URI(UPDATE_SITE_URL));
        }
        catch (URISyntaxException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
