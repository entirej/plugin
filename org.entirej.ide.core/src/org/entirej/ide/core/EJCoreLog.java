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

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.swt.widgets.Display;

public class EJCoreLog
{
    private EJCoreLog()
    {
    }

    public static void log(IStatus status)
    {
        ResourcesPlugin.getPlugin().getLog().log(status);
    }

    public static void logErrorMessage(String message)
    {
        log(new Status(IStatus.ERROR, EJCorePlugin.ID, IStatus.ERROR, message, null));
    }

    public static void logInfoMessage(String message)
    {
        log(new Status(IStatus.INFO, EJCorePlugin.ID, IStatus.INFO, message, null));
    }

    public static void logWarnningMessage(String message)
    {
        log(new Status(IStatus.WARNING, EJCorePlugin.ID, IStatus.WARNING, message, null));
    }

    public static void logWarnning(Throwable e)
    {
        if (e instanceof InvocationTargetException)
        {
            e = ((InvocationTargetException) e).getTargetException();
        }

        IStatus status = null;

        if (e instanceof CoreException)
        {
            status = ((CoreException) e).getStatus();
        }
        else
        {
            status = new Status(IStatus.WARNING, EJCorePlugin.ID, IStatus.OK, e.getMessage(), e);
        }
        log(status);
    }

    public static void logException(Throwable e, final String title, String message)
    {
        if (e instanceof InvocationTargetException)
        {
            e = ((InvocationTargetException) e).getTargetException();
        }
        IStatus status = null;
        if (e instanceof CoreException)
        {
            status = ((CoreException) e).getStatus();
        }
        else
        {
            if (message == null)
                message = e.getMessage();
            if (message == null)
                message = e.toString();
            status = new Status(IStatus.ERROR, EJCorePlugin.ID, IStatus.OK, message, e);
        }
        ResourcesPlugin.getPlugin().getLog().log(status);
        Display display = EJCorePlugin.getStandardDisplay();
        final IStatus fstatus = status;
        display.asyncExec(new Runnable()
        {
            public void run()
            {
                ErrorDialog.openError(null, title, null, fstatus);
            }
        });
    }

    public static void logException(Throwable e)
    {
        logException(e, null, null);
    }

    public static void log(Throwable e)
    {
        if (e instanceof InvocationTargetException)
        {
            e = ((InvocationTargetException) e).getTargetException();
        }

        IStatus status = null;

        if (e instanceof CoreException)
        {
            status = ((CoreException) e).getStatus();
        }
        else
        {
            status = new Status(IStatus.ERROR, EJCorePlugin.ID, IStatus.OK, e.getMessage(), e);
        }

        log(status);
    }
}
