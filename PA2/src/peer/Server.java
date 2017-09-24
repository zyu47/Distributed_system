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
					System.out.println("Socket closed");
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
				
			case "UPDATEPREDFT":
				Peer.updatePredFT(receivedMsg[1], receivedMsg[2]);
				break;
				
			case "UPDATEFTENTRY":
				Peer.updateFTEntry(receivedMsg[1], receivedMsg[2]);
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
