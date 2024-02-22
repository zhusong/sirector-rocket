package com.jd.sirector.rocket.proxy;

import com.jd.sirector.EventHandler;
import com.jd.sirector.rocket.annotation.Handler;
import com.jd.sirector.rocket.utils.ByteCodeUtils;
import com.jd.sirector.rocket.utils.ClassUtils;
import com.jd.sirector.rocket.utils.Utils;
import javassist.*;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.swing.text.html.parser.Entity;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

/**
 *
 * @author : zhusong
 * @date : 2020-05-24 22:51
 **/
public class EventHandlerBuilder {
    private static final EventHandlerBuilder builder = new EventHandlerBuilder();
    private boolean debug;
    private String path;
    private ClassLoader classLoader;

    EventHandlerBuilder() { }

    static EventHandlerBuilder builder(boolean debug, String path, ClassLoader classLoader) {
        builder.setDebug(debug);
        builder.setPath(path);
        builder.classLoader = classLoader;
        return builder;
    }

    private boolean extendsAllResults(AbstractArranger abstractArranger) {
        if (abstractArranger.eventExecutor != null) {
            return abstractArranger.eventExecutor.extendsAllResults();
        }
        if (abstractArranger.manyForOne != null) {
            return abstractArranger.manyForOne.extendsAllResults();
        }
        return false;
    }

    Map<String, EventHandler> build(List<Method> handlers, AbstractArranger.Entries entries, AbstractArranger arranger) {
        Map<String, EventHandler> eventHandlers = new HashMap<>(handlers.size());
        Map<String, List<Method>> links = parseHandlersLink(handlers, entries, arranger, arranger.methodsMapping);
        List<Method> usedHanlders = excluseUnuseHandlers(entries, arranger);
        for (Method handler : usedHanlders) {
            eventHanlderGenerate(eventHandlers, handler, links.get(ClassUtils.methodName(handler)), arranger);
        }
        return eventHandlers;
    }
    private List<Method> excluseUnuseHandlers(AbstractArranger.Entries entries, AbstractArranger arranger){
        List<Method> usedHandlers = new ArrayList<>();
        for(AbstractArranger.Entry entry : entries){
            if(entry.key.equals("begin") || entry.key.equals("then")) {
                for (String handler : entry.handlers) {
                    usedHandlers.add(arranger.methodsMapping.get(handler));
                }
            }
        }
        return usedHandlers;
    }

    public Map<String, List<Method>> parseHandlersLink(List<Method> handlers, AbstractArranger.Entries entries, Arranger arranger, Map<String, Method> hm) {

        List<Method> previous = new ArrayList<>(handlers.size());
        Map<String, List<Method>> handlerPreviousMapping = new HashMap<>(handlers.size());
        boolean extendsAllResults = extendsAllResults((AbstractArranger) arranger);
        for (AbstractArranger.Entry entry : entries) {
            if (entry.key.equals("then")) {
                String[] hs = entry.handlers;
                for (String h : hs) {
                    handlerPreviousMapping.put(h, new ArrayList<>(previous));
                }
            }

            String[] hs = entry.handlers;

            if (!extendsAllResults) {
                previous = new ArrayList<>(hs.length);
            }

            for (String h : hs) {
                previous.add(hm.get(h));
            }
        }
        return handlerPreviousMapping;
    }

    private void eventHanlderGenerate(Map<String, EventHandler> eventHandlers, Method handler, List<Method> previousMethods, Arranger arranger) {
        Handler annotation = handler.getAnnotation(Handler.class);
        String name = annotation.alias();
        if (StringUtils.isEmpty(name)) {
            name = handler.getName();
        }
        if (eventHandlers.get(name) != null) {
            throw new RuntimeException("Duplicate name [" + name + "] when generate event handler!");
        }
        EventHandler eventHandler = generate0(handler, previousMethods, arranger);
        eventHandlers.put(name, eventHandler);
    }

    private EventHandler generate0(Method handler, List<Method> previousMethods, Arranger arranger) {
        String className = ClassUtils.generateEventHandlerClassName(this.getEventSimpleName(arranger.name()), Utils.firstUpper(handler.getName()));
        EventHandler eventHandler = null;
        try {
            eventHandler = this.generateClass(className, previousMethods, handler, arranger);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Generate event handler exception", e);
        }
        return eventHandler;
    }

    @SuppressWarnings("unchecked")
    private EventHandler generateClass(String className, List<Method> previousMethods, Method handler, Arranger arranger) throws NotFoundException, CannotCompileException, IOException, IllegalAccessException, InstantiationException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException {
        String eventName = arranger.name();
        ClassPool cp = ClassUtils.getClassPool();

        CtClass ctClass = cp.makeClass(className);

        ctClass.setInterfaces(new CtClass[]{cp.get("com.jd.sirector.EventHandler")});
        ByteCodeUtils.buildField(ctClass, "event", eventName);
        ctClass.addConstructor(ByteCodeUtils.buildConstructor(ctClass, new String[]{eventName}, "{this.event = $1;}", (CtClass[]) null));
        ctClass.addMethod(this.buildOnEventMethodWithMapParam(ctClass, previousMethods, handler, this.getEventSimpleName(eventName)));
        ctClass.addMethod(this.buildOverrideMethod(ctClass, this.getEventSimpleName(eventName)));
        ByteCodeUtils.outputClass(ctClass, this.debug, this.path);


        /*
         * handler class name.
         */
        String handlerClassName = ClassUtils.generateEventHandlerClassName(this.getEventSimpleName(eventName), Utils.firstUpper(handler.getName()));
        /*
         * load the handler class
         */
        Class handlerClass = this.classLoader.loadClass(handlerClassName);
        Constructor<EventHandler> eventHandlerConstructor = handlerClass.getConstructor(arranger.getClass());
        return (EventHandler) eventHandlerConstructor.newInstance(arranger);
    }

    private CtMethod buildOverrideMethod(CtClass cc, String eventSimpleName) throws CannotCompileException {
        String eventClassName = ClassUtils.generateEventClassName(eventSimpleName);
        CtMethod method = new CtMethod(CtClass.voidType, "onEvent", new CtClass[]{ClassUtils.getObject()}, cc);
        method.setModifiers(1);
        StringBuilder body = new StringBuilder("{");
        body.append("\n    ").append(eventClassName).append(" event = ").append("(");
        body.append(eventClassName).append(") $1;");
        body.append(" onEvent(event);");
        body.append("\n}");
        method.setBody(body.toString());
        return method;
    }

    private CtMethod buildOnEventMethodWithMapParam(CtClass cc, List<Method> inputs, Method handler, String eventSimpleName) throws CannotCompileException {
        ByteCodeUtils.importPackage("java.util");
        String eventClassName = ClassUtils.generateEventClassName(eventSimpleName);
        CtMethod method = new CtMethod(CtClass.voidType, "onEvent", new CtClass[]{ClassUtils.get(eventClassName)}, cc);
        method.setModifiers(1);
        StringBuilder body = new StringBuilder("{");

        body.append("\n    Map param = $1.getParam();");
        if (Utils.isEmpty(inputs)) {
            body.append("\n    $1.set").append(Utils.firstUpper(handler.getName()));
            body.append("(event.").append(handler.getName()).append("(param));");
            body.append("\n}");
        } else {
            Iterator i$;
            Method input;
            body.append("\n    Map map = new HashMap();");
            for (i$ = inputs.iterator(); i$.hasNext(); ) {
                input = (Method) i$.next();
                body.append("\n    Object ");
                String name = ClassUtils.methodName(input);
                body.append(input.getName()).append(" = $1.get").append(Utils.firstUpper(input.getName())).append("();").append("\n    map.put(\"" + name + "\", " + input.getName() + ");");
            }
            body.append("\n    map.put(\"$param\", param);");
            body.append("\n    $1.set").append(Utils.firstUpper(handler.getName()));
            body.append("(event.").append(handler.getName()).append("(map));");
            body.append("\n}");
        }

        if (this.debug) {
            if(!CollectionUtils.isEmpty(inputs)) {
                for (Method method1 : inputs) {
                    System.out.println("previous method " + ClassUtils.methodName(method1));
                }
            }

            System.out.println("the body of method EventHandler$" + Utils.firstUpper(eventSimpleName) + "$" + Utils.firstUpper(handler.getName()) + ".onEvent() is:\n" + body.toString());
        }



        method.setBody(body.toString());
        return method;

    }

    private String getEventSimpleName(String eventName) {
        return eventName.substring(eventName.lastIndexOf(46) + 1);
    }

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }


}
