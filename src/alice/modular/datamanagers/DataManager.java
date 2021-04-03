package alice.modular.datamanagers;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class DataManager {
	
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	public static @interface Exclusive {
		String group() default "Default";
	}
	
	protected final static Lock DATA_MANAGER_LOCK = new ReentrantLock();
	
}
