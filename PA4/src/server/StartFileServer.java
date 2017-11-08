package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class StartFileServer {

	public static void main(String[] args) {
		// Get port number
		int port = Integer.parseInt(args[0]);
		FileServer fs = new FileServer(port);
		
		// Start server
		ServerSocket serversocket= null;
		try {
			serversocket = new ServerSocket(port);
		} catch (IOException e) {
			e.printStackTrace();
		}

		// Report to controller that this server joined
		fs.reportJoin();
		
		// Major heart beat thread
		new FileServerHeartBeat(fs).start();
		
		//debugging
//		new Debugging(fs).start();
		
		// Listen on serversocket, 
		Socket socket = null;
		while (true) {
			try {
				socket = serversocket.accept();
				new FileServerThread(socket, fs).start();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}

class Debugging extends Thread {
	public Debugging (FileServer f) {
		fs = f;
	}
	
	public void run () {

		while (true) {
			fs.debug();
			try{
				Thread.sleep(2000);
			} catch(InterruptedException ex){
				ex.printStackTrace();
			}
		}
	}
	
	private FileServer fs;
}