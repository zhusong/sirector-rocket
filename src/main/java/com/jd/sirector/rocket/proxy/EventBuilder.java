package com.jd.sirector.rocket.proxy;

import com.jd.sirector.EventHandler;
import com.jd.sirector.rocket.utils.ByteCodeUtils;
import com.jd.sirector.rocket.utils.ClassUtils;
import javassist.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * <p></p>
 *
 *
 * @author : zhusong
 * @date : 2020-05-25 20:08
 **/
public class EventBuilder {
    private static final EventBuilder builder = new EventBuilder();
    private boolean debug;
    private String path;

    public EventBuilder() { }

    public static EventBuilder builder(boolean debug, String path, ClassLoader loader) {
        builder.setDebug(debug);
        builder.setPath(path);
        return builder;
    }

    public void build(List<Method> handlers, AbstractArranger.Entries entries, AbstractArranger abstractArranger) {
        String eventSimpleName = abstractArranger.getClass().getSimpleName();
        ClassPool cp = ClassUtils.getClassPool();

        String eventClassname = ClassUtils.generateEventClassName(eventSimpleName);
        CtClass cc = cp.makeClass(eventClassname);

        try {
            cc.setSuperclass(cp.get("com.jd.sirector.rocket.proxy.Event"));
            cc.addConstructor(CtNewConstructor.defaultConstructor(cc));
            ByteCodeUtils.buildFieldGetSet(cc, "param", Map.class.getName());
            this.buildFieldGetterSetter(cc, entries, handlers, abstractArranger.methodsMapping);
            ByteCodeUtils.outputClass(cc, this.debug, this.path);

        } catch (Exception e) {
            throw new RuntimeException("Build event bean exception in EventBuilder", e);
        }
    }

    private void buildFieldGetterSetter(CtClass cc, AbstractArranger.Entries entries, List<Method> handlers, Map<String, Method> hm) throws CannotCompileException {

        if (entries != null) {
            for(AbstractArranger.Entry entry : entries){
                if(entry.key.equals("begin") || entry.key.equals("then")){
                    String[] hs = entry.handlers;
                    for(String h : hs){
                        if("endSentinel".equals(h)){
                            ByteCodeUtils.buildFieldGetSet(cc, "endSentinel", "java.lang.Object");
                            continue;
                        }
                        Method method = hm.get(h);
                        ByteCodeUtils.buildFieldGetSet(cc, method.getName(), method.getReturnType().getName());
                    }
                }
            }
        }
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
