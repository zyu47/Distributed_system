package peer;

import java.util.*;
import java.io.*;
import java.net.*;
import java.nio.file.*;

import dep.NetAddr;
import dep.ReadAddr;

public class Talk {
	public static String[] talkPeer(String h, int p, String[] msg){
		TalkBase t = new TalkBase(h, p);
		t.iniSendingMsg(msg);
		if(t.contactPeer()){
			return t.getReceivedMsg();
		} else {
			return null;
		}
	}

	public static String[] talkPeer(NetAddr n, String[] msg){
		TalkBase t = new TalkBase(n);
		t.iniSendingMsg(msg);
		if(t.contactPeer()){
			return t.getReceivedMsg();
		} else {
			return null;
		}
	}

	public static String[] talkDis(String[] msg){
		TalkBase t = new TalkBase();
		t.iniSendingMsg(msg);
		if(t.contactDis()){
			return t.getReceivedMsg();
		} else {
			return null;
		}
	}
	
	public static void storeAt(String fileID, String filePath, String fullAddr) {
		// Construct header messages
		Path path = Paths.get(filePath);
		String header = "STORE\n" + fileID + "\n" + path.getFileName().toString() + "\n";
		
		// Stream reading from file 
		File file = new File(path.toString());        
        //long length = file.length(); // Get the size of the file
        byte[] bytes = new byte[4096];
        InputStream fileIn = null;        
		
        // Start socket and send file by byte
		NetAddr target = new NetAddr(fullAddr);
		Socket client = null;
		OutputStream socketOut = null;
		
		try {
			client = new Socket(target.host, target.port);
			fileIn = new FileInputStream(file);
			socketOut = client.getOutputStream();
			
			// First write the header containing the command and file name
			byte[] tmp = header.getBytes();
			for (int i = 0; i != tmp.length; ++i) {
				socketOut.write(tmp[i]);
			}
			
			// File is following the header
	        int count;
	        while ((count = fileIn.read(bytes)) > 0) {
	            socketOut.write(bytes, 0, count);
	        }
	        fileIn.close();
	        socketOut.close();
	        client.close();
		} catch(IOException e){
			e.printStackTrace();
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
	
	public String[] getReceivedMsg(){
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
			for (int i = 0; i != received.length; ++i) {
				received[i] = in.readLine();
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
	private String[] received = new String[3];
	private NetAddr addr;
	private Socket client = null;
}