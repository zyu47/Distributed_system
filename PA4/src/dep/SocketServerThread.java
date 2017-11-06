package dep;

import java.net.*;
import java.io.*;

public class SocketServerThread extends Thread {
	public Socket server;
	public InputStream in;
	public PrintWriter out;
	public byte[] buffer;
	public String[] header = {"", "", ""};
	public int off; // Beginning of actual data, end of header
	public int count;
	
	public SocketServerThread (Socket s){
		server = s;
		try {
			in = s.getInputStream();
			out = new PrintWriter(s.getOutputStream());
			readHeader();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void readHeader () throws IOException{
		buffer = new byte[8192]; // 8K buffer
		count = in.read(buffer);
		int msgIndex = 0;
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
	
	public void sendLine (String[] msg){
		for (int i = 0; i != msg.length; ++i) {
			out.println(msg[i]);
		}
		out.flush();
	}
	
	public void closeConnection() {
		try {
			server.close();
		} catch (IOException e) {
			
		}
	}
}
