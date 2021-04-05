package alice.framework.datamanagers;

import java.util.HashMap;
import java.util.Map;

import alice.framework.utilities.ReadWriteReentrantLock;
import discord4j.core.object.entity.Guild;

public class DataManager {

	protected static Map<Class<? extends DataManager>, Map<Guild, ReadWriteReentrantLock>> lockMap = new HashMap<Class<? extends DataManager>, Map<Guild, ReadWriteReentrantLock>>();
	protected Guild guild;
	
	public DataManager(Guild guild) {
		this.guild = guild;
		
		putLockIfNone(this);
	}
	
	protected static void putLockIfNone(DataManager dm) {
		if( !lockMap.containsKey(dm.getClass()) ) {
			lockMap.put(dm.getClass(), new HashMap<Guild, ReadWriteReentrantLock>());
		}
		if( !lockMap.get(dm.getClass()).containsKey(dm.guild) ) {
			lockMap.get(dm.getClass()).put(dm.guild, new ReadWriteReentrantLock(true));
		}
	}
	
	protected static void lockReader(DataManager dm) {
		putLockIfNone(dm);
		lockMap.get(dm.getClass()).get(dm.guild).lockReader();
	}
	
	protected static void unlockReader(DataManager dm) {
		putLockIfNone(dm);
		lockMap.get(dm.getClass()).get(dm.guild).unlockReader();
	}
	
	protected static void lockWriter(DataManager dm) {
		putLockIfNone(dm);
		lockMap.get(dm.getClass()).get(dm.guild).lockWriter();
	}
	
	protected static void unlockWriter(DataManager dm) {
		putLockIfNone(dm);
		lockMap.get(dm.getClass()).get(dm.guild).unlockWriter();
	}
	
}
