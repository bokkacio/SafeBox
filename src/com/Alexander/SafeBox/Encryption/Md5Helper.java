package com.Alexander.SafeBox.Encryption;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public class Md5Helper {

    public byte[] GetMd5(String secretPhrase)
    {
        byte messageDigest[] = null;
        try
        {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            digest.update(secretPhrase.getBytes());
            messageDigest = digest.digest();
        }
        catch (NoSuchAlgorithmException e)
        {
            e.printStackTrace();
        }
        return  messageDigest;
    }

    public boolean CompareByteArrays(byte[] array1, byte[] array2)
    {
        if(array1 == null || array2 == null)
            return false;

        if(array1.length == 0 || array1.length != array2.length)
            return false;

        boolean result = true;

        for(int i = 0; i<array1.length; i++)
        {
            if(array1[i] != array2[i])
            {
                result = false;
                break;
            }
        }

        return result;
    }
}
