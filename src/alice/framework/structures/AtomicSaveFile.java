package alice.framework.structures;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import alice.framework.utilities.FileIO;

public class AtomicSaveFile extends JSONObject {
		 
	private String saveFileName;
	private Lock saveFileLock = null;
	
	public AtomicSaveFile(String saveFileName) {
		super(FileIO.readFromFile(saveFileName, "{}"));
		this.saveFileName = saveFileName;
		FileIO.writeToFile(saveFileName, toString());
	}

	@Override
	public JSONObject accumulate(String key, Object value) {
		if( saveFileLock == null ) {
			saveFileLock = new ReentrantLock();
		}
		saveFileLock.lock();
		super.accumulate(key, value);
		try {
			FileIO.writeToFile(saveFileName, toString());
		} catch (NullPointerException e) {}
		saveFileLock.unlock();
		return this;
	}

	@Override
	public JSONObject append(String key, Object value) {
		if( saveFileLock == null ) {
			saveFileLock = new ReentrantLock();
		}
		saveFileLock.lock();
		super.append(key, value);
		try {
			FileIO.writeToFile(saveFileName, toString());
		} catch (NullPointerException e) {}
		saveFileLock.unlock();
		return this;
	}
	
	@Override
	public JSONObject increment(String key) {
		if( saveFileLock == null ) {
			saveFileLock = new ReentrantLock();
		}
		saveFileLock.lock();
		super.increment(key);
		try {
			FileIO.writeToFile(saveFileName, toString());
		} catch (NullPointerException e) {}
		saveFileLock.unlock();
		return this;
	}
	
	public JSONArray optJSONArray(String key, JSONArray defaultValue) {
		try {
			return super.getJSONArray(key);
		} catch (JSONException e) {
			return defaultValue;
		}
	}
	
	public JSONObject optJSONObject(String key, JSONObject defaultValue) {
		try {
			return super.getJSONObject(key);
		} catch (JSONException e) {
			return defaultValue;
		}
	}
	
	@Override
	public JSONObject put(String key, boolean value) {
		if( saveFileLock == null ) {
			saveFileLock = new ReentrantLock();
		}
		saveFileLock.lock();
		super.put(key, value);
		try {
			FileIO.writeToFile(saveFileName, toString());
		} catch (NullPointerException e) {}
		saveFileLock.unlock();
		return this;
	}

	@Override
	public JSONObject put(String key, Collection<?> value) {
		if( saveFileLock == null ) {
			saveFileLock = new ReentrantLock();
		}
		saveFileLock.lock();
		super.put(key, value);
		try {
			FileIO.writeToFile(saveFileName, toString());
		} catch (NullPointerException e) {}
		saveFileLock.unlock();
		return this;
	}

	@Override
	public JSONObject put(String key, double value) {
		if( saveFileLock == null ) {
			saveFileLock = new ReentrantLock();
		}
		saveFileLock.lock();
		super.put(key, value);
		try {
			FileIO.writeToFile(saveFileName, toString());
		} catch (NullPointerException e) {}
		saveFileLock.unlock();
		return this;
	}

	@Override
	public JSONObject put(String key, float value) {
		if( saveFileLock == null ) {
			saveFileLock = new ReentrantLock();
		}
		saveFileLock.lock();
		super.put(key, value);
		try {
			FileIO.writeToFile(saveFileName, toString());
		} catch (NullPointerException e) {}
		saveFileLock.unlock();
		return this;
	}
	
	@Override
	public JSONObject put(String key, int value) {
		if( saveFileLock == null ) {
			saveFileLock = new ReentrantLock();
		}
		saveFileLock.lock();
		super.put(key, value);
		try {
			FileIO.writeToFile(saveFileName, toString());
		} catch (NullPointerException e) {}
		saveFileLock.unlock();
		return this;
	}
	
	@Override
	public JSONObject put(String key, long value) {
		if( saveFileLock == null ) {
			saveFileLock = new ReentrantLock();
		}
		saveFileLock.lock();
		super.put(key, value);
		try {
			FileIO.writeToFile(saveFileName, toString());
		} catch (NullPointerException e) {}
		saveFileLock.unlock();
		return this;
	}
	
	@Override
	public JSONObject put(String key, Map<?, ?> value) {
		if( saveFileLock == null ) {
			saveFileLock = new ReentrantLock();
		}
		saveFileLock.lock();
		super.put(key, value);
		try {
			FileIO.writeToFile(saveFileName, toString());
		} catch (NullPointerException e) {}
		saveFileLock.unlock();
		return this;
	}
	
	@Override
	public JSONObject put(String key, Object value) {
		if( saveFileLock == null ) {
			saveFileLock = new ReentrantLock();
		}
		saveFileLock.lock();
		super.put(key, value);
		try {
			FileIO.writeToFile(saveFileName, toString());
		} catch (NullPointerException e) {}
		saveFileLock.unlock();
		return this;
	}
	
	@Override
	public JSONObject putOnce(String key, Object value) {
		if( saveFileLock == null ) {
			saveFileLock = new ReentrantLock();
		}
		saveFileLock.lock();
		super.putOnce(key, value);
		try {
			FileIO.writeToFile(saveFileName, toString());
		} catch (NullPointerException e) {}
		saveFileLock.unlock();
		return this;
	}
	
	@Override
	public JSONObject putOpt(String key, Object value) {
		if( saveFileLock == null ) {
			saveFileLock = new ReentrantLock();
		}
		saveFileLock.lock();
		super.putOpt(key, value);
		try {
			FileIO.writeToFile(saveFileName, toString());
		} catch (NullPointerException e) {}
		saveFileLock.unlock();
		return this;
	}
	
	@Override
	public JSONObject remove(String key) {
		if( saveFileLock == null ) {
			saveFileLock = new ReentrantLock();
		}
		saveFileLock.lock();
		super.remove(key);
		try {
			FileIO.writeToFile(saveFileName, toString());
		} catch (NullPointerException e) {}
		saveFileLock.unlock();
		return this;
	}
	
	public Object modify(String key, Consumer<Object> modification) {
		if( saveFileLock == null ) {
			saveFileLock = new ReentrantLock();
		}
		saveFileLock.lock();
		modification.accept(get(key));
		try {
			FileIO.writeToFile(saveFileName, toString());
		} catch (NullPointerException e) {}
		saveFileLock.unlock();
		return get(key);
	}
	
	public BigDecimal modifyBigDecimal(String key, Consumer<BigDecimal> modification) {
		if( saveFileLock == null ) {
			saveFileLock = new ReentrantLock();
		}
		saveFileLock.lock();
		modification.accept(getBigDecimal(key));
		try {
			FileIO.writeToFile(saveFileName, toString());
		} catch (NullPointerException e) {}
		saveFileLock.unlock();
		return getBigDecimal(key);
	}
	public BigInteger modifyBigInteger(String key, Consumer<BigInteger> modification) {
		if( saveFileLock == null ) {
			saveFileLock = new ReentrantLock();
		}
		saveFileLock.lock();
		modification.accept(getBigInteger(key));
		try {
			FileIO.writeToFile(saveFileName, toString());
		} catch (NullPointerException e) {}
		saveFileLock.unlock();
		return getBigInteger(key);
	}
	public boolean modifyBoolean(String key, Consumer<Boolean> modification) {
		if( saveFileLock == null ) {
			saveFileLock = new ReentrantLock();
		}
		saveFileLock.lock();
		modification.accept(getBoolean(key));
		try {
			FileIO.writeToFile(saveFileName, toString());
		} catch (NullPointerException e) {}
		saveFileLock.unlock();
		return getBoolean(key);
	}
	
	public double modifyDouble(String key, Consumer<Double> modification) {
		if( saveFileLock == null ) {
			saveFileLock = new ReentrantLock();
		}
		saveFileLock.lock();
		modification.accept(getDouble(key));
		try {
			FileIO.writeToFile(saveFileName, toString());
		} catch (NullPointerException e) {}
		saveFileLock.unlock();
		return getDouble(key);
	}
	
	public float modifyFloat(String key, Consumer<Float> modification) {
		if( saveFileLock == null ) {
			saveFileLock = new ReentrantLock();
		}
		saveFileLock.lock();
		modification.accept(getFloat(key));
		try {
			FileIO.writeToFile(saveFileName, toString());
		} catch (NullPointerException e) {}
		saveFileLock.unlock();
		return getFloat(key);
	}
	
	public int modifyInt(String key, Consumer<Integer> modification) {
		if( saveFileLock == null ) {
			saveFileLock = new ReentrantLock();
		}
		saveFileLock.lock();
		modification.accept(getInt(key));
		try {
			FileIO.writeToFile(saveFileName, toString());
		} catch (NullPointerException e) {}
		saveFileLock.unlock();
		return getInt(key);
	}
	
	public JSONArray modifyJSONArray(String key, Consumer<JSONArray> modification) {
		if( saveFileLock == null ) {
			saveFileLock = new ReentrantLock();
		}
		saveFileLock.lock();
		modification.accept(getJSONArray(key));
		try {
			FileIO.writeToFile(saveFileName, toString());
		} catch (NullPointerException e) {}
		saveFileLock.unlock();
		return getJSONArray(key);
	}
	
	public JSONObject modifyJSONObject(String key, Consumer<JSONObject> modification) {
		if( saveFileLock == null ) {
			saveFileLock = new ReentrantLock();
		}
		saveFileLock.lock();
		modification.accept(getJSONObject(key));
		try {
			FileIO.writeToFile(saveFileName, toString());
		} catch (NullPointerException e) {}
		saveFileLock.unlock();
		return getJSONObject(key);
	}
	
	public long modifyLong(String key, Consumer<Long> modification) {
		if( saveFileLock == null ) {
			saveFileLock = new ReentrantLock();
		}
		saveFileLock.lock();
		modification.accept(getLong(key));
		try {
			FileIO.writeToFile(saveFileName, toString());
		} catch (NullPointerException e) {}
		saveFileLock.unlock();
		return getLong(key);
	}
	
	public Number modifyNumber(String key, Consumer<Number> modification) {
		if( saveFileLock == null ) {
			saveFileLock = new ReentrantLock();
		}
		saveFileLock.lock();
		modification.accept(getNumber(key));
		try {
			FileIO.writeToFile(saveFileName, toString());
		} catch (NullPointerException e) {}
		saveFileLock.unlock();
		return getNumber(key);
	}

	public String modifyString(String key, Consumer<String> modification) {
		if( saveFileLock == null ) {
			saveFileLock = new ReentrantLock();
		}
		saveFileLock.lock();
		modification.accept(getString(key));
		try {
			FileIO.writeToFile(saveFileName, toString());
		} catch (NullPointerException e) {}
		saveFileLock.unlock();
		return getString(key);
	}
	
}
