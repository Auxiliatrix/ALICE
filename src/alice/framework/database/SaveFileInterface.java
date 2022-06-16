package alice.framework.database;

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
import org.json.JSONPointer;

import alice.framework.database.SFISyncProxy.RecursiveLock;
import alice.framework.database.SFISyncProxy.RedirectType;
import alice.framework.database.SFISyncProxy.Redirects;
import alice.framework.database.SFISyncProxy.ReferenceLock;
//import alice.framework.database.SynchronizedSaveFileProxy.RestrictLock;
import alice.framework.database.SFISyncProxy.ReturnsSelf;
import alice.framework.database.SFISyncProxy.WriteLock;

public interface SaveFileInterface {

//	@RestrictLock
//    public SaveFileInterface accumulate(String key, Object value) throws JSONException;
//	@RestrictLock
//	public SaveFileInterface append(String key, Object value) throws JSONException;
	public Object get(String key) throws JSONException;
	public <E extends Enum<E>> E getEnum(Class<E> clazz, String key) throws JSONException;
	public boolean getBoolean(String key) throws JSONException;
    public BigInteger getBigInteger(String key) throws JSONException;
    public BigDecimal getBigDecimal(String key) throws JSONException;
    public double getDouble(String key) throws JSONException;
    public float getFloat(String key) throws JSONException;
    public Number getNumber(String key) throws JSONException;
    public int getInt(String key) throws JSONException;
//    @RestrictLock
//    public JSONArray getJSONArray(String key) throws JSONException;
    @RecursiveLock
    public SaveFileInterface getJSONObject(String key) throws JSONException;
    public long getLong(String key) throws JSONException;
    public String getString(String key) throws JSONException;
    public boolean has(String key);
	@WriteLock
	@ReturnsSelf
    public SaveFileInterface increment(String key) throws JSONException;
    public boolean isNull(String key);
    @ReferenceLock
    public Iterator<String> keys();
    @ReferenceLock
    public Set<String> keySet();
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
//    @RestrictLock
//    public JSONArray optJSONArray(String key);
    public JSONObject optJSONObject(String key);
    public long optLong(String key);
    public long optLong(String key, long defaultValue);
    public Number optNumber(String key);
    public Number optNumber(String key, Number defaultValue);
    public String optString(String key);
    public String optString(String key, String defaultValue);
	@WriteLock
	@ReturnsSelf
    public SaveFileInterface put(String key, boolean value) throws JSONException;
	@WriteLock
	@ReturnsSelf
    public SaveFileInterface put(String key, Collection<?> value) throws JSONException;
	@WriteLock
	@ReturnsSelf
	public SaveFileInterface put(String key, double value) throws JSONException;
	@WriteLock
	@ReturnsSelf
	public SaveFileInterface put(String key, float value) throws JSONException;
	@WriteLock
	@ReturnsSelf
    public SaveFileInterface put(String key, int value) throws JSONException;
	@WriteLock
	@ReturnsSelf
    public SaveFileInterface put(String key, long value) throws JSONException;
	@WriteLock
	@ReturnsSelf
    public SaveFileInterface put(String key, Map<?, ?> value) throws JSONException;
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
    public Object remove(String key);
    public boolean similar(Object other);
//    @RestrictLock
//    public JSONArray toJSONArray(JSONArray names) throws JSONException;
    public String toString();
    public String toString(int indentFactor) throws JSONException;
    public Writer write(Writer writer) throws JSONException;
    public Writer write(Writer writer, int indentFactor, int indent);
    public Map<String, Object> toMap();
    
    @ReturnsSelf
    @Redirects(type=RedirectType.putJSONObject)
    public default void putJSONObject(String key) throws JSONException {}
    
}
