package alina.utilities;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public abstract class WrappingProxy implements InvocationHandler {
	
	protected final Map<String, Method> methods = new HashMap<String, Method>();

	protected Object target;
	
	protected int recursions;
	
	protected WrappingProxy(Object target) {
		this.target = target;
		
		for( Method method : target.getClass().getDeclaredMethods() ) {
			this.methods.put(getParameterizedName(method), method);
		}
		
		recursions = 0;
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		recursions++;
		InvocationPair ip = preprocess(proxy, method, args);
		Object result = null;
		try {
			if( ip != null ) {
				result = methods.get(getParameterizedName(ip.method)).invoke(target, ip.args);
			}
		} finally {
			result = postprocess(proxy, method, args, result);
		}
		recursions--;
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
