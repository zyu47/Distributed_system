package test;

import java.net.*;
import java.util.*;
import java.io.*;

public class TestServer {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
			ServerSocket ss = new ServerSocket(10000);
			Socket s = ss.accept();
			InputStream in = s.getInputStream();
//			BufferedReader in_line = new BufferedReader(new InputStreamReader(in));
//			System.out.println(in_line.readLine());
			byte[] byteArray = new byte[1024];
			int count = 0;
			while ((count = in.read(byteArray)) != -1) {
				for (int i = 0; i != count; ++i)
				{
					System.out.print((char) byteArray[i]);
				}
			}
			s.close();
			ss.close();
		} catch (IOException e) {
			
		}
	}

}
