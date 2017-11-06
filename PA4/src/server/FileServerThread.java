package server;

import java.io.IOException;
import java.net.*;

import dep.*;

public class FileServerThread extends SocketServerThread{
	public FileServerThread (Socket s, FileServer f) {
		super(s);
		fs = f;
	}
	
	public void run() {
//		System.out.println("Testing FileServerThread received header: " + header[0]);
		
		switch (header[0]) {
			case "STORE":
//				System.out.println("Testing fileServer store");
				
				store();
				break;
				
			case "RETRIEVE":
//				System.out.println("Testing fileServer retrieve");
				break;
		}
	}
	
	private void store () {
		String[] chunkID_split = header[2].split("_");
		try {
			fs.storeChunkMeta(header[1], chunkID_split[0]);
			
			// if chunkID_split contains other addresses, we need to copy to other servers as well
			SocketClient sendNext = null;
			if (chunkID_split.length > 1) {
				HeaderMSG hmsg = new HeaderMSG("STORE", header[1], chunkID_split[0]);
				if (chunkID_split.length == 3) {
					hmsg.headerMsg[2] += chunkID_split[2];
				}
				sendNext = new SocketClient(chunkID_split[1],1);
				sendNext.trySendHeader(hmsg.headerMsg);
			}
			
			do {
				fs.storeChunk(header[1], chunkID_split[0], buffer, off, count);
				if (chunkID_split.length > 1) {
					sendNext.trySendContent(buffer, off, count);
				}
				off = 0; // Any following messages should have an offset of 0
			} while ((count = in.read(buffer)) > 0);
			
			sendNext.closeSocketClient();
			server.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private FileServer fs;
}
