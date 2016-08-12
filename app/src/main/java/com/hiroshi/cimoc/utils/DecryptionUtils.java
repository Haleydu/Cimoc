package com.hiroshi.cimoc.utils;

import android.util.Base64;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

import java.security.Key;

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

    public static String base64Decrypt(String cipherString) throws Exception {
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

}
