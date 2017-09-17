package test;

import java.io.*;
import java.net.*;

public class TestDis {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try{
			Socket test = new Socket("129.82.45.47", 6666);
			PrintWriter p = new PrintWriter(test.getOutputStream());
			p.println("JOINED");
			p.println("0002");
			p.println("0.0.0.0:6666");
			p.flush();
		} catch (IOException e){
			e.printStackTrace();
		}
	}

}
