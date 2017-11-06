package dep;

import java.io.*;
import java.util.Vector;

public class ReadControllerAddr {

	public static NetAddr getControllerAddr(){
		try{
			// Open file
			InputStream infile = new FileInputStream("/s/chopin/k/grad/zhixian/CS555/PA4/controller.txt");
			// Get the object of DataInputStream
			DataInputStream in = new DataInputStream(infile);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
//			String line = br.readLine();
			//Read file line by line
			NetAddr controller = new NetAddr(br.readLine());
			infile.close();
			return controller;
		}catch(IOException e){
			System.out.println("Exception in reading controller address");
			return null;
		}
	}
}
