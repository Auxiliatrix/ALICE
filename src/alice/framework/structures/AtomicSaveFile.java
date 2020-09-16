package alice.framework.structures;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

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
	
}
