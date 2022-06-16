package alice.framework.database;

import java.io.File;
import java.lang.reflect.Proxy;

import org.json.JSONObject;

import alice.framework.main.Constants;
import alice.framework.utilities.AliceLogger;
import alice.framework.utilities.FileIO;
import alice.framework.utilities.ReadWriteReentrantLock;

public interface SyncedSaveFile extends SyncedJSONObject {

	public static SyncedSaveFile of(String key) {
		if( !SaveSyncProxy.lockMap.containsKey(key) ) {
			AliceLogger.info(String.format("Loaded guild data from %s.", key), 1);
			SaveSyncProxy.lockMap.put(key, new ReadWriteReentrantLock(true));
		}
		
		if( SaveSyncProxy.cache.containsKey(key) ) {
			return (SyncedSaveFile) SaveSyncProxy.cache.get(key);
		} else {
			SyncedJSONObject sfi = (SyncedJSONObject) Proxy.newProxyInstance(
					SaveSyncProxy.class.getClassLoader(), 
					new Class[] {SyncedJSONObject.class}, 
					new SaveSyncProxy(key, new JSONObject(FileIO.readFromFile(key, Constants.DEFAULT_GUILD_DATA))));
			SaveSyncProxy.cache.put(key, sfi);
			return (SyncedSaveFile) sfi;
		}
	}
	
	public static SyncedSaveFile ofGuild(long guildID) {
		return of(String.format("%s%s%s%s%s.json", Constants.TEMP_DATA_DIRECTORY, File.separator, Constants.GUILD_DATA_SUBDIRECTORY, File.separator, guildID));
	}
}
