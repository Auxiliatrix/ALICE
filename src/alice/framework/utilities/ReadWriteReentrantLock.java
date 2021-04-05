package alice.framework.utilities;

import java.util.concurrent.locks.ReentrantLock;

public class ReadWriteReentrantLock {
	
	private ReentrantLock writerLock;
	private ReentrantLock readerLock;
	private int readers;

	public ReadWriteReentrantLock(boolean fair) {
		this.writerLock = new ReentrantLock(fair);
		this.readerLock = new ReentrantLock(fair);
		this.readers = 0;
	}
	
	public void lockWriter() {
		writerLock.lock();
	}
	
	public void unlockWriter() {
		writerLock.unlock();
	}
	
	public void lockReader() {
		readerLock.lock();
		readers++;
		if( readers == 1 ) {
			lockWriter();
		}
		readerLock.unlock();
	}
	
	public void unlockReader() {
		readerLock.lock();
		readers--;
		if( readers == 0 ) {
			unlockWriter();
		}
		readerLock.unlock();
	}
	
}
