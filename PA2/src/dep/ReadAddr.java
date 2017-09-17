package dep;
//import java.net.*;
import java.util.*;
import java.io.*;

public class ReadAddr{
	public ReadAddr(String fileAddr){
		try{
			// Open file
			InputStream infile = new FileInputStream(fileAddr);
			// Get the object of DataInputStream
			DataInputStream in = new DataInputStream(infile);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String line;
			//Read file line by line
			while((line = br.readLine()) != null){
				servers.addElement(line.split(":", 0)[0]);
				ports.addElement(Integer.parseInt(line.split(":", 0)[1]));
			}
			infile.close();
		}catch(IOException e){
			System.out.println("Exception in reading proc_set.txt");
		}
	}
	
	public Vector<String> servers = new Vector<String>();
	public Vector<Integer> ports = new Vector<Integer>();
}