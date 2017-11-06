package test;

import java.io.*;
import java.net.*;

public class TestClient {

	public static void main(String[] args) {
		try{
			byte[] chararray = {'a','b','c','1','3','d','2','g'};
			Socket client = new Socket("129.82.45.44", 10000);
			OutputStream out = client.getOutputStream();
			for (int i = 0; i != args.length; ++i) {
				byte[] infoSent = (args[i] + "\n").getBytes();
				out.write(infoSent);
				out.flush();

				try{
					Thread.sleep(2000);
				} catch(InterruptedException ex){
					ex.printStackTrace();
				}
			}
//			out.flush();
			out.write(chararray);
			out.flush();
			client.close();
		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}

}
