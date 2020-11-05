package alice.framework.structures;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

@SuppressWarnings("serial")
public class AtomicStringMap<E> extends HashMap<String, E> {
	
	private Lock saveFilesLock;
	
	public AtomicStringMap() {
		this.saveFilesLock = new ReentrantLock();
	}

	public void clear() {
		saveFilesLock.lock();
		super.clear();
		saveFilesLock.unlock();
	}

	public void forEach(BiConsumer<? super String, ? super E> arg0) {
		saveFilesLock.lock();
		super.forEach(arg0);
		saveFilesLock.unlock();
	}
	
	public E merge(String key, E value, BiFunction<? super E, ? super E, ? extends E> remappingFunction) {
		saveFilesLock.lock();
		E ret = super.merge(key, value, remappingFunction);
		saveFilesLock.unlock();
		return ret;
	}
	
	public E put(String arg0, E arg1) {
		saveFilesLock.lock();
		E ret = super.put(arg0, arg1);
		saveFilesLock.unlock();
		return ret;
	}
	
	public void putAll(Map<? extends String, ? extends E> arg0) {
		saveFilesLock.lock();
		super.putAll(arg0);
		saveFilesLock.unlock();
	}
	
	public E putIfAbsent(String key, E value) {
		saveFilesLock.lock();
		E ret = super.putIfAbsent(key, value);
		saveFilesLock.unlock();
		return ret;
	}
	
	public E remove(Object arg0) {
		saveFilesLock.lock();
		E ret = super.remove(arg0);
		saveFilesLock.unlock();
		return ret;
	}
	
	public boolean remove(Object key, Object value) {
		saveFilesLock.lock();
		boolean ret = super.remove(key, value);
		saveFilesLock.unlock();
		return ret;
	}

	public E replace(String key, E value) {
		saveFilesLock.lock();
		E ret = super.replace(key, value);
		saveFilesLock.unlock();
		return ret;
	}
	
	public boolean replace(String key, E oldValue, E newValue) {
		saveFilesLock.lock();
		boolean ret = super.replace(key, oldValue, newValue);
		saveFilesLock.unlock();
		return ret;
	}
	
	public void replaceAll(BiFunction<? super String, ? super E, ? extends E> arg0) {
		saveFilesLock.lock();
		super.replaceAll(arg0);
		saveFilesLock.unlock();
	}
	
}
