package peer;

import java.util.*;
import java.net.*;
import java.io.*;

import dep.*;

public class AcceptSocketThread extends Thread{
	public AcceptSocketThread(Socket s) {
		server = s;
	}
	
	public void run () {
//		System.out.println("Start processing messages");
		String[] receivedMsg= new String[3];
		BufferedReader in = null;
		PrintWriter out = null;
		try {
			in = new BufferedReader(new InputStreamReader(server.getInputStream()));
			out = new PrintWriter(server.getOutputStream());
			
			for (int i = 0; i != receivedMsg.length; ++i) {
				receivedMsg[i] = in.readLine();
			}
		} catch (IOException e){
			e.printStackTrace();
		}
//		for(int i = 0; i != receivedMsg.length; ++i) {
//			System.out.println(receivedMsg.get(i));
//		}
		switch (receivedMsg[0]) {
			case "LOOKUP":
				Vector<String> lookupMsg = Peer.lookup(receivedMsg[1]);
				for (int i = 0; i != lookupMsg.size(); ++i) {
					out.println(lookupMsg.get(i));
				}
				break;
				
			case "UPDATEPRED":
				InfoEntry tmp = Peer.updatePred(receivedMsg[1], receivedMsg[2]);
				out.println(tmp.id);
				out.println(tmp.getNetAddr());
				break;
				
			default:
				break;
		}
		out.flush();

		//close socket
		try{
			server.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private Socket server;
}
