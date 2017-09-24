package dep;

public class InfoEntry {
	public InfoEntry(){
		id = null;
		addr = new NetAddr();
	}
	public InfoEntry(String i, NetAddr a){
		id = new String(i);
		addr = new NetAddr(a);
	}
	public InfoEntry(String i, String fulladdr){
		id = new String(i);
		addr = new NetAddr(fulladdr);
	}
	public InfoEntry(InfoEntry i){
		id = new String(i.id);
		addr = new NetAddr(i.addr);
	}
	public InfoEntry(String fullInfo) {
		id = fullInfo.split("\\s")[0];
		addr = new NetAddr(fullInfo.split("\\s")[1]);
	}
	public String getFullFTEntry(){
		return id + '\t' + addr.getFullAddr();
	}
	public String getNetAddr(){
		return addr.getFullAddr();
	}
	public String id; // ID is stored as a hex number string (4-digit)
	public NetAddr addr;
}
