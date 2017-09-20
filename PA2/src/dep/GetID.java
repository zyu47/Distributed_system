package dep;

import java.io.UnsupportedEncodingException;
import java.security.*;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

public class GetID {
	/**
	 * This function generates a 16-bit hash value from a String
	 * @param s
	 * @return
	 */
	public static byte[] getID(String s){
		byte[] src = null;
		try{
			byte[] bytesOfMessage = s.getBytes("UTF-8");	
			MessageDigest md = MessageDigest.getInstance("MD5");
			src = md.digest(bytesOfMessage);
		} catch( UnsupportedEncodingException e) {
			e.printStackTrace();
	    } catch(NoSuchAlgorithmException ex){
	    	ex.printStackTrace();
	    }

		return Arrays.copyOfRange(src, 0, 2);
	}
	
	/**
	 * This function generates a 16-bit hash value from current time stamp
	 * @return
	 */
	public static byte[] getID(){
		String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());
		return getID(timeStamp);
	}
	
	/**
	 * Return the hexadecimal string of the hashed ID from a string
	 * @param s
	 * @return
	 */
	public static String getHexID(String s){
		return BytesHex.BytesToHex(getID(s));
	}
	
	/**
	 * Return the hexadecimal string of the hashed ID from current time stamp
	 * @return
	 */
	public static String getHexID(){
		return BytesHex.BytesToHex(getID());
	}
}
