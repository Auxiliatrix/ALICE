package alina.structures;

import java.util.concurrent.Semaphore;

public class ReadWriteReentrantLock {
	
	private Semaphore writerSemaphore;
	private Semaphore readerSemaphore;
//	
//	private ReentrantLock writerLock;
//	private ReentrantLock readerLock;
	
	private int readers;

	public ReadWriteReentrantLock() {
		this(false);
	}
	
	public ReadWriteReentrantLock(boolean fair) {
//		this.writerLock = new ReentrantLock(fair);
//		this.readerLock = new ReentrantLock(fair);
//		
		writerSemaphore = new Semaphore(1);
		readerSemaphore = new Semaphore(1);
		
		this.readers = 0;
	}
	
	public void lockWriter() {
		try {
			writerSemaphore.acquire();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
//		writerLock.lock();
	}
	
	public void unlockWriter() {
		writerSemaphore.release();
//		writerLock.unlock();
	}
	
	public void lockReader() {
		try {
			readerSemaphore.acquire();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
//		readerLock.lock();
		try {
			readers++;
			if( readers == 1 ) {
				try {
					writerSemaphore.acquire();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
//				writerLock.lock();
			}
		} finally {
			readerSemaphore.release();
//			readerLock.unlock();
		}
	}
	
	public void unlockReader() {
		try {
			readerSemaphore.acquire();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
//		readerLock.lock();
		try {
			readers--;
			if( readers == 0 ) {
				writerSemaphore.release();
//				unlockWriter();
			}
		} finally {
			readerSemaphore.release();
//			readerLock.unlock();
		}
	}
	
}
