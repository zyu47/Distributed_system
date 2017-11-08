package dep;

public class IntByte {
	public static final byte[] intToByteArray(int value) {
	    return new byte[] {
	            (byte)(value >>> 24),
	            (byte)(value >>> 16),
	            (byte)(value >>> 8),
	            (byte)value};
	}
	
	public static int ByteToInt(byte[] bytes) {
		int ret = 0;
		for (int i=0; i<4; i++) {
			ret <<= 8;
			ret |= (int)bytes[i] & 0xFF;
		}
		return ret;
	}
}
