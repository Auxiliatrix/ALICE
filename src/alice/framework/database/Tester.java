package alice.framework.database;

public class Tester {

	public static void main(String[] args) {
		SaveFileInterface sfi = SFISyncProxy.of("lab/test1");
//		sfi.put("key1", 5);
		System.out.println(sfi.getInt("key1"));
//		sfi.putJSONObject("key2");
//		SaveFileInterface addon = sfi.getJSONObject("key2");
//		addon.put("key1", 5);
	}

}
