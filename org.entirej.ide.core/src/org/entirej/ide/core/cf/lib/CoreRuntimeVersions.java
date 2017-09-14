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
package org.entirej.ide.core.cf.lib;

import org.eclipse.core.runtime.IPath;

public class CoreRuntimeVersions
{

    public static Version V_2_3 = new Version("4.0.0", CoreRuntimeClasspathContainerInitializer.ID);
    //public static Version V_1_0 = new Version("1.0", CoreRuntimeClasspathContainerInitializer.ID_V_1_0);

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
