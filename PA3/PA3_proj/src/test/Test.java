package test;

import java.io.*;
import java.util.*;

public class Test {
	public static void main(String[] args) {
		Map<String, Integer> m = new HashMap<String, Integer>();

		String x = "abc";
		String y = "abc";
		m.put(x, 1);
		System.out.println(m.containsKey(y));
		System.out.println(m.get(y));
	}
	private static int findMin (int[] top)
	{
		int minInd = 0;
		int minVal = top[0];
		for (int i = 0; i != top.length; ++i) {
			if (top[i] < minVal) {
				minInd = i;
				minVal = top[i];
			}
		}
		return minInd;
	}
}


class testObj {
	public testObj()
	{
		i = 9;
	}
	public int i = 0;
}