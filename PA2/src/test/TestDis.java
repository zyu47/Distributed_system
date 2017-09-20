package test;

import java.io.*;
import java.net.*;
import java.util.*;

public class TestDis {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try{
			for(int i = 0; i != 5; ++i){
				Socket test = new Socket("129.82.45.47", 6666);
				PrintWriter p = new PrintWriter(test.getOutputStream());
				p.println("JOINED");
				p.println("000" + i);
				p.println("129.82.45.45:9999");
				p.flush();
				BufferedReader in = new BufferedReader(new InputStreamReader(test.getInputStream()));
				System.out.println(in.readLine());
				System.out.println(in.readLine());
				test.close();
			}
		} catch (IOException e){
			e.printStackTrace();
		}
	}

}
