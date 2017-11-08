package dep;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class SHA1 {
	public static byte[] hash (byte[] hashThis) {
		try {
			byte[] hash = new byte[20];
			MessageDigest md = MessageDigest.getInstance("SHA-1");
			
			hash = md.digest(hashThis);
			return hash;
		} catch (NoSuchAlgorithmException nsae) {
			System.err.println("SHA-1 algorithm is not available...");
	    }
		return null;
	}
	
	public static boolean checkHash (byte[] sha, byte[] content, int len) {
		byte[] content_tmp = Arrays.copyOf(content, len);
		byte[] sha_compare = hash(content_tmp);
		if (sha_compare != null && sha != null) {
			for (int i = 0; i != 20; ++i) {
				if (sha[i] != sha_compare[i])
					return false;
			}
			return true;
		} else {
			return false;
		}
	}
}
