package alice.framework.database;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;

import alice.framework.utilities.FileIO;
import alice.framework.utilities.ReadWriteReentrantLock;
import alina.utilities.WrappingProxy;

public class SaveSyncProxy extends WrappingProxy {

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	public @interface WriteLock {}
	
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	public @interface RecursiveLock {}
	
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	public @interface ReturnsSelf {}
	
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	public @interface Redirects {
		RedirectType type();
	}
	
	protected enum RedirectType {
		SFIputJSONObject,
		SFIputJSONArray,
		SFIputObject,
		SFIkeys,
		SFIkeySet, 
		SAIputJSONObject,
		SAIputJSONArray,
		SAIputObject,
		SAIappendObject,
		SAIiterator,
		SAItoList,
	};
	
	protected static Map<String, ReadWriteReentrantLock> lockMap = new HashMap<String, ReadWriteReentrantLock>();
	protected static Map<String, SyncedJSONObject> cache = new HashMap<String, SyncedJSONObject>();
	
	protected Map<JSONObject, SyncedJSONObject> recursiveCache;
	protected Map<JSONArray, SyncedJSONArray> recursiveArrayCache;
	
	protected String key;
	
	protected SaveSyncProxy(String key, JSONObject target) {
		super(target);
		this.key = key;
		recursiveCache = new HashMap<JSONObject, SyncedJSONObject>();
		recursiveArrayCache = new HashMap<JSONArray, SyncedJSONArray>();
	}
	
	protected SaveSyncProxy(String key, JSONArray target) {
		super(target);
		this.key = key;
		recursiveCache = new HashMap<JSONObject, SyncedJSONObject>();
		recursiveArrayCache = new HashMap<JSONArray, SyncedJSONArray>();
	}
	
	@Override
	public InvocationPair preprocess(Object proxy, Method method, Object[] args) {
		if( recursions == 1 ) {
			if( method.isAnnotationPresent(WriteLock.class) ) {
				lockWriter(key);
			} else {
				lockReader(key);
			}
			
			if( method.isAnnotationPresent(Redirects.class) ) {
				switch (method.getAnnotation(Redirects.class).type()) {
					case SFIputJSONObject:
						method = methods.get("put(java.lang.Stringjava.lang.Object)");
						args = new Object[] {
								args[0],
								new JSONObject()
						};
						break;
					case SFIputJSONArray:
						method = methods.get("put(java.lang.Stringjava.lang.Object)");
						args = new Object[] {
								args[0],
								new JSONArray()
						};
						break;
					case SFIputObject:
						method = methods.get("put(java.lang.Stringjava.lang.Object)");
						break;
					case SFIkeys:
						method = methods.get("keySet()");
						args = new Object[] {};
						break;
					case SFIkeySet:
						method = methods.get("keySet()");
						args = new Object[] {};
						break;
					case SAIputJSONObject:
						method = methods.get("put(intjava.lang.Object)");
						args = new Object[] {
								args[0],
								new JSONObject()
						};
						break;
					case SAIputJSONArray:
						method = methods.get("put(intjava.lang.Object)");
						args = new Object[] {
								args[0],
								new JSONArray()
						};
						break;
					case SAIputObject:
						method = methods.get("put(intjava.lang.Object)");
						break;
					case SAIappendObject:
						method = methods.get("put(java.lang.Object)");
						break;
					case SAIiterator:
						method = methods.get("toList()");
						break;
					case SAItoList:
						method = methods.get("toList()");
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
				if( method.getReturnType().equals(SyncedJSONObject.class) ) {
					return recursiveCache.get(target);
				} else if( method.getReturnType().equals(SyncedJSONArray.class) ) {
					return recursiveArrayCache.get(target);
				}
				return proxy;
			}
			
			if( method.isAnnotationPresent(RecursiveLock.class) ) {
				if( method.getReturnType().equals(SyncedJSONObject.class) ) {
					JSONObject intermediary = (JSONObject) result;
					if( recursiveCache.containsKey(intermediary) ) {
						return recursiveCache.get(intermediary);
					} else {
						SaveSyncProxy intermediaryProxy = new SaveSyncProxy(key, intermediary);
						result = (SyncedJSONObject) Proxy.newProxyInstance(SaveSyncProxy.class.getClassLoader(), new Class[] {SyncedJSONObject.class}, intermediaryProxy);
						intermediaryProxy.recursiveCache.put(intermediary, (SyncedJSONObject) result);
						recursiveCache.put(intermediary, (SyncedJSONObject) result);
					}
				} else if( method.getReturnType().equals(SyncedJSONArray.class) ) {
					JSONArray intermediary = (JSONArray) result;
					if( recursiveArrayCache.containsKey(intermediary) ) {
						return recursiveArrayCache.get(intermediary);
					} else {
						SaveSyncProxy intermediaryProxy = new SaveSyncProxy(key, intermediary);
						result = (SyncedJSONArray) Proxy.newProxyInstance(SaveSyncProxy.class.getClassLoader(), new Class[] {SyncedJSONArray.class}, intermediaryProxy);
						intermediaryProxy.recursiveArrayCache.put(intermediary, (SyncedJSONArray) result);
						recursiveArrayCache.put(intermediary, (SyncedJSONArray) result);
					}
				} else {
				}
			}
			
			if( method.isAnnotationPresent(Redirects.class) ) {
				switch (method.getAnnotation(Redirects.class).type()) {
					case SFIkeys:
						result = new HashSet<String>((Set<String>) result).iterator();
						break;
					case SFIkeySet:
						result = new HashSet<String>((Set<String>) result);
						break;
					case SAIiterator:
						result = new ArrayList<Object>((List<Object>) result).iterator();
						break;
					case SAItoList:
						result = new ArrayList<Object>((List<Object>) result);
						break;
					default:
						break;
				}
			}
		}
		return result;
	}
	
	protected static void lockReader(String key) {
		lockMap.get(key).lockReader();
	}

	protected static void unlockReader(String key) {
		lockMap.get(key).unlockReader();
	}
	
	protected static void lockWriter(String key) {
		lockMap.get(key).lockWriter();
	}
	
	protected static void unlockWriter(String key) {
		FileIO.writeToFile(key, cache.get(key).toString(1));
		lockMap.get(key).unlockWriter();
	}
}
