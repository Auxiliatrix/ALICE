package alice.framework.database;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import org.json.JSONObject;

import alice.framework.main.Constants;
import alice.framework.utilities.FileIO;
import alice.framework.utilities.ReadWriteReentrantLock;

/**
 * A statically accessible interface for Guild save data files that ensures that save files are accessed atomically across concurrent processes.
 * @author Auxiliatrix
 *
 */
public class SharedSaveFile extends SharedJSONObject {
	
	/**
	 * A static map which associates file names with a ReadWriteReentrantLock. This Lock is automatically created and associated when a new save file is accessed.
	 * It is accessed statically whenever the associated file is read from or written to.
	 */
	protected static Map<String, ReadWriteReentrantLock> lockMap = new HashMap<String, ReadWriteReentrantLock>();
	
	/**
	 * A static map which associates file names with their JSONObject contents.
	 * The cache is updated first, and then written to the save file. The save file itself is only read the first time it is loaded.
	 */
	protected static Map<String, JSONObject> cache = new HashMap<String, JSONObject>();
	
	public SharedSaveFile(long guildId) {
		this(String.format("%s%s%s%s%s.json", Constants.TEMP_DATA_DIRECTORY, File.separator, Constants.GUILD_DATA_SUBDIRECTORY, File.separator, guildId));
	}
	
	/**
	 * Constructs an accessing interface for the given save file name.
	 * It also updates the static maps accordingly.
	 * @param saveFileName String name of the file being accessed
	 */
	public SharedSaveFile(String saveFileName) {
		super(saveFileName, new JSONObject(FileIO.readFromFile(saveFileName, Constants.DEFAULT_GUILD_DATA)));

		if( !lockMap.containsKey(saveFileName) ) {
			lockMap.put(saveFileName, new ReadWriteReentrantLock(true));
		}
		if( !cache.containsKey(saveFileName) ) {
			cache.put(saveFileName, object);
		}
	}
	
	/* Lock Management Functions */
	protected static void lockReader(String fileName) {
		lockMap.get(fileName).lockReader();
	}
	
	protected static void unlockReader(String fileName) {
		lockMap.get(fileName).unlockReader();
	}
	
	protected static void lockWriter(String fileName) {
		lockMap.get(fileName).lockWriter();
	}
	
	protected static void unlockWriter(String fileName) {
		lockMap.get(fileName).unlockWriter();
	}
	
	public static <O> O lockReaderAndExecute(String fileName, Supplier<O> readFunction) {
		lockReader(fileName);
		O out = null;
		try {
			out = readFunction.get();
		} finally {
			unlockReader(fileName);
		}
		return out;
	}
	
	public static void lockWriterAndExecute(String fileName, Runnable writeFunction) {
		lockWriter(fileName);
		try {
			writeFunction.run();;
		} finally {
			FileIO.writeToFile(fileName, cache.get(fileName).toString(1));
			unlockWriter(fileName);
		}
	}
}