package dep;

import java.net.*;
import java.io.*;

/**
 * This class is used for connecting to a server and send data
 * @author zhixian
 *
 */
public class SocketClient {
	private InputStream in;
	private OutputStream out;
	private BufferedReader read_in;
	private Socket client;
	private NetAddr addr; // server address
	private int times; // times of trying connecting to server
	
	//public byte[] recByte; // bytes received from the server
	
	public SocketClient (NetAddr a, int t) {
		addr = a;
		times = t;
		trySocketClient();
	}	
	
	public SocketClient (String fulladdr, int t) {
		this(new NetAddr(fulladdr), t);
	}
	
	/**
	 * 
	 * @param times		How many times to try connecting, -1 means forever
	 * @return			<code>true</code> if connect successfully, otherwise <code>false</code>
	 */
	private void trySocketClient () {
//		System.out.println("Testing SocketClient trySocketClient ...");
		int tried = 0;
		while (tried != times) {
			try {
				client = new Socket(addr.host, addr.port);
//				System.out.println("Test connecting " + addr.getFullAddr());
				in = client.getInputStream();
				out = client.getOutputStream();
				read_in = new BufferedReader(new InputStreamReader(in));
				break; // break after successful connection
			} catch (IOException e) {
				System.out.println("Server not available.");
				++tried;
				if (tried != times) {
					System.out.println("Retrying: " + tried + " times...");
					try{
						Thread.sleep(2000);
					} catch(InterruptedException ex){
						ex.printStackTrace();
					}
				}
			}
		}
		if (client == null)
			System.out.println("Cannot connect to: " + addr.getFullAddr());
		else
			System.out.println("Successfully connected to: " + addr.getFullAddr());
	}
	
//	public boolean isConnected () {
//		return client != null && client.isConnected();
//	}
	
	/**
	 * 
	 * @param header	[commands, filename, chunkNo]
	 * 					3 and only 3 elements, filename and chunkNo can be empty
	 * @param content	Actual content to transfer, either file chunk or metadata
	 * @return			<code>true</code> if sending successfully, otherwise <code>false</code>
	 */
	public void trySendHeader (String[] header) throws IOException{
		for (int i = 0; i != header.length; ++i) {
			byte[] infoSent = (header[i] + "\n").getBytes();
			out.write(infoSent);
		}
		out.flush();
	}
	
	public void trySendContent (byte[] content) throws IOException{
		out.write(content);
		out.flush();
	}
	
	public void trySendContent (byte[] content, int off, int len) throws IOException {
		out.write(content, off, len);
		out.flush();
	}
	
	public void trySendContent (byte[] content, int len) throws IOException {
		trySendContent(content, 0, len);
	}
	
	/**
	 * 
	 * @return		number of bytes actually read into recByte 
	 */
	public int tryGetByte (byte[] recByte) throws IOException{
		return in.read(recByte);
	}
	
	public String tryGetString () throws IOException{
		return read_in.readLine();
	}
	
	public void closeSocketClient () {
		try {
			client.close();
		} catch (IOException e) {
			//e.printStackTrace();
		}
	}
}
