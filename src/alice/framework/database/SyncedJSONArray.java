package alice.framework.database;

import java.io.Writer;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import alice.framework.database.SaveSyncProxy.RecursiveLock;
import alice.framework.database.SaveSyncProxy.RedirectType;
import alice.framework.database.SaveSyncProxy.Redirects;
import alice.framework.database.SaveSyncProxy.ReturnsSelf;
import alice.framework.database.SaveSyncProxy.WriteLock;

public interface SyncedJSONArray {

    public Object get(int index) throws JSONException;
    public boolean getBoolean(int index) throws JSONException;
    public double getDouble(int index) throws JSONException;
    public float getFloat(int index) throws JSONException;
    public Number getNumber(int index) throws JSONException;
    public <E extends Enum<E>> E getEnum(Class<E> clazz, int index) throws JSONException;
    public BigDecimal getBigDecimal (int index) throws JSONException;
    public BigInteger getBigInteger (int index) throws JSONException;
    public int getInt(int index) throws JSONException;
    @RecursiveLock
    public SyncedJSONArray getJSONArray(int index) throws JSONException;
    @RecursiveLock
    public SyncedJSONObject getJSONObject(int index) throws JSONException;
    public long getLong(int index) throws JSONException;
    public String getString(int index) throws JSONException;
    public boolean isNull(int index);
    public String join(String separator) throws JSONException;
    public int length();
    public Object opt(int index);
    public boolean optBoolean(int index);
    public boolean optBoolean(int index, boolean defaultValue);
    public double optDouble(int index);
    public double optDouble(int index, double defaultValue);
    public float optFloat(int index);
    public float optFloat(int index, float defaultValue);
    public int optInt(int index);
    public int optInt(int index, int defaultValue);
    public <E extends Enum<E>> E optEnum(Class<E> clazz, int index);
    public <E extends Enum<E>> E optEnum(Class<E> clazz, int index, E defaultValue);
    public BigInteger optBigInteger(int index, BigInteger defaultValue);
    public BigDecimal optBigDecimal(int index, BigDecimal defaultValue);
    public JSONArray optJSONArray(int index);
    public JSONObject optJSONObject(int index);
    public long optLong(int index);
    public long optLong(int index, long defaultValue);
    public Number optNumber(int index);
    public Number optNumber(int index, Number defaultValue);
    public String optString(int index);
    public String optString(int index, String defaultValue);
    @ReturnsSelf
    @WriteLock
    public JSONArray put(boolean value);
    @ReturnsSelf
    @WriteLock
    public JSONArray put(Collection<?> value);
    @ReturnsSelf
    @WriteLock
    public JSONArray put(double value) throws JSONException;
    @ReturnsSelf
    @WriteLock
    public JSONArray put(float value) throws JSONException;
    @ReturnsSelf
    @WriteLock
    public JSONArray put(int value);
    @ReturnsSelf
    @WriteLock
    public JSONArray put(long value);
    @ReturnsSelf
    @WriteLock
    public JSONArray put(Map<?, ?> value);
//    @ReturnsSelf
//    @WriteLock
//    public JSONArray put(Object value);
    @ReturnsSelf
    @WriteLock
    public JSONArray put(int index, boolean value) throws JSONException;
    @ReturnsSelf
    @WriteLock
    public JSONArray put(int index, Collection<?> value) throws JSONException;
    @ReturnsSelf
    @WriteLock
    public JSONArray put(int index, double value) throws JSONException;
    @ReturnsSelf
    @WriteLock
    public JSONArray put(int index, float value) throws JSONException;
    @ReturnsSelf
    @WriteLock
    public JSONArray put(int index, int value) throws JSONException;
    @ReturnsSelf
    @WriteLock
    public JSONArray put(int index, long value) throws JSONException;
    @ReturnsSelf
    @WriteLock
    public JSONArray put(int index, Map<?, ?> value) throws JSONException;
//    @WriteLock
//    public JSONArray put(int index, Object value) throws JSONException;
//    public Object query(String jsonPointer);
//    public Object query(JSONPointer jsonPointer);
    @WriteLock
    public Object remove(int index);
    public boolean similar(Object other);
    
    public String toString();
    public String toString(int indentFactor) throws JSONException;
    public Writer write(Writer writer) throws JSONException;
    public Writer write(Writer writer, int indentFactor, int indent);
    public boolean isEmpty();

    @Redirects(type=RedirectType.SAItoList)
    public default List<Object> toList() {
    	return null;
    }
    @Redirects(type=RedirectType.SAIiterator)
    public default Iterator<Object> iterator() {
    	return null;
    }
    @ReturnsSelf
    @Redirects(type=RedirectType.SAIputJSONObject)
    public default SyncedJSONArray putJSONObject(int index) throws JSONException {
    	return null;
    }
    @ReturnsSelf
    @Redirects(type=RedirectType.SAIputJSONArray)
    public default SyncedJSONArray putJSONArray(int index) throws JSONException {
    	return null;
    }
    
    @WriteLock
	@ReturnsSelf
	@Redirects(type=RedirectType.SAIputObject)
    public default SyncedJSONObject put(int index, String value) throws JSONException {
    	return null;
    }
    
    @WriteLock
	@ReturnsSelf
	@Redirects(type=RedirectType.SAIappendObject)
    public default SyncedJSONObject put(String value) throws JSONException {
    	return null;
    }
}
