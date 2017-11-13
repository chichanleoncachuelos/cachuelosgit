package com.example.cachuelos.utils;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import org.apache.commons.codec.binary.Base64;


public class PasswordEncrypterSHABase64 {
	
	public static String encryptPassword(String password)
	{
	    byte[] enc;
	    String resul="";
	    
	    try
	    {
	        MessageDigest md = MessageDigest.getInstance("SHA-1");
	        md.reset();
	        md.update(password.getBytes("UTF-8"));
	        byte[] digest = md.digest();	        	       
	        enc = Base64.encodeBase64(digest);
	        resul = new String(enc);
	    }
	    catch(NoSuchAlgorithmException e)
	    {
	        e.printStackTrace();
	    }
	    catch(UnsupportedEncodingException e)
	    {
	        e.printStackTrace();
	    }
	    return resul;
	}

}
