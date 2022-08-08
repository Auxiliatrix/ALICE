package alina.structures;

import java.io.Writer;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import alina.structures.SaveSyncProxy.Desynchronized;
import alina.structures.SaveSyncProxy.RecursiveLock;
import alina.structures.SaveSyncProxy.RedirectType;
import alina.structures.SaveSyncProxy.Redirects;
import alina.structures.SaveSyncProxy.ReturnsSelf;
import alina.structures.SaveSyncProxy.WriteLock;

public interface SyncedJSONObject {

	// TODO: guarantee() function that just checks !has then create
	
//	@WriteLock
//  public SaveFileInterface accumulate(String key, Object value) throws JSONException;
	@WriteLock
	public SyncedJSONObject append(String key, Object value) throws JSONException;
	public Object get(String key) throws JSONException;
	public <E extends Enum<E>> E getEnum(Class<E> clazz, String key) throws JSONException;
	public boolean getBoolean(String key) throws JSONException;
    public BigInteger getBigInteger(String key) throws JSONException;
    public BigDecimal getBigDecimal(String key) throws JSONException;
    public double getDouble(String key) throws JSONException;
    public float getFloat(String key) throws JSONException;
    public Number getNumber(String key) throws JSONException;
    public int getInt(String key) throws JSONException;
	@RecursiveLock
	public SyncedJSONArray getJSONArray(String key) throws JSONException;
    @RecursiveLock
    public SyncedJSONObject getJSONObject(String key) throws JSONException;
    public long getLong(String key) throws JSONException;
    public String getString(String key) throws JSONException;
    public boolean has(String key);
	@WriteLock
	@ReturnsSelf
    public SyncedJSONObject increment(String key) throws JSONException;
    public boolean isNull(String key);
    public int length();
    public boolean isEmpty();
    public JSONArray names();
    public Object opt(String key);
    public <E extends Enum<E>> E optEnum(Class<E> clazz, String key);
    public <E extends Enum<E>> E optEnum(Class<E> clazz, String key, E defaultValue);
    public boolean optBoolean(String key);
    public boolean optBoolean(String key, boolean defaultValue);
    public BigDecimal optBigDecimal(String key, BigDecimal defaultValue);
    public BigInteger optBigInteger(String key, BigInteger defaultValue);
    public double optDouble(String key);
    public double optDouble(String key, double defaultValue);
    public float optFloat(String key);
    public float optFloat(String key, float defaultValue);
    public int optInt(String key);
    public int optInt(String key, int defaultValue);
    public JSONArray optJSONArray(String key);
    public JSONObject optJSONObject(String key);
    public long optLong(String key);
    public long optLong(String key, long defaultValue);
    public Number optNumber(String key);
    public Number optNumber(String key, Number defaultValue);
    public String optString(String key);
    public String optString(String key, String defaultValue);
	@WriteLock
	@ReturnsSelf
    public SyncedJSONObject put(String key, boolean value) throws JSONException;
	@WriteLock
	@ReturnsSelf
    public SyncedJSONObject put(String key, Collection<?> value) throws JSONException;
	@WriteLock
	@ReturnsSelf
	public SyncedJSONObject put(String key, double value) throws JSONException;
	@WriteLock
	@ReturnsSelf
	public SyncedJSONObject put(String key, float value) throws JSONException;
	@WriteLock
	@ReturnsSelf
    public SyncedJSONObject put(String key, int value) throws JSONException;
	@WriteLock
	@ReturnsSelf
    public SyncedJSONObject put(String key, long value) throws JSONException;
	@WriteLock
	@ReturnsSelf
    public SyncedJSONObject put(String key, Map<?, ?> value) throws JSONException;
//	@RestrictLock
//    public SaveFileInterface put(String key, Object value) throws JSONException;
//	@RestrictLock
//    public SaveFileInterface putOnce(String key, Object value) throws JSONException;
//	@RestrictLock
//    public SaveFileInterface putOpt(String key, Object value) throws JSONException;
//	@RestrictLock
//    public Object query(String jsonPointer);
//	@RestrictLock
//	public Object query(JSONPointer jsonPointer);
//	@RestrictLock
//	public Object optQuery(String jsonPointer);
//	@RestrictLock
//	public Object optQuery(JSONPointer jsonPointer);
	@WriteLock
	// TODO: decouple when JSONObject or JSONArray is removed
    public Object remove(String key);
    public boolean similar(Object other);
    public String toString();
    public String toString(int indentFactor) throws JSONException;
    public Writer write(Writer writer) throws JSONException;
    public Writer write(Writer writer, int indentFactor, int indent);
    public Map<String, Object> toMap();
    
    @Desynchronized
    public default String desyncedToString() {
    	return null;
    }
    
    @Desynchronized
    public default String desyncedToString(int indentFactor) throws JSONException {
    	return null;
    }
    
    @WriteLock
    @ReturnsSelf
    @Redirects(type=RedirectType.SFIputJSONObject)
    public default SyncedJSONObject putJSONObject(String key) throws JSONException {
    	return null;
    }
    
    @WriteLock
    @ReturnsSelf
    @Redirects(type=RedirectType.SFIputJSONArray)
    public default SyncedJSONObject putJSONArray(String key) throws JSONException {
    	return null;
    }
    
    @Redirects(type=RedirectType.SFIkeys)
    public default Iterator<String> keys() {
    	return null;
    }
    
    @Redirects(type=RedirectType.SFIkeySet)
    public default Set<String> keySet() {
    	return null;
    }
    
    @WriteLock
	@ReturnsSelf
	@Redirects(type=RedirectType.SFIputObject)
    public default SyncedJSONObject put(String key, String value) throws JSONException {
    	return null;
    }
    
}
