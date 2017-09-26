package store;

import java.nio.file.*;
import java.util.*;

public class Store {
	public static void main(String[] args) {
		System.out.println("Please input the file path: ");
		Scanner input = new Scanner(System.in);
		String filePath = null;
		
		while((filePath = input.next()) != null) {
			new StoreData(filePath).startStore();
		}
		input.close();
		System.out.println("Done storing files!");
	}
}
