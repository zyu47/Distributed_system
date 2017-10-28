package store;

//import java.nio.file.*;
import java.util.*;

public class Store {
	public static void main(String[] args) {
		System.out.println("Please input the file path: ");
		Scanner input = new Scanner(System.in);
		String filePath = null;
		
		while((filePath = input.next()) != null) {
			String[] tmp = filePath.split("\\*");
			if (tmp.length == 1) {
				new StoreData(filePath).startStore();
			} else {
				new StoreData(tmp[0], tmp[1]).startStore();
			}
		}
		input.close();
		System.out.println("Done storing files!");
	}
}
