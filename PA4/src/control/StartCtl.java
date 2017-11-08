package control;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import dep.ReadControllerAddr;

public class StartCtl {

	public static void main (String[] args) {
		Controller ctrlObj = null;
		if (args.length >= 1 && args[0].equals("r")) 
			ctrlObj = new Controller(true);
		else 
			ctrlObj = new Controller(false);
		
		int ctrlPort= ReadControllerAddr.getControllerAddr().port;
		ServerSocket serversocket= null;
		Socket socket = null;
		try {
			serversocket = new ServerSocket(ctrlPort);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		new DebugCtrl(ctrlObj).start(); // For printing out information for debugging
		
		while (true) {
			try {
				socket = serversocket.accept();
				new ControllerServerThread(socket, ctrlObj).start();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
}

class DebugCtrl extends Thread{
	private Controller c;
	public DebugCtrl (Controller cc) {
		c = cc;
	}
	
	public void run () {
		while (true) {
			c.printDebugInfo();
			try{
				Thread.sleep(2000);
			} catch(InterruptedException ex){
				ex.printStackTrace();
			}
		}
	}
}



