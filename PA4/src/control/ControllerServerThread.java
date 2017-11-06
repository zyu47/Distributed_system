package control;

import java.net.*;

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
				
			case "JOINED":
				ctrl.addServer(header[1], header[2]);
				break;
		}
	}
	
	private Controller ctrl;
}
