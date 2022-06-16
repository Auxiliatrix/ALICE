package alice.framework.database;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.json.JSONObject;

import alice.framework.main.Constants;
import alice.framework.utilities.AliceLogger;
import alice.framework.utilities.FileIO;
import alice.framework.utilities.ReadWriteReentrantLock;
import alina.utilities.WrappingProxy;

public class SFISyncProxy extends WrappingProxy {

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	public @interface WriteLock {}
	
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	public @interface RecursiveLock {}
	
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	public @interface ReferenceLock {}
	
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	public @interface ReturnsSelf {}
	
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	public @interface Redirects {
		RedirectType type();
	}
	
	protected enum RedirectType {
		putJSONObject,
	};
	
	protected static Map<String, ReadWriteReentrantLock> lockMap = new HashMap<String, ReadWriteReentrantLock>();
	protected static Map<String, SaveFileInterface> cache = new HashMap<String, SaveFileInterface>();
	
	protected Map<String, SaveFileInterface> recursiveCache;
	
	protected String key;
	
	public static SaveFileInterface of(String key) {
		if( !lockMap.containsKey(key) ) {
			AliceLogger.info(String.format("Loaded guild data from %s.", key), 1);
			lockMap.put(key, new ReadWriteReentrantLock(true));
		}
		
		if( cache.containsKey(key) ) {
			return cache.get(key);
		} else {
			SaveFileInterface sfi = (SaveFileInterface) Proxy.newProxyInstance(
					SFISyncProxy.class.getClassLoader(), 
					new Class[] {SaveFileInterface.class}, 
					new SFISyncProxy(key, new JSONObject(FileIO.readFromFile(key, Constants.DEFAULT_GUILD_DATA))));
			cache.put(key, sfi);
			return sfi;
		}
	}
	
	protected SFISyncProxy(String key, JSONObject target) {
		super(target);
		this.key = key;
		recursiveCache = new HashMap<String, SaveFileInterface>();
	}
	
	@Override
	public InvocationPair preprocess(Object proxy, Method method, Object[] args) {
		System.out.println("Recursion Level: " + recursions);
		System.out.println(method.getName());
		if( recursions == 1 ) {
			if( method.isAnnotationPresent(WriteLock.class) ) {
				lockWriter(key);
			} else {
				lockReader(key);
			}
			
			if( method.isAnnotationPresent(ReferenceLock.class) ) {
				System.out.println("	Dereferenced; disguising.");
				if( method.getReturnType().getClass().equals(Iterator.class) ) {
					method = methods.get("keySet");
				}
			}
			
			if( method.isAnnotationPresent(Redirects.class) ) {
				System.out.println("	Redirected; redirecting.");
				switch (method.getAnnotation(Redirects.class).type()) {
					case putJSONObject:
						method = methods.get("put(java.lang.Stringjava.lang.Object)");
						args = new Object[] {
								args[0],
								new JSONObject()
						};
						break;
					default:
						break;
				
				}
			}
		}
		return new InvocationPair(method, args);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object postprocess(Object proxy, Method method, Object[] args, Object result) {
		if( recursions == 1 ) {
			if( method.isAnnotationPresent(WriteLock.class) ) {
				unlockWriter(key);
			} else {
				unlockReader(key);
			}
			
			if( method.isAnnotationPresent(ReturnsSelf.class) ) {
				System.out.println("	Returning self.");
				return proxy;
			}
			
			if( method.isAnnotationPresent(RecursiveLock.class) ) {
				System.out.println("	Recursive copy generated.");
				if( recursiveCache.containsKey(args[0].toString()) ) {
					return recursiveCache.get(args[0].toString());
				} else {
					result = (SaveFileInterface) Proxy.newProxyInstance(SFISyncProxy.class.getClassLoader(), new Class[] {SaveFileInterface.class}, new SFISyncProxy(key, (JSONObject) result));
					recursiveCache.put(args[0].toString(), (SaveFileInterface) result);
				}
			} else if( method.isAnnotationPresent(ReferenceLock.class) ) {
				System.out.println("	Dereferenced; resolved.");
				if( method.getReturnType().getClass().equals(Iterator.class) ) {
					result = new HashSet<String>((Set<String>) result).iterator();
				} else if( method.getReturnType().getClass().equals(Set.class) ) {
					result = new HashSet<String>((Set<String>) result);
				}
			}
		}
		return result;
	}
	
	protected static void lockReader(String key) {
		System.out.println("		" + key + " Reader Locked");
		lockMap.get(key).lockReader();
	}

	protected static void unlockReader(String key) {
		System.out.println("		" + key + " Reader Unlocked");
		lockMap.get(key).unlockReader();
	}
	
	protected static void lockWriter(String key) {
		System.out.println("		" + key + " Writer Locked");
		lockMap.get(key).lockWriter();
	}
	
	protected static void unlockWriter(String key) {
		System.out.println("		" + key + " Writer Unlocked");
		FileIO.writeToFile(key, cache.get(key).toString(1));
		lockMap.get(key).unlockWriter();
	}
}
