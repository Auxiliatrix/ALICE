package alice.framework.database;

import java.util.Iterator;
import java.util.function.Function;

import org.json.JSONArray;
import org.json.JSONException;
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
	
	// TODO: currently returns object so that it can return null if object not found; might be better to simply cast nulls to 0, or to allow errors to filter through
	
	/* Atomic Getter Functions */
	public Object get(String key) {
		try { return SharedSaveFile.lockReaderAndExecute(saveFileName, () -> object.get(key)); } catch (JSONException j) {return null;}
	}
	
	public Boolean getBoolean(String key) {
		try { return SharedSaveFile.lockReaderAndExecute(saveFileName, () -> object.getBoolean(key)); } catch (JSONException j) {return null;}
	}
	
	public String getString(String key) {
		try { return SharedSaveFile.lockReaderAndExecute(saveFileName, () -> object.getString(key)); } catch (JSONException j) {return null;}
	}
	
	public Integer getInt(String key) {
		try { return SharedSaveFile.lockReaderAndExecute(saveFileName, () -> object.getInt(key)); } catch (JSONException j) {return null;}
	}
	
	public Double getDouble(String key) {
		try { return SharedSaveFile.lockReaderAndExecute(saveFileName, () -> object.getDouble(key)); } catch (JSONException j) {return null;}
	}
	
	public SharedJSONArray getSharedJSONArray(String key) {
		try { return new SharedJSONArray(saveFileName, SharedSaveFile.lockReaderAndExecute(saveFileName, () -> object.getJSONArray(key))); } catch (JSONException j) {return null;}
	}
	
	public SharedJSONObject getSharedJSONObject(String key) {
		try { return new SharedJSONObject(saveFileName, SharedSaveFile.lockReaderAndExecute(saveFileName, () -> object.getJSONObject(key))); } catch (JSONException j) {return null;}
	}
	
	public boolean has(String key) {
		return SharedSaveFile.lockReaderAndExecute(saveFileName, () -> object.has(key));
	}
	
	public Iterator<String> getKeys() {
		return SharedSaveFile.lockReaderAndExecute(saveFileName, () -> object.keys());
	}
	
	/* Atomic Setter Functions */
	public void put(String key, Object o) {
		SharedSaveFile.lockWriterAndExecute(saveFileName, () -> object.put(key, o));
	}
	
	public void putBoolean(String key, boolean o) {
		SharedSaveFile.lockWriterAndExecute(saveFileName, () -> object.put(key, o));
	}
	
	public void getString(String key, String o) {
		SharedSaveFile.lockWriterAndExecute(saveFileName, () -> object.put(key, o));
	}
	
	public void putInt(String key, int o) {
		SharedSaveFile.lockWriterAndExecute(saveFileName, () -> object.put(key, o));
	}
	
	public void putDouble(String key, double o) {
		SharedSaveFile.lockWriterAndExecute(saveFileName, () -> object.put(key, o));
	}
	
	/**
	 * Associates a shallow copy of the given JSONArray with the given key.
	 * @param key String to associate the object with
	 * @param o JSONArray to insert
	 */
	public void putJSONArray(String key, JSONArray o) {
		SharedSaveFile.lockWriterAndExecute(saveFileName, () -> object.put(key, new JSONArray(o)));
	}
	
	/**
	 * Associates a shallow copy of the given JSONObject with the given key.
	 * @param key String to associate the object with
	 * @param o JSONObject to insert
	 */
	public void putJSONObject(String key, JSONObject o) {
		SharedSaveFile.lockWriterAndExecute(saveFileName, () -> object.put(key, new JSONObject(o)));
	}
	
	public void modify(String key, Function<Object, Object> modifyFunction) {
		SharedSaveFile.lockWriterAndExecute(saveFileName, () -> object.put(key, modifyFunction.apply(!object.has(key) ? null : object.get(key))));
	}
	
	public void modifyBoolean(String key, Function<Boolean, Boolean> modifyFunction) {
		SharedSaveFile.lockWriterAndExecute(saveFileName, () -> object.put(key, modifyFunction.apply(!object.has(key) ? null : object.getBoolean(key))));
	}
	
	public void modifyString(String key, Function<String, String> modifyFunction) {
		SharedSaveFile.lockWriterAndExecute(saveFileName, () -> object.put(key, modifyFunction.apply(!object.has(key) ? null : object.getString(key))));
	}
	
	public void modifyInt(String key, Function<Integer, Integer> modifyFunction) {
		SharedSaveFile.lockWriterAndExecute(saveFileName, () -> object.put(key, modifyFunction.apply(!object.has(key) ? null : object.getInt(key))));
	}
	
	public void modifyDouble(String key, Function<Double, Double> modifyFunction) {
		SharedSaveFile.lockWriterAndExecute(saveFileName, () -> object.put(key, modifyFunction.apply(!object.has(key) ? null : object.getDouble(key))));
	}
	
	public void modifyJSONArray(String key, Function<JSONArray, JSONArray> modifyFunction) {
		SharedSaveFile.lockWriterAndExecute(saveFileName, () -> object.put(key, modifyFunction.apply(new JSONArray(!object.has(key) ? null : object.getJSONArray(key)))));	// New JSONArray constructed to prevent copying from within the modifier
	}
	
	public void modifyJSONObject(String key, Function<JSONObject, JSONObject> modifyFunction) {
		SharedSaveFile.lockWriterAndExecute(saveFileName, () -> object.put(key, modifyFunction.apply(new JSONObject(!object.has(key) ? null : object.getJSONObject(key)))));	// New JSONObject constructed to prevent copying from within the modifier
	}
	
	public void remove(String key) {
		SharedSaveFile.lockWriterAndExecute(saveFileName, () -> object.remove(key));
	}
}