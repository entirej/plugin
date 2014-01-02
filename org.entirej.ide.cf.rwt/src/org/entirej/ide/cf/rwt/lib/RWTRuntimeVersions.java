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
package org.entirej.ide.cf.rwt.lib;

import org.eclipse.core.runtime.IPath;
import org.entirej.ide.cf.rwt.lib.rap.RWTRapRuntimeClasspathContainer;
import org.entirej.ide.cf.rwt.lib.rcp.RWTRcpRuntimeClasspathContainer;

public class RWTRuntimeVersions
{

    public static Version CF_V_2_0     = new Version("2.0", RWTCFRuntimeClasspathContainer.ID);
    public static Version CF_RAP_V_2_0 = new Version("2.0", RWTRapRuntimeClasspathContainer.ID);
    public static Version CF_RCP_V_2_0 = new Version("2.0", RWTRcpRuntimeClasspathContainer.ID);

    public static class Version
    {
        final String name;
        final IPath  path;

        public Version(String name, IPath path)
        {
            super();
            this.name = name;
            this.path = path;
        }

        public String getName()
        {
            return name;
        }

        public IPath getPath()
        {
            return path;
        }

    }
}
