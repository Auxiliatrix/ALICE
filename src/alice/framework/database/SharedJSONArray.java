package alice.framework.database;

import java.util.Iterator;
import java.util.Map;
import java.util.function.Function;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * A wrapper class for JSONArrays to work with the Sharing concurrent design.
 * This structure allows concurrent modification of JSONArrays, while still maintaining concurrency from the save file it was drawn from, and while still allowing the JSONArrays to be passed by value.
 * @author Auxiliatrix
 *
 */
public class SharedJSONArray {
	protected SharedJSONArray parentArray;
	protected SharedJSONObject parentObject;
	protected String saveFileName;
	protected int referenceIndex;
	protected String referenceKey;
	
	protected JSONArray cached;
		
	/**
	 * Construct a SharedJSONArray, linking it with the save file name of the save file it was taken from.
	 * This object cannot be constructed outside of its package to prevent overlapping saveFileNames.
	 */
	protected SharedJSONArray(SharedJSONArray parent, String saveFileName, int reference) {
		this.saveFileName = saveFileName;
		
		this.parentArray = parent;
		this.parentObject = null;
		this.referenceIndex = reference;
		this.referenceKey = null;
		
		try { cached = SharedSaveFile.lockReaderAndExecute(saveFileName, jo -> getSelfFromParent(jo)); } catch (JSONException j) {cached = new JSONArray();}
	}
	
	protected SharedJSONArray(SharedJSONObject parent, String saveFileName, String reference) {
		this.saveFileName = saveFileName;
		
		this.parentArray = null;
		this.parentObject = parent;
		this.referenceIndex = -1;
		this.referenceKey = reference;
		
		try { cached = SharedSaveFile.lockReaderAndExecute(saveFileName, jo -> getSelfFromParent(jo)); } catch (JSONException j) {cached = new JSONArray();}
	}
	

	protected JSONArray getSelfFromParent(Map<String, JSONObject> origin) {
		try {
			if( parentArray == null ) {
				return parentObject.getSelfFromParent(origin).getJSONArray(referenceKey);
			} else {
				return parentArray.getSelfFromParent(origin).getJSONArray(referenceIndex);
			}
		} catch (JSONException j) {
			return cached;
		}
	}
	
	/* Atomic Getter Functions */
	public Object get(int index) {
		try { return SharedSaveFile.lockReaderAndExecute(saveFileName, jo -> getSelfFromParent(jo).get(index)); } catch (JSONException j) {return null;}
	}
	
	public Boolean getBoolean(int index) {
		try { return SharedSaveFile.lockReaderAndExecute(saveFileName, jo -> getSelfFromParent(jo).getBoolean(index)); } catch (JSONException j) {return null;}
	}
	
	public String getString(int index) {
		try { return SharedSaveFile.lockReaderAndExecute(saveFileName, jo -> getSelfFromParent(jo).getString(index)); } catch (JSONException j) {return null;}
	}
	
	public Integer getInt(int index) {
		try { return SharedSaveFile.lockReaderAndExecute(saveFileName, jo -> getSelfFromParent(jo).getInt(index)); } catch (JSONException j) {return null;}
	}
	
	public Double getDouble(int index) {
		try { return SharedSaveFile.lockReaderAndExecute(saveFileName, jo -> getSelfFromParent(jo).getDouble(index)); } catch (JSONException j) {return null;}
	}
	
	public SharedJSONArray getSharedJSONArray(int index) {
		try { 
			SharedSaveFile.lockReaderAndExecute(saveFileName, jo -> getSelfFromParent(jo).getJSONArray(index));
			return new SharedJSONArray(this, saveFileName, index);
		} catch (JSONException j) {
			return null;
		}
	}
	
	public SharedJSONArray getOrDefaultSharedJSONArray(int index) {
		try { 
			SharedSaveFile.lockReaderAndExecute(saveFileName, jo -> getSelfFromParent(jo).getJSONArray(index));
			return new SharedJSONArray(this, saveFileName, index);
		} catch (JSONException j) {
			SharedSaveFile.lockWriterAndExecute(saveFileName, jo -> {getSelfFromParent(jo).put(index, new JSONArray()); cached = getSelfFromParent(jo); } );
			return new SharedJSONArray(this, saveFileName, index);
		}
	}
	
	public SharedJSONObject getSharedJSONObject(int index) {
		try { 
			SharedSaveFile.lockReaderAndExecute(saveFileName, jo -> getSelfFromParent(jo).getJSONObject(index));
			return new SharedJSONObject(this, saveFileName, index);
		} catch (JSONException j) {
			return null;
		}
	}
	
	public SharedJSONObject getOrDefaultSharedJSONObject(int index) {
		try { 
			SharedSaveFile.lockReaderAndExecute(saveFileName, jo -> getSelfFromParent(jo).getJSONObject(index));
			return new SharedJSONObject(this, saveFileName, index);
		} catch (JSONException j) {
			SharedSaveFile.lockWriterAndExecute(saveFileName, jo -> {
				getSelfFromParent(jo).put(index, new JSONObject());
				if( getSelfFromParent(jo).getJSONObject(index).has("empty") ) {
					getSelfFromParent(jo).getJSONObject(index).remove("empty");
				}
				cached = getSelfFromParent(jo); 
			} );
			return new SharedJSONObject(this, saveFileName, index);
		}
	}
	
	public int length() {
		return SharedSaveFile.lockReaderAndExecute(saveFileName, jo -> getSelfFromParent(jo).length());
	}
	
	public Iterator<Object> iterator() {
		return SharedSaveFile.lockReaderAndExecute(saveFileName, jo -> getSelfFromParent(jo).iterator());
	}
	
	/* Atomic Setter Functions */
	public void put(int index, Object o) {
		SharedSaveFile.lockWriterAndExecute(saveFileName, jo -> {getSelfFromParent(jo).put(index, o); cached = getSelfFromParent(jo); });
	}
	
	public void put(int index, boolean o) {
		SharedSaveFile.lockWriterAndExecute(saveFileName, jo -> {getSelfFromParent(jo).put(index, o); cached = getSelfFromParent(jo); });
	}
	
	public void put(int index, String o) {
		SharedSaveFile.lockWriterAndExecute(saveFileName, jo -> {getSelfFromParent(jo).put(index, o); cached = getSelfFromParent(jo); });
	}
	
	public void put(int index, int o) {
		SharedSaveFile.lockWriterAndExecute(saveFileName, jo -> {getSelfFromParent(jo).put(index, o); cached = getSelfFromParent(jo); });
	}
	
	public void put(int index, double o) {
		SharedSaveFile.lockWriterAndExecute(saveFileName, jo -> {getSelfFromParent(jo).put(index, o); cached = getSelfFromParent(jo); });
	}
	
	public void put(Object o) {
		SharedSaveFile.lockWriterAndExecute(saveFileName, jo -> {getSelfFromParent(jo).put(o); cached = getSelfFromParent(jo); });
	}
	
	public void put(boolean o) {
		SharedSaveFile.lockWriterAndExecute(saveFileName, jo -> {getSelfFromParent(jo).put(o); cached = getSelfFromParent(jo); });
	}
	
	public void put(String o) {
		SharedSaveFile.lockWriterAndExecute(saveFileName, jo -> {getSelfFromParent(jo).put(o); cached = getSelfFromParent(jo); });
	}
	
	public void put(int o) {
		SharedSaveFile.lockWriterAndExecute(saveFileName, jo -> {getSelfFromParent(jo).put(o); cached = getSelfFromParent(jo); });
	}
	
	public void put(double o) {
		SharedSaveFile.lockWriterAndExecute(saveFileName, jo -> {getSelfFromParent(jo).put(o); cached = getSelfFromParent(jo); });
	}
	
	/**
	 * Creates a new JSONArray with the given index.
	 * If a JSONArray already exists, it will be replaced, and any SharedJSONArrays referencing it will be re-associated with the new JSONArray.
	 * This replaces putting a JSONArray to prevent SharedJSONArrays from being associated with more than one parent.
	 * To access a given JSONArray an origin, it should be referenced from the same location.
	 * @param index String to associate the object with
	 */
	public void createJSONArray(int index) {
		SharedSaveFile.lockWriterAndExecute(saveFileName, jo -> {getSelfFromParent(jo).put(index, new JSONArray()); cached = getSelfFromParent(jo); });
	}
	
	public void modify(int index, Function<Object, Object> modifyFunction) {
		SharedSaveFile.lockWriterAndExecute(saveFileName, jo -> {getSelfFromParent(jo).put(index, modifyFunction.apply(getSelfFromParent(jo).length() <= index ? null : getSelfFromParent(jo).get(index))); cached = getSelfFromParent(jo); });
	}
	
	public void modifyBoolean(int index, Function<Boolean, Boolean> modifyFunction) {
		SharedSaveFile.lockWriterAndExecute(saveFileName, jo -> {getSelfFromParent(jo).put(index, modifyFunction.apply(getSelfFromParent(jo).length() <= index ? null : getSelfFromParent(jo).getBoolean(index))); cached = getSelfFromParent(jo); });
	}
	
	public void modifyString(int index, Function<String, String> modifyFunction) {
		SharedSaveFile.lockWriterAndExecute(saveFileName, jo -> {getSelfFromParent(jo).put(index, modifyFunction.apply(getSelfFromParent(jo).length() <= index ? null : getSelfFromParent(jo).getString(index))); cached = getSelfFromParent(jo); });
	}
	
	public void modifyInt(int index, Function<Integer, Integer> modifyFunction) {
		SharedSaveFile.lockWriterAndExecute(saveFileName, jo -> {getSelfFromParent(jo).put(index, modifyFunction.apply(getSelfFromParent(jo).length() <= index ? null : getSelfFromParent(jo).getInt(index))); cached = getSelfFromParent(jo); });
	}
	
	public void modifyDouble(int index, Function<Double, Double> modifyFunction) {
		SharedSaveFile.lockWriterAndExecute(saveFileName, jo -> {getSelfFromParent(jo).put(index, modifyFunction.apply(getSelfFromParent(jo).length() <= index ? null : getSelfFromParent(jo).getDouble(index))); cached = getSelfFromParent(jo); });
	}
	
	public void modifyJSONArray(int index, Function<JSONArray, JSONArray> modifyFunction) {
		SharedSaveFile.lockWriterAndExecute(saveFileName, jo -> {getSelfFromParent(jo).put(index, modifyFunction.apply(new JSONArray(getSelfFromParent(jo).length() <= index ? null : getSelfFromParent(jo).getJSONArray(index)))); cached = getSelfFromParent(jo); });	// New JSONArray constructed to prevent copying from within the modifier
	}
	
	public void modifyJSONObject(int index, Function<JSONObject, JSONObject> modifyFunction) {
		SharedSaveFile.lockWriterAndExecute(saveFileName, jo -> {getSelfFromParent(jo).put(index, modifyFunction.apply(new JSONObject(getSelfFromParent(jo).length() <= index ? null : getSelfFromParent(jo).getJSONObject(index)))); cached = getSelfFromParent(jo); });	// New JSONArray constructed to prevent copying from within the modifier
	}
	
	public void remove(int index) {
		SharedSaveFile.lockWriterAndExecute(saveFileName, jo -> {getSelfFromParent(jo).remove(index); cached = getSelfFromParent(jo); });
	}
}