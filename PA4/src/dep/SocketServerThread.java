package dep;

import java.net.*;
import java.util.Vector;
import java.io.*;

public class SocketServerThread extends Thread {
	public Socket server;
	public InputStream in;
	public OutputStream out;
	public PrintWriter printOut;
	public byte[] buffer;
	public String[] header = {"", "", ""};
	public int off; // Beginning of actual data, end of header
	public int count;
	
	public SocketServerThread (Socket s){
		server = s;
		try {
			in = s.getInputStream();
			out = s.getOutputStream();
			printOut = new PrintWriter(s.getOutputStream());
			readHeader();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void readHeader () throws IOException{
		buffer = new byte[8192]; // 8K buffer
		int msgIndex = 0;
		while (msgIndex < 3) { // read until the 3 lines in the header are complete
			count = in.read(buffer);
			for (off = 0; off != count; ++off) {
	//			System.out.println("Received bytes: " + (char) buffer[off]);
				if (msgIndex >= 3) {
					break;
				}
				if ((char) buffer[off] == '\n') {
					++msgIndex;
					continue;
				}
				header[msgIndex] += (char) buffer[off];
			}
		}
	} 
	
	public Vector<String> readAllLine() {
		// First read what is left in buffer
		Vector<String> res = new Vector<String>();
		String tmp = "";
		try {
			do {
				for (; off != count; ++off) {
		//			System.out.println("Received bytes: " + (char) buffer[off]);
					if ((char) buffer[off] == '\n') {
						res.add(new String(tmp));
						tmp = "";
						continue;
					}
					tmp += (char) buffer[off];
				}
				off = 0;
			} while ((count = in.read(buffer)) > 0);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return res;
	}
	
	public void sendOneLine (String s) {
		String[] msg = {s};
		sendLine(msg);
	}
	
	public void sendLine (String[] msg){
		for (int i = 0; i != msg.length; ++i) {
			printOut.println(msg[i]);
		}
		printOut.flush();
	}
	
	public void sendByte () throws IOException{
		out.write(buffer, 0, count);
		out.flush();
	}
	
	public void sendByte (byte[] b) throws IOException{
		out.write(b);
		out.flush();
	}
	
	public void closeConnection() {
		try {
			server.close();
//			System.out.println(CurrentTimeStamp.getFullTimeStamp() + "SocketServerThread closed!");
		} catch (IOException e) {
			
		}
	}
}
