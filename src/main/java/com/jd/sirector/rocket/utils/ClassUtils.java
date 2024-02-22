package com.jd.sirector.rocket.utils;

import com.jd.sirector.rocket.annotation.Handler;
import javassist.ClassClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.LoaderClassPath;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;

public class ClassUtils {
    private static final ClassPool CLASS_POOL = ClassPool.getDefault();

    public ClassUtils() {
    }

    public static ClassPool getClassPool() {
        CLASS_POOL.appendClassPath(new LoaderClassPath(Thread.currentThread().getContextClassLoader()));
        return CLASS_POOL;
    }

    public static CtClass getObject() {
        try {
            return CLASS_POOL.get("java.lang.Object");
        } catch (Exception var1) {
            var1.printStackTrace();
            return null;
        }
    }

    public static CtClass get(String name) {
        try {
            return CLASS_POOL.get(name);
        } catch (Exception var2) {
            var2.printStackTrace();
            return null;
        }
    }

    public static String methodName(Method method){
        Handler handler = method.getAnnotation(Handler.class);
        return handler == null ? method.getName() : StringUtils.isEmpty(handler.alias()) ? method.getName() : handler.alias() ;
    }

    public static String firstParameterType(Method method){
        return method.getParameterTypes()[0].getName();
    }

    public static void registerClassPath(Class clazz) {
        getClassPool().insertClassPath(new ClassClassPath(clazz));
    }

    public static String generateClassName(String pre, String proxyClassName) {
        return pre + "$" + proxyClassName;
    }

    public static String generateEventClassName(String proxyClassName) {
        return generateClassName("com.jd.m.sirector.rocket.generated.Event", proxyClassName);
    }

    public static String generateEventExecuteClassName(String proxyClassName) {
        return generateClassName("com.jd.m.sirector.rocket.generated.EventExecute", proxyClassName);
    }

    public static String generateEventHandlerClassName(String proxyClassName, String proxyMethodName) {
        return "com.jd.m.sirector.rocket.generated.EventHandler$" + proxyClassName + "$" + proxyMethodName;
    }
}
