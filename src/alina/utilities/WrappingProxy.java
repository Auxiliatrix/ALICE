package alina.utilities;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public abstract class WrappingProxy implements InvocationHandler {
	
	protected final Map<String, Method> methods = new HashMap<String, Method>();

	protected Object target;
	
	protected int recursions;
	
	protected Object lock;
		
	protected WrappingProxy(Object target) {
		this.target = target;

		for( Method method : target.getClass().getDeclaredMethods() ) {
			this.methods.put(getParameterizedName(method), method);
		}
		
		recursions = 0;
		
		this.lock = new Object();
		
	}

	// ERROR SEEMS TO BE CAUSED BY INVOCATION BEING CALLED TWICE
	
	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		InvocationPair ip = preprocess(proxy, method, args);
		Object result = null;
		synchronized(lock) {
			recursions++;
			try {
				if( ip != null ) {
					if( !methods.containsKey(getParameterizedName(ip.method)) ) {
						System.err.println("Missing method: " + getParameterizedName(ip.method));
					}
					result = methods.get(getParameterizedName(ip.method)).invoke(target, ip.args);
				}
			} finally {
				recursions--;
				result = postprocess(proxy, method, args, result);
			}
		}
		return result;
	}
	
	public abstract InvocationPair preprocess(Object proxy, Method method, Object[] args);
	
	public abstract Object postprocess(Object proxy, Method method, Object[] args, Object result);

	protected class InvocationPair {
		public Method method;
		public Object[] args;
		
		public InvocationPair(Method method, Object[] args) {
			this.method = method;
			this.args = args;
		}
	}
	
	public static String getParameterizedName(Method method) {
		StringBuilder uniqueKey = new StringBuilder();
		uniqueKey.append(method.getName());
		uniqueKey.append("(");
		for( Class<?> c : method.getParameterTypes() ) {
			uniqueKey.append(c.getName());
		}
		uniqueKey.append(")");
		return uniqueKey.toString();
	}
	
}
