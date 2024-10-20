package jvn;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class JvnProxy implements InvocationHandler {
    private final JvnObject jvnObj;

    public JvnProxy(JvnObject jvnObj) {
        this.jvnObj = jvnObj;
    }

    public JvnObject getJvnObj() {
        return jvnObj;
    }

    public static Object newInstance(JvnObject jvnObj) {
        try {
            return Proxy.newProxyInstance(
                    jvnObj.jvnGetSharedObject().getClass().getClassLoader(),
                    jvnObj.jvnGetSharedObject().getClass().getInterfaces(),
                    new JvnProxy(jvnObj)
            );
        } catch (Exception e) {
            return null;
        }
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (method.isAnnotationPresent(ReadOperation.class)) {
            jvnObj.jvnLockRead();
        } else if (method.isAnnotationPresent(WriteOperation.class)) {
            jvnObj.jvnLockWrite();
        }
        Object result = method.invoke(jvnObj.jvnGetSharedObject(), args);
        jvnObj.jvnUnLock();
        return result;
    }
}
