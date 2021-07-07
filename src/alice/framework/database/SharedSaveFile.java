package alice.framework.database;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import org.json.JSONObject;

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
	
	/**
	 * Constructs an accessing interface for the given save file name.
	 * It also updates the static maps accordingly.
	 * @param saveFileName String name of the file being accessed
	 */
	public SharedSaveFile(String saveFileName) {
		super(saveFileName, new JSONObject(FileIO.readFromFile(saveFileName, "")));

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
	
	protected static <O> O lockReaderAndExecute(String fileName, Function<Void, O> readFunction) {
		lockReader(fileName);
		O out = null;
		try {
			out = readFunction.apply(null);
		} finally {
			unlockReader(fileName);
		}
		return out;
	}
	
	protected static <O> O lockReaderAndExecute(String fileName, Function<Integer, O> readFunction, int index) {
		lockReader(fileName);
		O out = null;
		try {
			out = readFunction.apply(index);
		} finally {
			unlockReader(fileName);
		}
		return out;
	}
	
	protected static <O> O lockReaderAndExecute(String fileName, Function<String, O> readFunction, String key) {
		lockReader(fileName);
		O out = null;
		try {
			out = readFunction.apply(key);
		} finally {
			unlockReader(fileName);
		}
		return out;
	}
	
	protected static <I, O> O lockReaderAndExecute(String fileName, BiFunction<Integer, I, O> readFunction, int index, I input) {
		lockReader(fileName);
		O out = null;
		try {
			out = readFunction.apply(index, input);
		} finally {
			unlockReader(fileName);
		}
		return out;
	}
	
	protected static <I, O> O lockReaderAndExecute(String fileName, BiFunction<String, I, O> readFunction, String key, I input) {
		lockReader(fileName);
		O out = null;
		try {
			out = readFunction.apply(key, input);
		} finally {
			unlockReader(fileName);
		}
		return out;
	}
	
	protected static void lockWriterAndExecute(String fileName, Consumer<Integer> writeFunction, int index) {
		lockWriter(fileName);
		try {
			writeFunction.accept(index);
		} finally {
			FileIO.writeToFile(fileName, cache.get(fileName).toString(1));
			unlockWriter(fileName);
		}
	}
	
	protected static void lockWriterAndExecute(String fileName, Consumer<String> writeFunction, String key) {
		lockWriter(fileName);
		try {
			writeFunction.accept(key);
		} finally {
			FileIO.writeToFile(fileName, cache.get(fileName).toString(1));
			unlockWriter(fileName);
		}
	}
	
	protected static <I> void lockWriterAndExecute(String fileName, BiConsumer<Integer, I> writeFunction, int index, I in) {
		lockWriter(fileName);
		try {
			writeFunction.accept(index, in);
		} finally {
			FileIO.writeToFile(fileName, cache.get(fileName).toString(1));
			unlockWriter(fileName);
		}
	}
	
	protected static <I> void lockWriterAndExecute(String fileName, BiConsumer<String, I> writeFunction, String key, I in) {
		lockWriter(fileName);
		try {
			writeFunction.accept(key, in);
		} finally {
			FileIO.writeToFile(fileName, cache.get(fileName).toString(1));
			unlockWriter(fileName);
		}
	}
	
	// TODO: make lockXandY not need type arguments
}
