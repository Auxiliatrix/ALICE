package alice.framework.database;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.json.JSONObject;

import alice.framework.utilities.FileIO;
import alice.framework.utilities.ReadWriteReentrantLock;

public class SharedSaveFile {

	protected static Map<String, ReadWriteReentrantLock> lockMap = new HashMap<String, ReadWriteReentrantLock>();
	protected static Map<String, JSONObject> cache = new HashMap<String, JSONObject>();

	protected String saveFileName;
	
	public SharedSaveFile(String saveFileName) {
		this.saveFileName = saveFileName;
		
		if( !lockMap.containsKey(saveFileName) ) {
			lockMap.put(saveFileName, new ReadWriteReentrantLock(true));
		}
		if( !cache.containsKey(saveFileName) ) {
			cache.put(saveFileName, new JSONObject(FileIO.readFromFile(saveFileName, "")));
		}
	}
	
	public void accumulate(String key, Object value) {
		lockWriterAndExecute((k, v) -> cache.get(saveFileName).accumulate(k, v), key, value);
	}
	
	public void append(String key, Object value) {
		lockWriterAndExecute((k, v) -> cache.get(saveFileName).append(k, v), key, value);
	}
	
	public Object get(String key) {
		return lockReaderAndExecute(k -> cache.get(saveFileName).get(k), key);
	}
	
	// getEnum
	
	
	
	protected void lockReader() {
		lockMap.get(saveFileName).lockReader();
	}
	
	protected void unlockReader() {
		lockMap.get(saveFileName).unlockReader();
	}
	
	protected void lockWriter() {
		lockMap.get(saveFileName).lockWriter();
	}
	
	protected void unlockWriter() {
		lockMap.get(saveFileName).unlockWriter();
	}
	
	protected <O> O lockReaderAndExecute(Function<String, O> readFunction, String key) {
		lockReader();
		O out = null;
		try {
			out = readFunction.apply(key);
		} finally {
			unlockReader();
		}
		return out;
	}
	
	protected <I, O> O lockReaderAndExecute(BiFunction<String, I, O> readFunction, String key, I input) {
		lockReader();
		O out = null;
		try {
			out = readFunction.apply(key, input);
		} finally {
			unlockReader();
		}
		return out;
	}
	
	protected <I> void lockWriterAndExecute(BiConsumer<String, I> writeFunction, String key, I in) {
		lockWriter();
		try {
			writeFunction.accept(key, in);
		} finally {
			FileIO.writeToFile(saveFileName, cache.get(saveFileName).toString(1));
			unlockWriter();
		}
	}
	
}
