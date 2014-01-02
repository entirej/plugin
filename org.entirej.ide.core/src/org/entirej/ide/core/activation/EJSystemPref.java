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
package org.entirej.ide.core.activation;

import java.security.NoSuchAlgorithmException;
import java.util.prefs.Preferences;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

import org.entirej.ide.core.EJCoreLog;

public class EJSystemPref
{
    private EJSystemPref()
    {
        throw new AssertionError();
    }

    private static final Preferences _PREF  = Preferences.userRoot().node("/org/entirej/ide/system/v_1");

    private static final byte[]      ___KEY = (new byte[] { 69, 74, 67, 79, 82, 69, 45, 35, 45, 115, 121, 115, 45, 49, 45, 48, 45, 48 });

    public static String get(String key, String def)
    {
        return _PREF.get(key, def);
    }

    public boolean getBoolean(String key, boolean def)
    {
        return _PREF.getBoolean(key, def);
    }

    public static byte[] getByteArray(String key, byte[] def)
    {
        return _PREF.getByteArray(key, def);
    }

    public static double getDouble(String key, double def)
    {
        return _PREF.getDouble(key, def);
    }

    public static float getFloat(String key, float def)
    {
        return _PREF.getFloat(key, def);
    }

    public static int getInt(String key, int def)
    {
        return _PREF.getInt(key, def);
    }

    public static long getLong(String key, long def)
    {
        return _PREF.getLong(key, def);
    }

    public static void put(String key, String value)
    {
        _PREF.put(key, value);
    }

    public void putBoolean(String key, boolean value)
    {
        _PREF.putBoolean(key, value);
    }

    public static void putByteArray(String key, byte[] value)
    {
        _PREF.putByteArray(key, value);
    }

    public static void putDouble(String key, double value)
    {
        _PREF.putDouble(key, value);
    }

    public void putFloat(String key, float value)
    {
        _PREF.putFloat(key, value);
    }

    public static void putInt(String key, int value)
    {
        _PREF.putInt(key, value);
    }

    public static void putLong(String key, long value)
    {
        _PREF.putLong(key, value);
    }

    public static void remove(String key)
    {
        _PREF.remove(key);
    }

    public static String getDecrypt(String key, String def)
    {
        byte[] decrypt = _PREF.getByteArray(key, new byte[0]);

        if (KeyEnc.checkBytes(decrypt))
        {
            decrypt = KeyEnc.decrypt(___KEY, decrypt);
        }
        return KeyEnc.checkBytes(decrypt) ? new String(decrypt) : def;
    }

    public static void putEncrypt(String key, String value)
    {
        if (value == null || value.length() == 0)
        {
            _PREF.putByteArray(key, new byte[0]);
        }
        else
        {

            byte[] encrypt = KeyEnc.encrypt(___KEY, value);
            _PREF.putByteArray(key, encrypt);
        }

    }

    private static class KeyEnc
    {
        private static final String ALGORITHEM_DES = "DES";

        private static final String SALT           = new String(new byte[] { 0x01, 0x23, 0x45, 0x67 });
        private static Cipher       cipher         = null;

        // get an instance of cipher
        static
        {
            try
            {
                cipher = Cipher.getInstance(ALGORITHEM_DES);
            }
            catch (NoSuchAlgorithmException ex)
            {
                EJCoreLog.logException(ex);
            }
            catch (NoSuchPaddingException ex)
            {
                EJCoreLog.logException(ex);
            }
        }

        private KeyEnc()
        {
            throw new AssertionError();
        }

        public static byte[] encrypt(final byte[] key, final String rawString)
        {
            try
            {
                StringBuffer buffer = new StringBuffer(SALT);
                buffer.append(new String(key));
                DESKeySpec secretKeySpec = new DESKeySpec(getKeyBytes(buffer.toString()));
                SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(ALGORITHEM_DES);
                cipher.init(Cipher.ENCRYPT_MODE, keyFactory.generateSecret(secretKeySpec));

                return cipher.doFinal(rawString.getBytes());
            }
            catch (Exception ex)
            {
                EJCoreLog.log(ex);
            }
            return new byte[0];
        }

        private static byte[] getKeyBytes(String key)
        {
            byte[] keyBytes = key.getBytes();
            return keyBytes;
        }

        public static byte[] decrypt(final byte[] key, final byte[] encryptionBytes)
        {

            if (checkBytes(encryptionBytes))
            {
                try
                {
                    StringBuffer buffer = new StringBuffer(SALT);
                    buffer.append(new String(key));
                    DESKeySpec secretKeySpec = new DESKeySpec(getKeyBytes(buffer.toString()));
                    SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(ALGORITHEM_DES);
                    cipher.init(Cipher.DECRYPT_MODE, keyFactory.generateSecret(secretKeySpec));
                    byte[] decryptedBytes = cipher.doFinal(encryptionBytes);
                    return decryptedBytes;
                }
                catch (Exception ex)
                {
                    EJCoreLog.log(ex);
                }
            }
            return new byte[0];
        }

        public static boolean checkBytes(byte[] bytes)
        {
            return (bytes != null && bytes.length > 0) ? true : false;
        }
    }

}
