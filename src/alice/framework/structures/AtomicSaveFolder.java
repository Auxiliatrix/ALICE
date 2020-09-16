package alice.framework.structures;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

@SuppressWarnings("serial")
public class AtomicSaveFolder extends HashMap<String, AtomicSaveFile> {
	
	private Lock saveFilesLock;
	
	public AtomicSaveFolder() {
		this.saveFilesLock = new ReentrantLock();
	}

	public void clear() {
		saveFilesLock.lock();
		super.clear();
		saveFilesLock.unlock();
	}

	public void forEach(BiConsumer<? super String, ? super AtomicSaveFile> arg0) {
		saveFilesLock.lock();
		super.forEach(arg0);
		saveFilesLock.unlock();
	}
	
	public AtomicSaveFile merge(String key, AtomicSaveFile value, BiFunction<? super AtomicSaveFile, ? super AtomicSaveFile, ? extends AtomicSaveFile> remappingFunction) {
		saveFilesLock.lock();
		AtomicSaveFile ret = super.merge(key, value, remappingFunction);
		saveFilesLock.unlock();
		return ret;
	}
	
	public AtomicSaveFile put(String arg0, AtomicSaveFile arg1) {
		saveFilesLock.lock();
		AtomicSaveFile ret = super.put(arg0, arg1);
		saveFilesLock.unlock();
		return ret;
	}
	
	public void putAll(Map<? extends String, ? extends AtomicSaveFile> arg0) {
		saveFilesLock.lock();
		super.putAll(arg0);
		saveFilesLock.unlock();
	}
	
	public AtomicSaveFile putIfAbsent(String key, AtomicSaveFile value) {
		saveFilesLock.lock();
		AtomicSaveFile ret = super.putIfAbsent(key, value);
		saveFilesLock.unlock();
		return ret;
	}
	
	public AtomicSaveFile remove(Object arg0) {
		saveFilesLock.lock();
		AtomicSaveFile ret = super.remove(arg0);
		saveFilesLock.unlock();
		return ret;
	}
	
	public boolean remove(Object key, Object value) {
		saveFilesLock.lock();
		boolean ret = super.remove(key, value);
		saveFilesLock.unlock();
		return ret;
	}

	public AtomicSaveFile replace(String key, AtomicSaveFile value) {
		saveFilesLock.lock();
		AtomicSaveFile ret = super.replace(key, value);
		saveFilesLock.unlock();
		return ret;
	}
	
	public boolean replace(String key, AtomicSaveFile oldValue, AtomicSaveFile newValue) {
		saveFilesLock.lock();
		boolean ret = super.replace(key, oldValue, newValue);
		saveFilesLock.unlock();
		return ret;
	}
	
	public void replaceAll(BiFunction<? super String, ? super AtomicSaveFile, ? extends AtomicSaveFile> arg0) {
		saveFilesLock.lock();
		super.replaceAll(arg0);
		saveFilesLock.unlock();
	}
	
}
