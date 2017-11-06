package dep;

public class HeaderMSG {
	public HeaderMSG(String a, String b, String c) {
		headerMsg[0] = a;
		headerMsg[1] = b;
		headerMsg[2] = c;
	}
	public HeaderMSG(String a, String b) {
		this(a,b,"");
	}
	public HeaderMSG(String a) {
		this(a,"","");
	}
	public HeaderMSG (String a, String b, int c) {
		this(a, b);
		headerMsg[3] += c;
	}
	public HeaderMSG(String a, String b, long c) {
		this(a, b);
		headerMsg[2] = Long.toString(c);
	}
	public String[] headerMsg = {"", "", ""};
}
