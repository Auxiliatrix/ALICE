package alice.framework.database;

import java.util.Iterator;
import java.util.Map;
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
	protected SharedJSONObject parent;
	protected String saveFileName;
	protected String reference;
	
	// TODO: when a new save file is created, the objects are disconnected because it creates two separate instances of sharedjsonobject. therefore, calling a function in one will not edit the other
	
	/**
	 * Construct a SharedJSONObject, linking it with the save file name of the save file it was taken from.
	 * This object cannot be constructed outside of its package to prevent overlapping saveFileNames.
	 * @param saveFileName String reference to the file this JSONObject was taken from
	 * @param object JSONObject that was pulled
	 */
	protected SharedJSONObject(String saveFileName) {
		this(null, saveFileName, saveFileName);
	}
	
	protected SharedJSONObject(SharedJSONObject parent, String saveFileName, String reference) {
		this.parent = parent;
		this.saveFileName = saveFileName;
		this.reference = reference;
	}
	
	// TODO: currently returns object so that it can return null if object not found; might be better to simply cast nulls to 0, or to allow errors to filter through
	
	private JSONObject getSelfFromParent(Map<String, JSONObject> origin) {
		if( parent == null ) {
			return origin.get(reference);
		} else {
			return parent.getSelfFromParent(origin).getJSONObject(reference);
		}
	}
	
	/* Atomic Getter Functions */
	public Object get(String key) {
		try { return SharedSaveFile.lockReaderAndExecute(saveFileName, jo -> getSelfFromParent(jo).get(key)); } catch (JSONException j) {return null;}
	}
	
	public Boolean getBoolean(String key) {
		try { return SharedSaveFile.lockReaderAndExecute(saveFileName, jo -> getSelfFromParent(jo).getBoolean(key)); } catch (JSONException j) {return null;}
	}
	
	public String getString(String key) {
		try { return SharedSaveFile.lockReaderAndExecute(saveFileName, jo -> getSelfFromParent(jo).getString(key)); } catch (JSONException j) {return null;}
	}
	
	public Integer getInt(String key) {
		try { return SharedSaveFile.lockReaderAndExecute(saveFileName, jo -> getSelfFromParent(jo).getInt(key)); } catch (JSONException j) {return null;}
	}
	
	public Double getDouble(String key) {
		try { return SharedSaveFile.lockReaderAndExecute(saveFileName, jo -> getSelfFromParent(jo).getDouble(key)); } catch (JSONException j) {return null;}
	}
	
	public SharedJSONObject getSharedJSONObject(String key) {
		try {
			SharedSaveFile.lockReaderAndExecute(saveFileName, jo -> getSelfFromParent(jo).getJSONObject(key));
			SharedJSONObject result = new SharedJSONObject(this, saveFileName, key);
			return result;
		} catch (JSONException j) {
			return null;
		}
	}
	
	public boolean has(String key) {
		return SharedSaveFile.lockReaderAndExecute(saveFileName, jo -> getSelfFromParent(jo).has(key));
	}
	
	public Iterator<String> getKeys() {
		return SharedSaveFile.lockReaderAndExecute(saveFileName, jo -> getSelfFromParent(jo).keys());
	}
	
	/* Atomic Setter Functions */
	public void put(String key, Object o) {
		SharedSaveFile.lockWriterAndExecute(saveFileName, jo -> getSelfFromParent(jo).put(key, o));
	}
	
	public void putBoolean(String key, boolean o) {
		SharedSaveFile.lockWriterAndExecute(saveFileName, jo -> getSelfFromParent(jo).put(key, o));
	}
	
	public void putString(String key, String o) {
		SharedSaveFile.lockWriterAndExecute(saveFileName, jo -> getSelfFromParent(jo).put(key, o));
	}
	
	public void putInt(String key, int o) {
		SharedSaveFile.lockWriterAndExecute(saveFileName, jo -> getSelfFromParent(jo).put(key, o));
	}
	
	public void putDouble(String key, double o) {
		SharedSaveFile.lockWriterAndExecute(saveFileName, jo -> getSelfFromParent(jo).put(key, o));
	}
	
	/**
	 * Associates a shallow copy of the given JSONObject with the given key.
	 * @param key String to associate the object with
	 * @param o JSONObject to insert
	 */
	public void putJSONObject(String key, JSONObject o) {
		SharedSaveFile.lockWriterAndExecute(saveFileName, jo -> getSelfFromParent(jo).put(key, new JSONObject(o)));
	}
	
	public void modify(String key, Function<Object, Object> modifyFunction) {
		SharedSaveFile.lockWriterAndExecute(saveFileName, jo -> getSelfFromParent(jo).put(key, modifyFunction.apply(!getSelfFromParent(jo).has(key) ? null : getSelfFromParent(jo).get(key))));
	}
	
	public void modifyBoolean(String key, Function<Boolean, Boolean> modifyFunction) {
		SharedSaveFile.lockWriterAndExecute(saveFileName, jo -> getSelfFromParent(jo).put(key, modifyFunction.apply(!getSelfFromParent(jo).has(key) ? null : getSelfFromParent(jo).getBoolean(key))));
	}
	
	public void modifyString(String key, Function<String, String> modifyFunction) {
		SharedSaveFile.lockWriterAndExecute(saveFileName, jo -> getSelfFromParent(jo).put(key, modifyFunction.apply(!getSelfFromParent(jo).has(key) ? null : getSelfFromParent(jo).getString(key))));
	}
	
	public void modifyInt(String key, Function<Integer, Integer> modifyFunction) {
		SharedSaveFile.lockWriterAndExecute(saveFileName, jo -> getSelfFromParent(jo).put(key, modifyFunction.apply(!getSelfFromParent(jo).has(key) ? null : getSelfFromParent(jo).getInt(key))));
	}
	
	public void modifyDouble(String key, Function<Double, Double> modifyFunction) {
		SharedSaveFile.lockWriterAndExecute(saveFileName, jo -> getSelfFromParent(jo).put(key, modifyFunction.apply(!getSelfFromParent(jo).has(key) ? null : getSelfFromParent(jo).getDouble(key))));
	}
	
	public void modifyJSONArray(String key, Function<JSONArray, JSONArray> modifyFunction) {
		SharedSaveFile.lockWriterAndExecute(saveFileName, jo -> getSelfFromParent(jo).put(key, modifyFunction.apply(new JSONArray(!getSelfFromParent(jo).has(key) ? null : getSelfFromParent(jo).getJSONArray(key)))));	// New JSONArray constructed to prevent copying from within the modifier
	}
	
	public void modifyJSONObject(String key, Function<JSONObject, JSONObject> modifyFunction) {
		SharedSaveFile.lockWriterAndExecute(saveFileName, jo -> getSelfFromParent(jo).put(key, modifyFunction.apply(new JSONObject(!getSelfFromParent(jo).has(key) ? null : getSelfFromParent(jo).getJSONObject(key)))));	// New JSONObject constructed to prevent copying from within the modifier
	}
	
	public void remove(String key) {
		SharedSaveFile.lockWriterAndExecute(saveFileName, jo -> getSelfFromParent(jo).remove(key));
	}
}