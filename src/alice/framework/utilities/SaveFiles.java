package alice.framework.utilities;

import java.io.File;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import alice.framework.main.Constants;
import alina.structures.ReadWriteReentrantLock;
import alina.structures.SaveSyncProxy;
import alina.structures.SyncedJSONObject;
import alina.utilities.FileIO;
import discord4j.common.util.Snowflake;

public interface SaveFiles extends SyncedJSONObject {

	public static SyncedJSONObject of(String key) {
		if( !SaveSyncProxy.lockMap.containsKey(key) ) {
			AliceLogger.info(String.format("Loaded guild data from %s.", key), 1);
			SaveSyncProxy.lockMap.put(key, new ReadWriteReentrantLock());
		}
		
		if( SaveSyncProxy.cache.containsKey(key) ) {
			return (SyncedJSONObject) SaveSyncProxy.cache.get(key);
		} else {
			SyncedJSONObject sfi = (SyncedJSONObject) Proxy.newProxyInstance(
					SaveSyncProxy.class.getClassLoader(), 
					new Class[] {SyncedJSONObject.class}, 
					new SaveSyncProxy(key, new JSONObject(FileIO.readFromFile(key, Constants.DEFAULT_GUILD_DATA))));
			SaveSyncProxy.cache.put(key, sfi);
			return (SyncedJSONObject) sfi;
		}
	}
	
	public static SyncedJSONObject ofGuild(long guildID) {
		return of(String.format("%s%s%s%s%s.json", Constants.TEMP_DATA_DIRECTORY, File.separator, Constants.GUILD_DATA_SUBDIRECTORY, File.separator, guildID));
	}
	
	public static SyncedJSONObject ofGuild(Snowflake guildID) {
		return ofGuild(guildID.asLong());
	}
	
	public static SyncedJSONObject navigate(SyncedJSONObject root, List<String> path, List<String> index) {
		return navigate(root, root, path, index);
	}
	
	public static SyncedJSONObject navigate(SyncedJSONObject root, SyncedJSONObject current, List<String> path, List<String> index) {
		String next = path.remove(0);
		switch( next ) {
			case "~":
				index.clear();
				index.add("~");
				return navigate(root, root, path, index);
			case ".":
				if( index.size() == 1 ) {
					return null;
				} else {
					index.remove(index.size()-1);
					return navigate(root, navigate(root, index, new ArrayList<String>()), path, index);
				}
			case "..":
				return navigate(root, current, path, index);
			default:
				if( current.has(next) ) {
					index.add(next);
					return navigate(root, current.getJSONObject(next), path, index);
				} else {
					return null;
				}
		}
	}
}
