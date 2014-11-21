/*******************************************************************************
 * Copyright 2013 Mojave Innovations GmbH
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
 *     Mojave Innovations GmbH - initial API and implementation
 ******************************************************************************/
package org.entirej.ide.core.project;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.PlatformObject;
import org.entirej.ide.core.EJConstants;
import org.entirej.ide.core.EJCoreLog;

public class EJReportProject extends PlatformObject implements IProjectNature
{

    private IProject project;

    protected void addToBuildSpec(String builderID, boolean force) throws CoreException
    {

        IProjectDescription description = getProject().getDescription();
        ICommand builderCommand = getBuilderCommand(description, builderID);

        if (builderCommand == null)
        {
            // Add a new build spec
            ICommand command = description.newCommand();
            command.setBuilderName(builderID);
            setBuilderCommand(description, command, force);
        }
    }

    private ICommand getBuilderCommand(IProjectDescription description, String builderId)
    {
        ICommand[] commands = description.getBuildSpec();
        for (int i = 0; i < commands.length; ++i)
        {
            if (commands[i].getBuilderName().equals(builderId))
            {
                return commands[i];
            }
        }
        return null;
    }

    public IProject getProject()
    {
        return project;
    }

    protected IWorkspace getWorkspace()
    {
        return ResourcesPlugin.getWorkspace();
    }

    protected void removeFromBuildSpec(String builderID) throws CoreException
    {
        IProjectDescription description = getProject().getDescription();
        ICommand[] commands = description.getBuildSpec();
        for (int i = 0; i < commands.length; ++i)
        {
            if (commands[i].getBuilderName().equals(builderID))
            {
                ICommand[] newCommands = new ICommand[commands.length - 1];
                System.arraycopy(commands, 0, newCommands, 0, i);
                System.arraycopy(commands, i + 1, newCommands, i, commands.length - i - 1);
                description.setBuildSpec(newCommands);
                return;
            }
        }
    }

    private void setBuilderCommand(IProjectDescription description, ICommand newCommand, boolean force) throws CoreException
    {

        ICommand[] oldCommands = description.getBuildSpec();
        ICommand oldBuilderCommand = getBuilderCommand(description, newCommand.getBuilderName());

        ICommand[] newCommands;

        if (oldBuilderCommand == null)
        {
            // Add a build spec after other builders
            newCommands = new ICommand[oldCommands.length + 1];
            if (force)
            {
                System.arraycopy(oldCommands, 0, newCommands, 1, oldCommands.length);
                newCommands[0] = newCommand;
            }
            else
            {
                System.arraycopy(oldCommands, 0, newCommands, 0, oldCommands.length);
                newCommands[oldCommands.length] = newCommand;
            }
        }
        else
        {
            for (int i = 0, max = oldCommands.length; i < max; i++)
            {
                if (oldCommands[i] == oldBuilderCommand)
                {
                    oldCommands[i] = newCommand;
                    break;
                }
            }
            newCommands = oldCommands;
        }

        // Commit the spec change into the project
        description.setBuildSpec(newCommands);
        getProject().setDescription(description, null);
    }

    public void setProject(IProject project)
    {
        this.project = project;
    }

    public void configure() throws CoreException
    {
        // addToBuildSpec(EJConstants.EJ_FORM_CONST_BUILDER_ID, true);
        addToBuildSpec(EJConstants.EJ_REPORT_PROPERTIES_BUILDER_ID, false);
         addToBuildSpec(EJConstants.EJ_REPORT_CONST_BUILDER_ID, false);
         addToBuildSpec(EJConstants.EJ_REPORT_BUILDER_ID, false);
    }

    public void deconfigure() throws CoreException
    {
        removeFromBuildSpec(EJConstants.EJ_REPORT_PROPERTIES_BUILDER_ID);
         removeFromBuildSpec(EJConstants.EJ_REPORT_CONST_BUILDER_ID);
         removeFromBuildSpec(EJConstants.EJ_REPORT_BUILDER_ID);
    }

    public static IFile getPropertiesFile(IProject project)
    {
        if (project != null)
            return project.getProject().getFile("src/report.ejprop");

        return null;
    }

    public static boolean hasPluginNature(IProject project)
    {
        try
        {
            return project.hasNature(EJConstants.EJ_REPORT_NATURE);
        }
        catch (CoreException e)
        {
            EJCoreLog.log(e);
            return false;
        }
    }

    public void verifyBuilder()
    {
        try
        {
            IProjectDescription description = getProject().getDescription();
             if (getBuilderCommand(description,
             EJConstants.EJ_REPORT_CONST_BUILDER_ID) == null)
             {
             addToBuildSpec(EJConstants.EJ_REPORT_CONST_BUILDER_ID, true);
             }
             if (getBuilderCommand(description,
                     EJConstants.EJ_REPORT_BUILDER_ID) == null)
             {
                 addToBuildSpec(EJConstants.EJ_REPORT_BUILDER_ID, true);
             }
            if (getBuilderCommand(description, EJConstants.EJ_REPORT_PROPERTIES_BUILDER_ID) == null)
            {
                addToBuildSpec(EJConstants.EJ_REPORT_PROPERTIES_BUILDER_ID, false);
            }
            // if (getBuilderCommand(description,
            // EJConstants.EJ_FORM_BUILDER_ID) == null)
            // {
            // addToBuildSpec(EJConstants.EJ_FORM_BUILDER_ID, false);
            // }
        }
        catch (CoreException e)
        {
            EJCoreLog.log(e);
        }

    }
}
