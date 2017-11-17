package control;

import java.net.*;
import java.util.Vector;

import dep.*;

public class ControllerServerThread extends SocketServerThread{

	public ControllerServerThread(Socket s, Controller c) {
		super(s);
		ctrl = c;
	}
	
	public void run () {
//		System.out.println("Testing received header: " + header[0]);
		
		String[] servers = null;
		switch (header[0]) {
			case "STORE":
//				System.out.println("Testing controller store");
				servers = ctrl.getThreeServers();
				sendLine(servers);
				closeConnection();
				break;
				
			case "RETRIEVE":
//				System.out.println("Testing controller retrieve");
				servers = ctrl.getStoringServers(header[1]);
				sendLine(servers);
				closeConnection();
				break;
				
			case "RETRIEVE_CHUNK":
			case "FIX":
				servers = ctrl.getChunkServers(header[1], header[2]);
				sendLine(servers);
				closeConnection();
				break;
				
			case "JOINED":
				ctrl.addServer(header[1], header[2]);
				break;
				
			case "STORED":
//				System.out.println("Stored " + header[1] + header[2]);
				String[] fileName_split = header[1].split("\\*"); // fileName, chunkID
				String[] addr_split = header[2].split("\\*"); // Address, freeSpace
//				System.out.println(header[1] + " from " + header[2]);
				ctrl.updateAll(fileName_split[0], fileName_split[1],
						addr_split[0], addr_split[1]);
				break;
				
			case "MAHEARTBEAT":
				Vector<String> msg = readAllLine();
				ctrl.processMajorHB(header[1], header[2], msg);
				System.out.println("Major heart beat processed from " + header[1]);
				break;
				
			case "MIHEARTBEAT":
				if (ctrl.processMinorHB(header[1], header[2])) {
					sendOneLine("SENDMAJORHEARTBEAT");
					System.out.println("Minor heart beat - with major" + header[1]);
				} else {
					sendOneLine("");
//					System.out.println("Minor heart beat - no major" + header[1]);
				}
				break;
				
		}
	}
	
	private Controller ctrl;
}
