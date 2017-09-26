package test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.Vector;

import dep.*;

public class Test2 {
	public static void main(String[] args){
		byte[] byteArray = new byte[10];
		String[] receivedMsg= {"","",""};
		
		InputStream in = null;
		try {
			ServerSocket serverSocket = new ServerSocket(6666);
			Socket server = serverSocket.accept();
			in = server.getInputStream();
			
			int count = 0;
			int msgIndex = 0;
			int stoppedAt = 0;
			while ((count = in.read(byteArray)) > 0) {
				for (int i = 0; i != count; ++i) {
					if (msgIndex >= 3) {
						stoppedAt = i;
						break;
					}
					if ((char) byteArray[i] == '\n') {
						++msgIndex;
						continue;
					}
					receivedMsg[msgIndex] += (char) byteArray[i];
				}
				if(msgIndex >= 3) {
					break;
				}
			}
			System.out.println(receivedMsg[0]);

			System.out.println(receivedMsg[1]);

			System.out.println(receivedMsg[2]);
//			System.out.println(stoppedAt);
			for (int i = stoppedAt; i != count; ++i) {
				System.out.println((char) byteArray[i]);
			}
			while ((count = in.read(byteArray)) > 0) {
				for (int i = 0; i != count; ++i) {
					System.out.println((char) byteArray[i]);
				}
			}
			
		} catch (IOException e){
			e.printStackTrace();
		}
	}
	public static void test(){
	}
}
