package control;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ChunkIDServerInfo {
	public Map<Integer, int[]> info = new ConcurrentHashMap<Integer, int[]>();
}
