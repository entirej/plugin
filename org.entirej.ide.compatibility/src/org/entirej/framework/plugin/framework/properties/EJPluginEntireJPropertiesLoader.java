/*******************************************************************************
 * Copyright 2013 CRESOFT AG
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 * 
 * Contributors: CRESOFT AG - initial API and implementation
 ******************************************************************************/
package org.entirej.framework.plugin.framework.properties;

import java.util.WeakHashMap;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.entirej.ide.core.EJCoreLog;

public class EJPluginEntireJPropertiesLoader
{
    
    private static final WeakHashMap<IJavaProject, EJPluginEntireJProperties> WEAK_LOADERS = new WeakHashMap<IJavaProject, EJPluginEntireJProperties>();
    static
    {
        ResourcesPlugin.getWorkspace().addResourceChangeListener(new IResourceChangeListener()
        {
            final IResourceDeltaVisitor deltaVisitor = new IResourceDeltaVisitor()
                                                     {
                                                         
                                                         public boolean visit(IResourceDelta delta) throws CoreException
                                                         {
                                                             switch (delta.getKind())
                                                             {
                                                                 case IResourceDelta.REPLACED:
                                                                 case IResourceDelta.CHANGED:
                                                                     IResource resource = delta.getResource();
                                                                     if (resource instanceof IFile)
                                                                     {
                                                                         IFile cfile = (IFile) resource;
                                                                         
                                                                         if (cfile.getName().equals("application.ejprop") && cfile.getProject() != null)
                                                                         {
                                                                             IJavaProject project = JavaCore.create(cfile.getProject());
                                                                             WEAK_LOADERS.remove(project);
                                                                             return false;
                                                                         }
                                                                     }
                                                                     break;
                                                             
                                                             }
                                                             
                                                             return true;
                                                         }
                                                     };
            
            public void resourceChanged(IResourceChangeEvent event)
            {
                try
                {
                    event.getDelta().accept(deltaVisitor);
                }
                catch (CoreException e)
                {
                    EJCoreLog.log(e);
                }
                
            }
        }, IResourceChangeEvent.POST_CHANGE);
    }
    
    private EJPluginEntireJPropertiesLoader()
    {
    }
    
    public static void reload(final IJavaProject javaProject)
    {
        WEAK_LOADERS.remove(javaProject);
        getEntireJProperties(javaProject);
    }
    
    public static EJPluginEntireJProperties getEntireJProperties(final IJavaProject javaProject)
    {
        EJPluginEntireJProperties properties = WEAK_LOADERS.get(javaProject);
        
        if (properties == null)
        {
            try
            {
                properties = EntirejPropertiesUtils.retrieveEntirejProperties(javaProject);
                WEAK_LOADERS.put(javaProject, properties);
            }
            catch (CoreException e)
            {
                EJCoreLog.log(e);
            }
            
        }
        
        return properties;
        
    }
    
}
