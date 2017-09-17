package test;

import java.io.IOException;
import java.net.Socket;

public class Test2 {
	public static void main(String[] args){
		try{
			Socket client = new Socket("129.82.45.47", 6666);
		}catch(IOException e){
			e.printStackTrace();
		}
	}
}
