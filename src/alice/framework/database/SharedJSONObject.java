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
	protected String saveFileName;
	
	protected SharedJSONArray parentArray;
	protected SharedJSONObject parentObject;
	protected int referenceIndex;
	protected String referenceKey;
	
	protected JSONObject cached;
		
	/**
	 * Construct a SharedJSONObject, linking it with the save file name of the save file it was taken from.
	 * This object cannot be constructed outside of its package to prevent overlapping saveFileNames.
	 */
	protected SharedJSONObject(String saveFileName) {
		this.saveFileName = saveFileName;
		
		this.parentArray = null;
		this.parentObject = null;
		this.referenceIndex = -1;
		this.referenceKey = null;
		try { cached = SharedSaveFile.lockReaderAndExecute(saveFileName, jo -> getSelfFromParent(jo)); } catch (JSONException j) {cached = new JSONObject();}
	}
	
	protected SharedJSONObject(SharedJSONArray parent, String saveFileName, int reference) {
		this.saveFileName = saveFileName;
		
		this.parentArray = parent;
		this.parentObject = null;
		this.referenceIndex = reference;
		this.referenceKey = null;
	}
	
	protected SharedJSONObject(SharedJSONObject parent, String saveFileName, String reference) {
		this.saveFileName = saveFileName;
		
		this.parentArray = null;
		this.parentObject = parent;
		this.referenceIndex = -1;
		this.referenceKey = reference;
	}
	
	// TODO: currently returns object so that it can return null if object not found; might be better to simply cast nulls to 0, or to allow errors to filter through
	
	protected JSONObject getSelfFromParent(Map<String, JSONObject> origin) {
		try {
			if( parentArray == null && parentObject == null ) {
				return origin.get(saveFileName);
			} else if( parentArray == null ) {
				return parentObject.getSelfFromParent(origin).getJSONObject(referenceKey);
			} else {
				return parentArray.getSelfFromParent(origin).getJSONObject(referenceIndex);
			}
		} catch (JSONException j) {
			return cached;
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
	
	public SharedJSONArray getSharedJSONArray(String key) {
		try { 
			SharedSaveFile.lockReaderAndExecute(saveFileName, jo -> getSelfFromParent(jo).getJSONArray(key));
			return new SharedJSONArray(this, saveFileName, key);
		} catch (JSONException j) {
			return null;
		}
	}
	
	public SharedJSONArray getOrDefaultSharedJSONArray(String key) {
		try { 
			SharedSaveFile.lockReaderAndExecute(saveFileName, jo -> getSelfFromParent(jo).getJSONArray(key));
			return new SharedJSONArray(this, saveFileName, key);
		} catch (JSONException j) {
			SharedSaveFile.lockWriterAndExecute(saveFileName, jo -> {getSelfFromParent(jo).put(key, new JSONArray(key)); cached = getSelfFromParent(jo); } );
			return new SharedJSONArray(this, saveFileName, key);
		}
	}
	
	public SharedJSONObject getSharedJSONObject(String key) {
		try { 
			SharedSaveFile.lockReaderAndExecute(saveFileName, jo -> getSelfFromParent(jo).getJSONObject(key));
			return new SharedJSONObject(this, saveFileName, key);
		} catch (JSONException j) {
			return null;
		}
	}
	
	public SharedJSONObject getOrDefaultSharedJSONObject(String key) {
		try { 
			SharedSaveFile.lockReaderAndExecute(saveFileName, jo -> getSelfFromParent(jo).getJSONObject(key));
			return new SharedJSONObject(this, saveFileName, key);
		} catch (JSONException j) {
			SharedSaveFile.lockWriterAndExecute(saveFileName, jo -> {
				getSelfFromParent(jo).put(key, new JSONObject(key));
				if( getSelfFromParent(jo).getJSONObject(key).has("empty") ) {
					getSelfFromParent(jo).getJSONObject(key).remove("empty");
				}
				cached = getSelfFromParent(jo); 
			} );
			return new SharedJSONObject(this, saveFileName, key);
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
		SharedSaveFile.lockWriterAndExecute(saveFileName, jo -> {getSelfFromParent(jo).put(key, o); cached = getSelfFromParent(jo); });
	}
	
	public void putBoolean(String key, boolean o) {
		SharedSaveFile.lockWriterAndExecute(saveFileName, jo -> {getSelfFromParent(jo).put(key, o); cached = getSelfFromParent(jo); });
	}
	
	public void putString(String key, String o) {
		SharedSaveFile.lockWriterAndExecute(saveFileName, jo -> {getSelfFromParent(jo).put(key, o); cached = getSelfFromParent(jo); });
	}
	
	public void putInt(String key, int o) {
		SharedSaveFile.lockWriterAndExecute(saveFileName, jo -> {getSelfFromParent(jo).put(key, o); cached = getSelfFromParent(jo); });
	}
	
	public void putDouble(String key, double o) {
		SharedSaveFile.lockWriterAndExecute(saveFileName, jo -> {getSelfFromParent(jo).put(key, o); cached = getSelfFromParent(jo); });
	}
	
	/**
	 * Creates a new JSONObject with the given key.
	 * If a JSONObject already exists, it will be replaced, and any SharedJSONObjects referencing it will be re-associated with the new JSONObject.
	 * This replaces putting a JSONObject to prevent SharedJSONObjects from being associated with more than one parent.
	 * To access a given JSONObject an origin, it should be referenced from the same location.
	 * @param key String to associate the object with
	 */
	public void createJSONObject(String key) {
		SharedSaveFile.lockWriterAndExecute(saveFileName, jo -> {getSelfFromParent(jo).put(key, new JSONObject()); cached = getSelfFromParent(jo); });
	}
	
	public void modify(String key, Function<Object, Object> modifyFunction) {
		SharedSaveFile.lockWriterAndExecute(saveFileName, jo -> {getSelfFromParent(jo).put(key, modifyFunction.apply(!getSelfFromParent(jo).has(key) ? null : getSelfFromParent(jo).get(key))); cached = getSelfFromParent(jo); });
	}
	
	public void modifyBoolean(String key, Function<Boolean, Boolean> modifyFunction) {
		SharedSaveFile.lockWriterAndExecute(saveFileName, jo -> {getSelfFromParent(jo).put(key, modifyFunction.apply(!getSelfFromParent(jo).has(key) ? null : getSelfFromParent(jo).getBoolean(key))); cached = getSelfFromParent(jo); });
	}
	
	public void modifyString(String key, Function<String, String> modifyFunction) {
		SharedSaveFile.lockWriterAndExecute(saveFileName, jo -> {getSelfFromParent(jo).put(key, modifyFunction.apply(!getSelfFromParent(jo).has(key) ? null : getSelfFromParent(jo).getString(key))); cached = getSelfFromParent(jo); });
	}
	
	public void modifyInt(String key, Function<Integer, Integer> modifyFunction) {
		SharedSaveFile.lockWriterAndExecute(saveFileName, jo -> {getSelfFromParent(jo).put(key, modifyFunction.apply(!getSelfFromParent(jo).has(key) ? null : getSelfFromParent(jo).getInt(key))); cached = getSelfFromParent(jo); });
	}
	
	public void modifyDouble(String key, Function<Double, Double> modifyFunction) {
		SharedSaveFile.lockWriterAndExecute(saveFileName, jo -> {getSelfFromParent(jo).put(key, modifyFunction.apply(!getSelfFromParent(jo).has(key) ? null : getSelfFromParent(jo).getDouble(key))); cached = getSelfFromParent(jo); });
	}
	
	public void modifyJSONArray(String key, Function<JSONArray, JSONArray> modifyFunction) {
		SharedSaveFile.lockWriterAndExecute(saveFileName, jo -> {getSelfFromParent(jo).put(key, modifyFunction.apply(new JSONArray(!getSelfFromParent(jo).has(key) ? null : getSelfFromParent(jo).getJSONArray(key)))); cached = getSelfFromParent(jo); });	// New JSONArray constructed to prevent copying from within the modifier
	}
	
	public void modifyJSONObject(String key, Function<JSONObject, JSONObject> modifyFunction) {
		SharedSaveFile.lockWriterAndExecute(saveFileName, jo -> {getSelfFromParent(jo).put(key, modifyFunction.apply(new JSONObject(!getSelfFromParent(jo).has(key) ? null : getSelfFromParent(jo).getJSONObject(key)))); cached = getSelfFromParent(jo); });	// New JSONObject constructed to prevent copying from within the modifier
	}
	
	public void remove(String key) {
		SharedSaveFile.lockWriterAndExecute(saveFileName, jo -> {getSelfFromParent(jo).remove(key); cached = getSelfFromParent(jo); });
	}
}