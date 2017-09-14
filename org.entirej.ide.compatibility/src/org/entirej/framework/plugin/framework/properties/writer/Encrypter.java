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
package org.entirej.framework.plugin.framework.properties.writer;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

public class Encrypter
{
    private static final String pwd       = "?���Tn��%�8MV��NƼd~ƨW�+";
    private static String       algorithm = "DES";
    
    public static byte[] encrypt(String input) throws Exception
    {
        return encrypt(input.getBytes());
    }
    
    public static byte[] encrypt(byte[] input) throws Exception
    {
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
        DESKeySpec keySpec = new DESKeySpec(pwd.getBytes());
        SecretKey key = keyFactory.generateSecret(keySpec);
        Cipher cipher = Cipher.getInstance(algorithm);
        
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] inputBytes = input;
        return cipher.doFinal(inputBytes);
    }
    
    public static String decrypt(byte[] encryptionBytes) throws Exception
    {
        
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
        DESKeySpec keySpec = new DESKeySpec(pwd.getBytes());
        SecretKey key = keyFactory.generateSecret(keySpec);
        Cipher cipher = Cipher.getInstance(algorithm);
        cipher.init(Cipher.DECRYPT_MODE, key);
        
        byte[] recoveredBytes = cipher.doFinal(encryptionBytes);
        String recovered = new String(recoveredBytes);
        return recovered;
    }
    
    public static InputStream decrypt(InputStream in) throws Exception
    {
        
        StringBuffer buffer = new StringBuffer();
        
        byte[] buf = new byte[20480];
        
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
        DESKeySpec keySpec = new DESKeySpec(pwd.getBytes());
        SecretKey key = keyFactory.generateSecret(keySpec);
        Cipher dcipher = Cipher.getInstance(algorithm);
        dcipher.init(Cipher.DECRYPT_MODE, key);
        
        try
        {
            // Bytes read from in will be decrypted
            in = new CipherInputStream(in, dcipher);
            // Read in the decrypted bytes and write the cleartext to out
            int numRead = 0;
            while ((numRead = in.read(buf)) >= 0)
            {
                String str = new String(buf, 0, numRead);
                buffer.append(str);
            }
            
        }
        catch (java.io.IOException e)
        {
        }
        
        return new ByteArrayInputStream(buffer.toString().getBytes());
    }
    
}
