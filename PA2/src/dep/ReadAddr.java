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
				NetAddr n_tmp = new NetAddr(line.split(":", 0)[0],
						Integer.parseInt(line.split(":", 0)[1]));
				servers.add(n_tmp);
			}
			infile.close();
		}catch(IOException e){
			System.out.println("Exception in reading proc_set.txt");
		}
	}
	
	public Vector<NetAddr> servers = new Vector<NetAddr>();
}