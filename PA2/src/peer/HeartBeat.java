package peer;

import java.net.ServerSocket;

public class HeartBeat extends Thread {
	public HeartBeat (int i, ServerSocket s) {
		interval = i;
		serverSocket = s;
	}
	
	public void run() {
		while (true) {
			if (serverSocket.isClosed()) {
				break;
			}
//			Peer.stabilize();
//			Peer.fix_fingers();
			
			try{
				Thread.sleep(interval);
			} catch(InterruptedException e){
				e.printStackTrace();
			}
		}
	}
	private int interval = 2000;
	private ServerSocket serverSocket;
}
