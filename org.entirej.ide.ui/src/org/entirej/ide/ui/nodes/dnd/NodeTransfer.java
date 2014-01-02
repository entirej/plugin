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
package org.entirej.ide.ui.nodes.dnd;

import org.eclipse.swt.dnd.ByteArrayTransfer;
import org.eclipse.swt.dnd.TransferData;

public class NodeTransfer extends ByteArrayTransfer
{

    protected static final String     TYPE_NAME = "apd-node-transfer-format";
    protected static final int        TYPE_ID   = registerType(TYPE_NAME);

    private final static NodeTransfer instance  = new NodeTransfer();

    public static NodeTransfer getInstance()
    {
        return instance;
    }

    protected long   startTime;

    protected Object object;

    protected NodeTransfer()
    {
        super();
    }

    protected int[] getTypeIds()
    {
        return new int[] { TYPE_ID };
    }

    public String[] getTypeNames()
    {
        return new String[] { TYPE_NAME };
    }

    public void javaToNative(Object object, TransferData transferData)
    {
        startTime = System.currentTimeMillis();
        this.object = object;
        if (transferData != null)
        {
            super.javaToNative(String.valueOf(startTime).getBytes(), transferData);
        }
    }

    public Object nativeToJava(TransferData transferData)
    {
        byte[] bytes = (byte[]) super.nativeToJava(transferData);
        // native transfer fail to load get data use local type (see #135)
        if (bytes == null)
            return object;

        try
        {
            long startTime = Long.parseLong(new String(bytes));
            return this.startTime == startTime ? object : null;
        }
        catch (NumberFormatException exception)
        {
            return null;
        }
    }
}
