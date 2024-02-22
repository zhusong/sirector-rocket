package com.jd.sirector.rocket.proxy;

import com.jd.sirector.EventHandler;
import com.jd.sirector.EventHandlerGroup;
import com.jd.sirector.Sirector;
import com.jd.sirector.rocket.annotation.*;
import com.jd.sirector.rocket.utils.ClassUtils;
import com.jd.sirector.rocket.utils.Utils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * <p>加上结果事件处理的抽象事件编排</p>
 *
 * @author : zhusong
 * @date : 2020-05-24 17:52
 **/
public abstract class AbstractArranger implements Arranger, InitializingBean {

    private Map<String, EventHandler> eventHandlers;
    private ExecutorService executorService;
    private Class<Event> eventClass;
    private Method beginHandler;
    private ClassLoader loader;
    private Method endHandler;
    private Entries entries;
    private int timeout;
    List<Method> methodHandlers;
    Map<String, Method> methodsMapping;

    EventExecutor eventExecutor;
    ManyForOne manyForOne;
    OneForMany oneForMany;


    public Entries begin(String... handlers) {
        Entries entries = new Entries();
        entries.begin(handlers);
        return entries;
    }

    @Override
    public String name() {
        return getClass().getName();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        loader = new ByteCodeClassLoader();
        this.executorService = getExecutorService();
        if(this.executorService == null){
            throw new RuntimeException("ExecutorService null when " + this.getClass().getName() + " publish.");
        }
        this.arrange();
    }

    @Override
    public void arrange(){
        /*
         * arrange the event handler, can be rewrite in subclass.
         */
        process(null , 0);
    }

    private void process(Entries entries, int timeout) {
        this.entries = entries;
        this.timeout = timeout;

        Method[] methods = this.getClass().getMethods();

        methodsMapping = new HashMap<>(methods.length);
        this.methodHandlers = new ArrayList<>(methods.length);
        for (Method method : methods) {
            Object handler = method.getAnnotation(Handler.class);
            if (handler == null) {
                continue;
            }

            methodHandlers.add(method);
            if(methodsMapping.get(ClassUtils.methodName(method)) != null){
                throw new RuntimeException("Duplicate handler name [" + ClassUtils.methodName(method) + "] in " + this.getClass().getName());
            }
            methodsMapping.put(ClassUtils.methodName(method), method);
        }

        if (CollectionUtils.isEmpty(this.methodHandlers)) {
            throw new RuntimeException("No methods has been annotated by @Handler in " + this.getClass().getName() + ", please using @Handler to resolve this exception.");
        }

        this.eventExecutor = this.getClass().getAnnotation(EventExecutor.class);
        this.manyForOne = this.getClass().getAnnotation(ManyForOne.class);
        this.oneForMany = this.getClass().getAnnotation(OneForMany.class);

        checkAnnotations();
        checkEntries();

        generateEventClass(this.methodHandlers, this.entries, this.loader);
        generateEventHandlerClass(this.methodHandlers, this.entries, this.loader);

        try {
            String eventClassName = ClassUtils.generateEventClassName(this.getClass().getSimpleName());
            this.eventClass = (Class<Event>) this.loader.loadClass(eventClassName);
        } catch (Exception e) {
            throw new RuntimeException("Loading the event class exception.", e);
        }
    }


    private void checkAnnotations(){
        List annotations = new ArrayList();
        if(this.eventExecutor != null){
            annotations.add(this.eventExecutor);
        }
        if(this.manyForOne != null){
            if(this.entries == null) {
                checkLastOneHandler();
                generateManyForOneEntries();
            }
            annotations.add(this.manyForOne);
        }
        if(this.oneForMany != null){
            if(this.entries == null) {
                checkFirstOneHandler();
                generateOneForManyEntries();
            }
            annotations.add(this.oneForMany);
        }

        if(annotations.size() > 1){
            throw new RuntimeException("Multiple executor annotations in " + this.getClass().getName() + ", which is should be one of them!");
        }
    }

    private void generateManyForOneEntries() {
        String[] begins = this.entriesSearch(endHandler);
        this.timeout = timeout();
        this.entries = this.begin(begins).then(ClassUtils.methodName(endHandler)).timeout(this.timeout == 0 ? 10000 : this.timeout);
    }

    private String[] entriesSearch(Method searchMethod){
        String[] handlers = new String[methodHandlers.size() - 1];
        int index = 0;
        for(Method method : this.methodHandlers){
            if(method != searchMethod){
                handlers[index] = ClassUtils.methodName(method);
                index++;
            }
        }
        return handlers;
    }

    private void generateOneForManyEntries() {
        String[] thens = entriesSearch(beginHandler);
        this.timeout = timeout();
        this.entries = this.begin(ClassUtils.methodName(beginHandler)).then(thens).timeout(this.timeout == 0 ? 10000 : this.timeout);
    }

    private void checkFirstOneHandler() {
        int hasBegin = 0;
        for(Method method : this.methodHandlers){
            Handler handler = method.getAnnotation(Handler.class);
            if(handler.begin()){
                hasBegin++;
                this.beginHandler = method;
            }
        }
        if(hasBegin > 1){
            throw new RuntimeException("There is Only one @Handler can be began.");
        }
        if(hasBegin < 1){
            throw new RuntimeException("It must have one @Handler to begin.");
        }
    }

    private void checkEntries(){
        if(this.eventExecutor != null){
            if(this.entries == null){
                throw new RuntimeException("Entries are empty in " + this.getClass().getName() + ", should override the arrange() method to arrange the event handlers");
            }
        }
    }

    private void checkLastOneHandler(){
        int hasEnd = 0;
        for(Method method : this.methodHandlers){
            Handler handler = method.getAnnotation(Handler.class);
            if(handler.end()){
                hasEnd++;
                this.endHandler = method;
            }
        }
        if(hasEnd > 1){
            throw new RuntimeException("There is Only one @Handler can be ended.");
        }
        if(hasEnd < 1){
            throw new RuntimeException("It must have one @Handler to end.");
        }
    }

    private void generateEventClass(List<Method> handlers, Entries entries, ClassLoader loader) {
        EventBuilder.builder(debug(), classOutputPath(), loader).build(handlers, entries, this);
    }

    private void generateEventHandlerClass(List<Method> handlers, Entries entries, ClassLoader loader) {
//        List<Method> usedHandlers = excluseUnuseHandlers(entries);
        this.eventHandlers = EventHandlerBuilder.builder(debug(), classOutputPath(), loader).build(handlers, entries, this);
    }

    protected Boolean debug(){
        return false;
    }
    protected String classOutputPath(){
        return null;
    }

    private Object publish(Object params, Class<?> clazz) {

        Event event = null;
        try {
            event = eventClass.newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Event class " + eventClass.getName() + " new instance exception ", e);
        }

        if (event == null) {
            throw new RuntimeException("Generating the event object unsuccessful.");
        }

        Sirector<Event> sirector = new Sirector<>(executorService);
        EventHandlerGroup eventHandlerGroup = null;
        for (Entry entry : entries) {
            if (entry.key.equals("begin")) {
                eventHandlerGroup = sirector.begin(eventHandlers(entry.key, entry.handlers));
            }
            if (entry.key.equals("after")) {
                eventHandlerGroup = sirector.after(eventHandlers(entry.key, entry.handlers));
            }
            if (entry.key.equals("then")) {
                eventHandlerGroup.then(eventHandlers(entry.key, entry.handlers));
            }
        }

        try {
            invokeMethod(event, "setParam", new Object[]{params}, clazz);
        } catch (Exception e) {
            throw new RuntimeException("Invoke method [setParam] exception", e);
        }

        sirector.ready();
        sirector.publish(event, timeout);

        if(this.oneForMany != null){
            return null;
        }

        String lastHandler = entries.get(entries.size() - 1).handlers[0];
        try {
            return invokeMethod(event, "get" + Utils.firstUpper(methodsMapping.get(lastHandler).getName()), null, null);
        } catch (Exception e) {
            throw new RuntimeException("Invoke method [" + lastHandler + "] exception", e);
        }
    }

    @Override
    public Object publish(Map params) {
        return this.publish(params, Map.class);
    }

    private Object invokeMethod(Object methodObject, String methodName, Object[] args, Class type) throws Exception {
        Class ownerClass = methodObject.getClass();
        Method method = type == null ? ownerClass.getMethod(methodName) : ownerClass.getMethod(methodName, type);
        return method.invoke(methodObject, args);
    }

    private EventHandler[] eventHandlers(String key, String... handlers) {
        if (StringUtils.isEmpty(handlers)) {
            throw new IllegalArgumentException("Parameter [" + key + "] handlers are empty in method arrange() of " + this.getClass().getName());
        }
        EventHandler[] eventHandlerArr = new EventHandler[handlers.length];
        for (int i = 0; i < handlers.length; i++) {
            eventHandlerArr[i] = initExecuteHandler(handlers[i]);
        }
        return eventHandlerArr;
    }

    protected EventHandler initExecuteHandler(String handler) {
        if (CollectionUtils.isEmpty(this.eventHandlers)) {
            throw new RuntimeException("Loaded event handlers are empty in " + this.getClass().getName() + ", please using annotation @handler to resolve this exception.");
        }
        return this.eventHandlers.get(handler);
    }

    @Override
    public void shutdownGracefully(){
        this.executorService.shutdown();
    }

    protected class Entries extends ArrayList<Entry> {
        private int timeout;

        public Entries begin(String... handlers) {
            this.add(new Entry("begin", handlers));
            return this;
        }

        public Entries after(String... handlers) {
            this.add(new Entry("after", handlers));
            return this;
        }

        public Entries then(String... handlers) {
            this.add(new Entry("then", handlers));
            return this;
        }

        public Entries timeout(int timeout) {
            this.timeout = timeout;
            return this;
        }

        public void end() {
            AbstractArranger.this.process(this, this.timeout == 0 ? 10000 : this.timeout);
        }
    }

    public int timeout(){
        return 0;
    }

    class Entry {
        String key;
        String[] handlers;

        private Entry(String key, String[] handlers) {
            this.key = key;
            this.handlers = handlers;
        }
    }

    protected abstract ExecutorService getExecutorService();
}
