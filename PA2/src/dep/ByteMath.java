package dep;

public class ByteMath {
	/**
	 * This function increases the hex string by a specified amount, and take the modulus 
	 * @param id
	 * @param inc
	 * @return
	 */
	public static String add(String id, int inc){
		int ori_val = Integer.parseInt(id, 16);
		int new_val = (ori_val + inc) % (int)Math.pow(2, 16);
		String tmp = Integer.toString(new_val, 16);
		return ("0000" + tmp).substring(tmp.length());
	}
}
