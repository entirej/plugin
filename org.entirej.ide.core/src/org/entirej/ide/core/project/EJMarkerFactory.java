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
package org.entirej.ide.core.project;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

public class EJMarkerFactory
{
    public static final String MARKER_ID       = "org.entirej.ide.core.problem"; //$NON-NLS-1$

    public static final String CAT_ID          = "categoryId";                  //$NON-NLS-1$
    public static final String ID              = "id";                          //$NON-NLS-1$

    // problem categories
    public static final String CAT_FATAL       = "fatal";                       //$NON-NLS-1$
    public static final String CAT_DEPRECATION = "deprecation";                 //$NON-NLS-1$
    public static final String CAT_OTHER       = "";                            //$NON-NLS-1$

    public static IMarker createMarker(IResource resource, int id, String category) throws CoreException
    {
        IMarker marker = resource.createMarker(MARKER_ID);
        marker.setAttribute(ID, id);
        marker.setAttribute(CAT_ID, category);
        return marker;
    }

    public static IMarker createMarker(IResource resource) throws CoreException
    {
        IMarker marker = resource.createMarker(MARKER_ID);
        return marker;
    }

    public static IMarker addErrorMessage(IMarker marker, String msg) throws CoreException
    {
        marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
        marker.setAttribute(IMarker.MESSAGE, msg);
        return marker;
    }

    public static IMarker addWarningMessage(IMarker marker, String msg) throws CoreException
    {
        marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_WARNING);
        marker.setAttribute(IMarker.MESSAGE, msg);
        return marker;
    }

    public static IMarker addInfoMessage(IMarker marker, String msg) throws CoreException
    {
        marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_INFO);
        marker.setAttribute(IMarker.MESSAGE, msg);
        return marker;
    }

}
