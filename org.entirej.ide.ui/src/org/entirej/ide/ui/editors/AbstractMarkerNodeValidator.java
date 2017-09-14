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
package org.entirej.ide.ui.editors;

import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.entirej.ide.ui.nodes.NodeValidateProvider;

public abstract class AbstractMarkerNodeValidator implements NodeValidateProvider
{

    public static final int    NONE             = 0;
    private boolean error;
    private boolean warning;

    public void validate()
    {
        clear();
        List<IMarker> markers = getMarkers();
        for (IMarker marker : markers)
        {
            switch (marker.getAttribute(IMarker.SEVERITY, IMarker.SEVERITY_INFO))
            {
                case IMarker.SEVERITY_ERROR:
                    error = true;
                    return;
                case IMarker.SEVERITY_WARNING:
                    warning = true;
                    break;

            }
        }
    }

    public boolean hasErrors()
    {
        return error;
    }

    public boolean hasWarnings()
    {
        return warning;
    }

    protected void clear()
    {
        error = false;
        warning = false;
    }

    public abstract List<IMarker> getMarkers();

    public String getErrorMarkerMsg(List<IMarker> markers, Filter filter)
    {

        for (IMarker marker : markers)
        {
            int sev = marker.getAttribute(IMarker.SEVERITY, IMarker.SEVERITY_INFO);
            if (sev != IMarker.SEVERITY_ERROR)
                continue;
            int attribute = marker.getAttribute(NodeValidateProvider.NODE_TAG, NONE);
            if (filter.match(attribute, marker))
            {
                return marker.getAttribute(IMarker.MESSAGE, null);
            }
        }
        return null;
    }

    public String getWarningMarkerMsg(List<IMarker> markers, Filter filter)
    {

        for (IMarker marker : markers)
        {
            int sev = marker.getAttribute(IMarker.SEVERITY, IMarker.SEVERITY_INFO);
            if (sev != IMarker.SEVERITY_WARNING)
                continue;
            int attribute = marker.getAttribute(NodeValidateProvider.NODE_TAG, NONE);
            if (filter.match(attribute, marker))
            {
                return marker.getAttribute(IMarker.MESSAGE, null);
            }
        }
        return null;
    }

    public static interface Filter
    {
        boolean match(int tag, IMarker marker);
    }

}
