package alice.framework.database;

import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * A wrapper class for JSONObjects to work with the Sharing concurrent design.
 * This structure allows concurrent modification of JSONObjects, while still maintaining concurrency from the save file it was drawn from, and while still allowing the JSONObjects to be passed by value.
 * @author Auxiliatrix
 *
 */
public class SharedJSONObject {
	protected String saveFileName;
	protected JSONObject object;
	
	/**
	 * Construct a SharedJSONObject, linking it with the save file name of the save file it was taken from.
	 * This object cannot be constructed outside of its package to prevent overlapping saveFileNames.
	 * @param saveFileName String reference to the file this JSONObject was taken from
	 * @param object JSONObject that was pulled
	 */
	protected SharedJSONObject(String saveFileName, JSONObject object) {
		this.saveFileName = saveFileName;
		this.object = object;
	}
	
	/* Getter Functions */
	public Object get(String key) {
		return SharedSaveFile.lockReaderAndExecute(saveFileName, k -> object.get(k), key);
	}
	
	public boolean getBoolean(String key) {
		return SharedSaveFile.lockReaderAndExecute(saveFileName, k -> object.getBoolean(k), key);
	}
	
	public String getString(String key) {
		return SharedSaveFile.lockReaderAndExecute(saveFileName, k -> object.getString(k), key);
	}
	
	public int getInt(String key) {
		return SharedSaveFile.lockReaderAndExecute(saveFileName, k -> object.getInt(k), key);
	}
	
	public double getDouble(String key) {
		return SharedSaveFile.lockReaderAndExecute(saveFileName, k -> object.getDouble(k), key);
	}
	
	public SharedJSONArray getSharedJSONArray(String key) {
		return new SharedJSONArray(saveFileName, SharedSaveFile.lockReaderAndExecute(saveFileName, k -> object.getJSONArray(k), key));
	}
	
	public SharedJSONObject getSharedJSONObject(String key) {
		return new SharedJSONObject(saveFileName, SharedSaveFile.lockReaderAndExecute(saveFileName, k -> object.getJSONObject(k), key));
	}
	
	public boolean has(String key) {
		return SharedSaveFile.lockReaderAndExecute(saveFileName, k -> object.has(k), key);
	}
	
	public Iterator<String> getKeys() {
		return SharedSaveFile.lockReaderAndExecute(saveFileName, k -> object.keys(), null);
	}
	
	/* Setter Functions */
	public void put(String key, Object o) {
		SharedSaveFile.lockWriterAndExecute(saveFileName, (k, v) -> object.put(k, v), key, o);
	}
	
	public void putBoolean(String key, boolean o) {
		SharedSaveFile.lockWriterAndExecute(saveFileName, (k, v) -> object.put(k, v), key, o);
	}
	
	public void getString(String key, String o) {
		SharedSaveFile.lockWriterAndExecute(saveFileName, (k, v) -> object.put(k, v), key, o);
	}
	
	public void putInt(String key, int o) {
		SharedSaveFile.lockWriterAndExecute(saveFileName, (k, v) -> object.put(k, v), key, o);
	}
	
	public void putDouble(String key, double o) {
		SharedSaveFile.lockWriterAndExecute(saveFileName, (k, v) -> object.put(k, v), key, o);
	}
	
	public void putJSONArray(String key, JSONArray o) {
		SharedSaveFile.lockWriterAndExecute(saveFileName, (k, v) -> object.put(k, v), key, o);
	}
	
	public void putJSONObject(String key, JSONObject o) {
		SharedSaveFile.lockWriterAndExecute(saveFileName, (k, v) -> object.put(k, v), key, o);
	}
	
	public void remove(String key) {
		SharedSaveFile.lockWriterAndExecute(saveFileName, k -> object.remove(k), key);
	}
}