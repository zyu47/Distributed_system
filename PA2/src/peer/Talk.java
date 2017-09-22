package peer;

import java.util.*;
import java.io.*;
import java.net.*;

import dep.NetAddr;
import dep.ReadAddr;

public class Talk {
	public static Vector<String> talkPeer(String h, int p, String[] msg){
		TalkBase t = new TalkBase(h, p);
		t.iniSendingMsg(msg);
		if(t.contactPeer()){
			return t.getReceivedMsg();
		} else {
			return null;
		}
	}
	public static Vector<String> talkPeer(NetAddr n, String[] msg){
		TalkBase t = new TalkBase(n);
		t.iniSendingMsg(msg);
		if(t.contactPeer()){
			return t.getReceivedMsg();
		} else {
			return null;
		}
	}
	
	public static Vector<String> talkDis(String[] msg){
		TalkBase t = new TalkBase();
		t.iniSendingMsg(msg);
		if(t.contactDis()){
			return t.getReceivedMsg();
		} else {
			return null;
		}
	}
}

class TalkBase{
	public TalkBase(){
		addr = null;
	}
	public TalkBase(String h, int p){
		addr = new NetAddr(h, p);
	}
	public TalkBase(NetAddr n){
		addr = new NetAddr(n);
	}
	public void iniSendingMsg(String[] sn){
		sending = sn;
	}
	
	public Vector<String> getReceivedMsg(){
		return received;
	}
	
	public Boolean contactPeer(){
		return contactBase(false);
	}
	
	public Boolean contactDis(){
		String discoveryAddrText = "/s/chopin/k/grad/zhixian/CS555/PA2/discovery_addr.txt";
		ReadAddr rs = new ReadAddr(discoveryAddrText);
		addr = rs.servers.get(0);
		
		return contactBase(true);
	}
	
	private Boolean contactBase(Boolean wait){
		int tried_cnt = 0;
		PrintWriter out = null;
		BufferedReader in = null;
		
		// Trying to connect to the discovery node
		while (client == null && tried_cnt != 10)
		{
			++tried_cnt;
			try{
				client = new Socket(addr.host, addr.port);
				out = new PrintWriter(client.getOutputStream());
				in = new BufferedReader(new InputStreamReader(client.getInputStream()));
			} catch(IOException e){
				if(wait){
					System.out.println("Node " + addr.getFullAddr() +
							" is not available now. Retry in 2 seconds");
					client = null;
					try{
						Thread.sleep(2000);
					} catch(InterruptedException ex){
						ex.printStackTrace();
					}
				} else {
					return false; // Connection failed
				}
			}
		}
		if(tried_cnt == 10){
			return false; // Connection failed
		}

		// Send message to Node
		for (int i = 0; i != sending.length; ++i){
			out.println(sending[i]);
		}
		out.flush();
		
//		System.out.println("Message sent");
		
		// Get message from Node and close connection
		try{
			String tmp = "";
			while ((tmp = in.readLine()) != null){
				received.add(tmp);
			}
		} catch (IOException e){
			e.printStackTrace();
			return false;
		}
		
		// Try closing the connection
		try{
			client.close();
		}catch (IOException e){
			e.printStackTrace();
		}
		
		return true;
	}
	
	private String[] sending;
	private Vector<String> received = new Vector<String>();
	private NetAddr addr;
	private Socket client = null;
}