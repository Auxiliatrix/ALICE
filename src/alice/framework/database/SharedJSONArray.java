package alice.framework.database;

import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONObject;

public class SharedJSONArray {
	protected String saveFileName;
	protected JSONArray array;
	
	/**
	 * Construct a SharedJSONArray, linking it with the save file name of the save file it was taken from.
	 * This array cannot be constructed outside of its package to prevent overlapping saveFileNames.
	 * @param saveFileName String reference to the file this JSONArray was taken from
	 * @param array JSONArray that was pulled
	 */
	protected SharedJSONArray(String saveFileName, JSONArray array) {
		this.saveFileName = saveFileName;
		this.array = array;
	}
	
	/* Getter Functions */
	public Object get(int index) {
		return SharedSaveFile.lockReaderAndExecute(saveFileName, k -> array.get(k), index);
	}
	
	public boolean getBoolean(int index) {
		return SharedSaveFile.lockReaderAndExecute(saveFileName, k -> array.getBoolean(k), index);
	}
	
	public String getString(int index) {
		return SharedSaveFile.lockReaderAndExecute(saveFileName, k -> array.getString(k), index);
	}
	
	public int getInt(int index) {
		return SharedSaveFile.lockReaderAndExecute(saveFileName, k -> array.getInt(k), index);
	}
	
	public double getDouble(int index) {
		return SharedSaveFile.lockReaderAndExecute(saveFileName, k -> array.getDouble(k), index);
	}
	
	public SharedJSONArray getSharedJSONArray(int index) {
		return new SharedJSONArray(saveFileName, SharedSaveFile.lockReaderAndExecute(saveFileName, k -> array.getJSONArray(k), index));
	}
	
	public SharedJSONObject getSharedJSONObject(int index) {
		return new SharedJSONObject(saveFileName, SharedSaveFile.lockReaderAndExecute(saveFileName, k -> array.getJSONObject(k), index));
	}
	
	public boolean isNull(int index) {
		return SharedSaveFile.lockReaderAndExecute(saveFileName, k -> array.isNull(k), index);
	}
	
	public int length() {
		return SharedSaveFile.lockReaderAndExecute(saveFileName, k -> array.length(), null);
	}
	
	/* Setter Functions */
	public void put(int index, Object o) {
		SharedSaveFile.lockWriterAndExecute(saveFileName, (k, v) -> array.put(k, v), index, o);
	}
	
	public void putBoolean(int index, boolean o) {
		SharedSaveFile.lockWriterAndExecute(saveFileName, (k, v) -> array.put(k, v), index, o);
	}
	
	public void getString(int index, String o) {
		SharedSaveFile.lockWriterAndExecute(saveFileName, (k, v) -> array.put(k, v), index, o);
	}
	
	public void putInt(int index, int o) {
		SharedSaveFile.lockWriterAndExecute(saveFileName, (k, v) -> array.put(k, v), index, o);
	}
	
	public void putDouble(int index, double o) {
		SharedSaveFile.lockWriterAndExecute(saveFileName, (k, v) -> array.put(k, v), index, o);
	}
	
	public void putJSONArray(int index, JSONArray o) {
		SharedSaveFile.lockWriterAndExecute(saveFileName, (k, v) -> array.put(k, v), index, o);
	}
	
	public void putJSONObject(int index, JSONObject o) {
		SharedSaveFile.lockWriterAndExecute(saveFileName, (k, v) -> array.put(k, v), index, o);
	}
	
	public void remove(int index) {
		SharedSaveFile.lockWriterAndExecute(saveFileName, k -> array.remove(k), index);
	}
	
}
