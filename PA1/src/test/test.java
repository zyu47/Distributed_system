package test;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import dep.ReadSet;

public class test {
	public static void main (String[] args){
		initialize_map();
	}
	private static void initialize_map(){
		ReadSet rs = new ReadSet("/s/chopin/k/grad/zhixian/CS555/CS555PA1/bin/proc_set.txt");
		int server_cnt = rs.servers.size();
		for(int i = 0; i != server_cnt; ++i){
			Vector<BigInteger> x= new Vector<BigInteger>();
			for(int j = 0; j != 4; ++j){
				x.add(BigInteger.ZERO);
			}
			map.put(rs.servers.elementAt(i), x);
		}
		System.out.println(map);
		map.get("129.82.45.47").set(0, BigInteger.ONE);
		System.out.println(map);
	}
	// {ip_addr: [cnt_sent, cnt_received, val_sent, val_received]}
	private static Map<String, Vector<BigInteger> > map = new HashMap<String, Vector<BigInteger> >();
}
