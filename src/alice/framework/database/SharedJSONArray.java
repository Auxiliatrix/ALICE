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
	
	// TODO: can only put json objects from a shared interface
	// TODO: type checking
	
	/* Atomic Getter Functions */
	public Object get(int index) {
		return SharedSaveFile.lockReaderAndExecute(saveFileName, () -> array.get(index));
	}
	
	public boolean getBoolean(int index) {
		return SharedSaveFile.lockReaderAndExecute(saveFileName, () -> array.getBoolean(index));
	}
	
	public String getString(int index) {
		return SharedSaveFile.lockReaderAndExecute(saveFileName, () -> array.getString(index));
	}
	
	public int getInt(int index) {
		return SharedSaveFile.lockReaderAndExecute(saveFileName, () -> array.getInt(index));
	}
	
	public double getDouble(int index) {
		return SharedSaveFile.lockReaderAndExecute(saveFileName, () -> array.getDouble(index));
	}
	
	public SharedJSONArray getSharedJSONArray(int index) {
		return new SharedJSONArray(saveFileName, SharedSaveFile.lockReaderAndExecute(saveFileName, () -> array.getJSONArray(index)));
	}
	
	public SharedJSONObject getSharedJSONObject(int index) {
		return new SharedJSONObject(saveFileName, SharedSaveFile.lockReaderAndExecute(saveFileName, () -> array.getJSONObject(index)));
	}
	
	public boolean isNull(int index) {
		return SharedSaveFile.lockReaderAndExecute(saveFileName, () -> array.isNull(index));
	}
	
	public int length() {
		return SharedSaveFile.lockReaderAndExecute(saveFileName, () -> array.length());
	}
	
	/* Atomic Setter Functions */
	public void put(Object o) {
		SharedSaveFile.lockWriterAndExecute(saveFileName, () -> array.put(o));
	}
	
	public void putBoolean(boolean o) {
		SharedSaveFile.lockWriterAndExecute(saveFileName, () -> array.put(o));
	}
	
	public void putString(String o) {
		SharedSaveFile.lockWriterAndExecute(saveFileName, () -> array.put(o));
	}
	
	public void putInteger(int o) {
		SharedSaveFile.lockWriterAndExecute(saveFileName, () -> array.put(o));
	}
	
	public void putDouble(double o) {
		SharedSaveFile.lockWriterAndExecute(saveFileName, () -> array.put(o));
	}
	
	public void putJSONArray(JSONArray o) {
		SharedSaveFile.lockWriterAndExecute(saveFileName, () -> array.put(o));
	}
	
	public void putJSONObject(JSONObject o) {
		SharedSaveFile.lockWriterAndExecute(saveFileName, () -> array.put(o));
	}
	
	public void put(int index, Object o) {
		SharedSaveFile.lockWriterAndExecute(saveFileName, () -> array.put(index, o));
	}
	
	public void putBoolean(int index, boolean o) {
		SharedSaveFile.lockWriterAndExecute(saveFileName, () -> array.put(index, o));
	}
	
	public void getString(int index, String o) {
		SharedSaveFile.lockWriterAndExecute(saveFileName, () -> array.put(index, o));
	}
	
	public void putInt(int index, int o) {
		SharedSaveFile.lockWriterAndExecute(saveFileName, () -> array.put(index, o));
	}
	
	public void putDouble(int index, double o) {
		SharedSaveFile.lockWriterAndExecute(saveFileName, () -> array.put(index, o));
	}
	
	/**
	 * Inserts a shallow copy of the given JSONArray into the given index.
	 * @param index int location to insert the object into
	 * @param o JSONArray to insert
	 */
	public void putJSONArray(int index, JSONArray o) {
		SharedSaveFile.lockWriterAndExecute(saveFileName, () -> array.put(index, new JSONArray(o)));
	}
	
	/**
	 * Inserts a shallow copy of the given JSONObject into the given index.
	 * @param index int location to insert the object into
	 * @param o JSONObject to insert
	 */
	public void putJSONObject(int index, JSONObject o) {
		SharedSaveFile.lockWriterAndExecute(saveFileName, () -> array.put(index, new JSONObject(o)));
	}
	
	public void remove(int index) {
		SharedSaveFile.lockWriterAndExecute(saveFileName, () -> array.remove(index));
	}
	
}
