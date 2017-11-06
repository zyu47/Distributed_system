package dep;

public class NetAddr {
	public NetAddr (){
		host = null;
		port = 0;
	}
	public NetAddr(String h, int p){
		host = new String(h);
		port = p;
	}
	public NetAddr (NetAddr n){
		host = new String(n.host);
		port = n.port;
	}
	public NetAddr(String fullAddr){
		host = fullAddr.split(":")[0];
		port = Integer.parseInt(fullAddr.split(":")[1]);
	}
	public String getFullAddr(){
		return host + ":" + port;
	}
	public String host;
	public int port;
}
