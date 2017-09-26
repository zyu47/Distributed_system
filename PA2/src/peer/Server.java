package peer;

import java.util.*;
import java.net.*;
import java.io.*;

import dep.*;

public class Server extends Thread{
	public Server (ServerSocket s) {
		serverSocket = s;
	}
	public void run (){
		Socket server = null;
		while(true){
			if (serverSocket.isClosed()) {
				break;
			}
	//		System.out.println("Waiting for client on port " + 
	//		        serverSocket.getLocalPort() + "...");
			try{
				server = serverSocket.accept();
			}catch(IOException e){
				if(e.getMessage().equals("Socket closed")){
//					System.out.println("Socket closed");
					break;
				} else {
					e.printStackTrace();
				}
			}
			new AcceptSocketThread(server).start();
		}
	}
	private ServerSocket serverSocket = null;
}

class AcceptSocketThread extends Thread{
	public AcceptSocketThread(Socket s) {
		server = s;
	}
	
	public void run () {
//		System.out.println("Start processing messages");
		String[] receivedMsg= {"", "", ""};
		InputStream in = null;
		PrintWriter out = null;
		
		byte[] byteArray = new byte[4096]; // Read byte from input
		int count = 0;
		int msgIndex = 0;
		int stoppedAt = 0;
		try {
			in = server.getInputStream();
			out = new PrintWriter(server.getOutputStream());
			
//			for (int i = 0; i != receivedMsg.length; ++i) {
//				receivedMsg[i] = in.readLine();
//			}
			
			// Parse input by byte
			while ((count = in.read(byteArray)) > 0) {
				int i = 0;
				for (; i != count; ++i) {
					if (msgIndex >= 3) {
						break;
					}
					if ((char) byteArray[i] == '\n') {
						++msgIndex;
						continue;
					}
					receivedMsg[msgIndex] += (char) byteArray[i];
				}
				if(msgIndex >= 3) {
					stoppedAt = i;
					break;
				}
			}
		} catch (IOException e){
			e.printStackTrace();
		}
//		
//		for(int i = 0; i != receivedMsg.length; ++i) {
//			System.out.print(receivedMsg[i] + "\t");
//		}
//		System.out.println("\n");
		
		switch (receivedMsg[0]) {
			case "LOOKUP":
				String[] lookupMsg = Peer.lookup(receivedMsg[1]);
				for (int i = 0; i != lookupMsg.length; ++i) {
					out.println(lookupMsg[i]);
				}
				break;
				
			case "UPDATEPRED":
				InfoEntry tmp = Peer.updatePred(receivedMsg[1], receivedMsg[2]);
				out.println(tmp.id);
				out.println(tmp.getNetAddr());
				out.println("");
				break;
				
			case "UPDATESUC":
				Peer.updateSuc(receivedMsg[1], receivedMsg[2]);
				out.println("");
				out.println("");
				out.println("");
				break;
				
			case "UPDATEPREDFTJOIN":
				Peer.updatePredFT(receivedMsg[1], receivedMsg[2], true);
				break;
				
			case "UPDATEPREDFTLEAVE":
				Peer.updatePredFT(receivedMsg[1], receivedMsg[2], false);
				break;
				
			case "UPDATEFTENTRYJOIN":
				Peer.updateFTEntry(receivedMsg[1], receivedMsg[2], true);
				break;
				
			case "UPDATEFTENTRYLEAVE":
				Peer.updateFTEntry(receivedMsg[1], receivedMsg[2], false);
				break;
				
			case "STORE":
				Peer.storeFile(receivedMsg[1], receivedMsg[2], in, byteArray, count, stoppedAt);
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
