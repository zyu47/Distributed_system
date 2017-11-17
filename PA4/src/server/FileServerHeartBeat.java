package server;

import java.net.Inet4Address;
import java.net.UnknownHostException;

public class FileServerHeartBeat extends Thread {
	public FileServerHeartBeat (FileServer f) {
		counter = 0;
		fs = f;
	}
	
	public void run () {
		while (true) {
			try{
				Thread.sleep(5000);
//				Thread.sleep(30000); // mnor heartbeat interval
				++counter;
			} catch(InterruptedException ex){
				ex.printStackTrace();
			}
			if (counter != 10) {
				System.out.println("Send Minor heart beat");
				fs.sendMinorHB();
			} else {
				System.out.println("Send Major heart beat");
				fs.sendMajorHB();
				counter = 0;
			}
//			try {
//				System.out.println(Inet4Address.getLocalHost().getHostAddress());
//			} catch (UnknownHostException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
		}
	}
	
	private FileServer fs;
	private int counter;
}
