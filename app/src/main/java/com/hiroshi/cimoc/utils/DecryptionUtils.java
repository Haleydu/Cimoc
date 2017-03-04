package com.hiroshi.cimoc.utils;

import android.util.Base64;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.security.Key;
import java.util.ArrayList;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

/**
 * Created by Hiroshi on 2016/7/8.
 */
public class DecryptionUtils {

    public static String desDecrypt(String keyString, String cipherString) throws Exception {
        byte[] cipherBytes = Base64.decode(cipherString, Base64.DEFAULT);
        DESKeySpec keySpec = new DESKeySpec(keyString.getBytes());
        Key key = SecretKeyFactory.getInstance("DES").generateSecret(keySpec);
        Cipher cipher = Cipher.getInstance("DES");
        cipher.init(Cipher.DECRYPT_MODE, key);
        byte[] result = cipher.doFinal(cipherBytes);
        return new String(result, "UTF-8");
    }

    public static String base64Decrypt(String cipherString) throws UnsupportedEncodingException {
        byte[] cipherBytes = Base64.decode(cipherString, Base64.DEFAULT);
        return new String(cipherBytes, "UTF-8");
    }

    public static String evalDecrypt(String jsCode) {
        Context rhino = Context.enter();
        rhino.setOptimizationLevel(-1);
        Scriptable scope = rhino.initStandardObjects();
        Object object = rhino.evaluateString(scope, jsCode, null, 1, null);
        return Context.toString(object);
    }

    public static String urlDecrypt(String str) {
        try {
            return URLDecoder.decode(str, "utf-8");
        } catch (Exception e) {
            return str;
        }
    }

    /**
     * https://github.com/tommyettinger/BlazingChain
     */

    public static String LZ64Decrypt(String str) {
        final char[] valStrBase64 = new char[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                62, 0, 0, 0, 63, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 0, 0, 0, 64, 0, 0, 0, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25,
                0, 0, 0, 0, 0, 0, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51};
        if (str == null) {
            return null;
        }
        if (str.isEmpty()) {
            return "";
        }
        final char[] input = str.toCharArray();
        return LZ64Decrypt(input.length, 32, input, valStrBase64, 0);
    }

    private static String LZ64Decrypt(int length, int resetValue, char[] getNextValue, char[] modify, int offset) {
        ArrayList<String> dictionary = new ArrayList<>();
        int enlargeIn = 4, dictSize = 4, numBits = 3, position = resetValue, index = 1, resb, maxpower, power;
        String entry, w, c;
        ArrayList<String> result = new ArrayList<>();
        char bits, val = (modify == null) ? (char) (getNextValue[0] + offset) : modify[getNextValue[0]];

        for (char i = 0; i < 3; i++) {
            dictionary.add(i, String.valueOf(i));
        }

        bits = 0;
        maxpower = 2;
        power = 0;
        while (power != maxpower) {
            resb = val & position;
            position >>= 1;
            if (position == 0) {
                position = resetValue;
                val = (modify == null) ? (char) (getNextValue[index++] + offset) : modify[getNextValue[index++]];
            }
            bits |= (resb > 0 ? 1 : 0) << power++;
        }

        switch (bits) {
            case 0:
                bits = 0;
                maxpower = 8;
                power = 0;
                while (power != maxpower) {
                    resb = val & position;
                    position >>= 1;
                    if (position == 0) {
                        position = resetValue;
                        val = (modify == null) ? (char) (getNextValue[index++] + offset) : modify[getNextValue[index++]];
                    }
                    bits |= (resb > 0 ? 1 : 0) << power++;
                }
                c = String.valueOf(bits);
                break;
            case 1:
                bits = 0;
                maxpower = 16;
                power = 0;
                while (power != maxpower) {
                    resb = val & position;
                    position >>= 1;
                    if (position == 0) {
                        position = resetValue;
                        val = (modify == null) ? (char) (getNextValue[index++] + offset) : modify[getNextValue[index++]];
                    }
                    bits |= (resb > 0 ? 1 : 0) << power++;
                }
                c = String.valueOf(bits);
                break;
            default:
                return "";
        }
        dictionary.add(c);
        w = c;
        result.add(w);
        while (true) {
            if (index > length) {
                return "";
            }

            bits = 0;
            maxpower = numBits;
            power = 0;
            while (power != maxpower) {
                resb = val & position;
                position >>= 1;
                if (position == 0) {
                    position = resetValue;
                    val = (modify == null) ? (char) (getNextValue[index++] + offset) : modify[getNextValue[index++]];
                }
                bits |= (resb > 0 ? 1 : 0) << power++;
            }
            int cc;
            switch (cc = bits) {
                case 0:
                    bits = 0;
                    maxpower = 8;
                    power = 0;
                    while (power != maxpower) {
                        resb = val & position;
                        position >>= 1;
                        if (position == 0) {
                            position = resetValue;
                            val = (modify == null) ? (char) (getNextValue[index++] + offset) : modify[getNextValue[index++]];
                        }
                        bits |= (resb > 0 ? 1 : 0) << power++;
                    }

                    dictionary.add(String.valueOf(bits));
                    cc = dictSize++;
                    enlargeIn--;
                    break;
                case 1:
                    bits = 0;
                    maxpower = 16;
                    power = 0;
                    while (power != maxpower) {
                        resb = val & position;
                        position >>= 1;
                        if (position == 0) {
                            position = resetValue;
                            val = (modify == null) ? (char) (getNextValue[index++] + offset) : modify[getNextValue[index++]];
                        }
                        bits |= (resb > 0 ? 1 : 0) << power++;
                    }
                    dictionary.add(String.valueOf(bits));
                    cc = dictSize++;
                    enlargeIn--;
                    break;
                case 2:
                    StringBuilder sb = new StringBuilder(result.size());
                    for (String s : result)
                        sb.append(s);
                    return sb.toString();
            }

            if (enlargeIn == 0) {
                enlargeIn = 1 << numBits;
                numBits++;
            }

            if (cc < dictionary.size() && dictionary.get(cc) != null) {
                entry = dictionary.get(cc);
            } else {
                if (cc == dictSize) {
                    entry = w + w.charAt(0);
                } else {
                    return "";
                }
            }
            result.add(entry);

            // Add w+entry[0] to the dictionary.
            dictionary.add(w + entry.charAt(0));
            dictSize++;
            enlargeIn--;

            w = entry;

            if (enlargeIn == 0) {
                enlargeIn = 1 << numBits;
                numBits++;
            }
        }
    }

}
