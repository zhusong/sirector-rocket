# sirector-rocket

sirector-rocket

实现CompletableFuture的线程编排能力，基于sirector-core实现，但使用原生sirector-core编排的过程稍微有点别扭，一大堆的EventHandler、proxy聚合等，
所以在sirector-core封装出sirector-rocket，rocket并不是代表并发速度rocket，而是开发理解rocket。

使用方式
@EventExecutor(extendsAllResults = true)
public class SearchResultHandlerArranger extends AbstractArranger{

    private static final ExecutorService newWareTagExecutor = new ScheduledThreadPoolExecutor(20);

    @Handler(alias = "stock")
    public List<String> handler1(Map<String, Object> param){
        System.out.println("param " + param);
        List<String> list = new ArrayList<>();
        list.add("handler1");
        return list;
    }

    @Handler(alias = "price")
    public List<String> handler2(Map<String, Object> params){
        List<String> list = new ArrayList<>();
        list.add("handler2");
        return list;
    }

    @Handler
    public List<String> handler3(Map<String, Object> params){
        List<String> list = new ArrayList<>();
        list.add("handler3");
        return list;
    }

    @Handler(alias = "merge")
    public Map<String, Object> merge(Map<String, Object> param){
        System.out.println("merge param " + param.get("$param"));
        List<String> stocks = (List<String>) param.get("stock");
        System.out.println("stocks " + stocks);
        return new HashMap<>();
    }

    @Override
    protected ExecutorService getExecutorService() {
        return newWareTagExecutor;
    }

    @Override
    public void arrange() {
        this.begin("stock", "price").then("handler3").then("merge").timeout(3000).end();
    }
}
spring.xml配置：

<beans>
   <bean id = "searchResultHandlerArranger" class="demo.SearchResultHandlerArranger" />
   <bean id = "searchResultHandlerArrangerSubClass" class="demo.extend.SearchResultHandlerArrangerSubClass" />
   <bean id = "manyForOneEventArrangerDemo" class="demo.ManyForOneEventArrangerDemo" />
   <bean id = "oneForManyEventArrangerDemo" class="demo.OneForManyEventArrangerDemo" />
</beans>
使用说明
1、@EventExecutor类注解，表明该类属于一个事件执行器，extendsAllResults参数表示是否后一级的handler都继承前所有级handler的返回结果（同级不继承），为true表示继承，否则不继承。例如为true时，在上面的声明中，merge同时继承stock、price、handler3的返回结果。否则merge的handler只继承来自handler3的返回结果。类注解还有@ManyForOne、@OneForMany。

2、@Handler方法注解，表明该方法是一个handler，因为类中存在很多方法，为了区分是否是Handler，增加该注解。被声明为该注解的方法会通过javassit生成EventHandler。里面包含三个属性：begin、end、alias。begin表示是否是第一个开始的Handler，和@OneForMany搭配使用。end表示是否是最后一个Handler，和ManyForOne搭配使用。alias标记该handler的别名，用于例如merge方法通过该alias获取该handler的返回结果，如果该值未配置，则使用方法名获取handler的返回结果。

3、必须继承AbstractArranger类，可以实现arrange方法，里面可以编排事件，将最大的自由交给作者。如果配合OneForMany和ManyForOne使用时，可以不实现该arrange方法，则表明编排交给sirector-rocket。实现arrange方法后，必须调用end方法结尾。

4、arrange()事件编排方法，如果使用了OneForMany和ManyForOne注解，可以无需实现该方法，如果实现类该方法，则以该方法的事件编排为准。

5、@OneForMany注解，声明的handler中，必须有一个包含begin=true，则该handler为第一个handler。

6、@ManyForOne注解，声明的handler中，必须有一个包含end=true，则该handler为最后一个handler。

7、传递的参数，必须是map，除了第一级的handler，该入参就是函数入参，而第二级以及后面的handler，以$param为入参。

8、继承也支持。

9、覆盖debug方法，返回true，同时覆盖classOutputPath方法，即可看到该路径下生成的EventHandler。

﻿

@Override
public Boolean debug(){
return true;
}

@Override
public String classOutputPath(){
return "/Users/zhusong5/Documents/mat_data";
}
例如ManyForOne：

@ManyForOne
public class ManyForOneEventArrangerDemo extends AbstractArranger{


    private static final ExecutorService newWareTagExecutor = new ScheduledThreadPoolExecutor(20);

    @Handler(alias = "stock")
    public List<String> handler1(Map<String, Object> result){
        List<String> list = new ArrayList<>();
        list.add("handler1");
        return list;
    }

    @Handler(alias = "price")
    public List<String> handler2(Map<String, Object> result){
        List<String> list = new ArrayList<>();
        list.add("handler2");
        return list;
    }

    @Handler
    public List<String> handler3(Map<String, Object> result){
        List<String> list = new ArrayList<>();
        list.add("handler3");
        return list;
    }

    @Handler(alias = "merge", end = true)
    public Map<String, String> merge(Map<String, Object> result){
        List<String> stocks = (List<String>) result.get("stock");
        Map<String, String> $param = (Map<String, String>) result.get("$param");
        System.out.println("stocks " + stocks);
        System.out.println("$param " + $param);
        Map a = new HashMap<String, String>();
        a.put("result", "good");
        return a;
    }

    @Override
    protected ExecutorService getExecutorService() {
        return newWareTagExecutor;
    }

    @Override
    public int timeout(){
        return 3000;
    }

}
以及OneForMany：

@OneForMany
public class OneForManyEventArrangerDemo extends AbstractArranger{


    private static final ExecutorService newWareTagExecutor = new ScheduledThreadPoolExecutor(20);

    @Handler(alias = "stock", begin = true)
    public List<String> handler1(Map<String, Object> result){
        List<String> list = new ArrayList<>();
        list.add("handler1");
        return list;
    }

    @Handler(alias = "price")
    public List<String> handler2(Map<String, Object> result){
        List<String> list = new ArrayList<>();
        list.add("handler2");
        return list;
    }

    @Handler
    public List<String> handler3(Map<String, Object> result){
        List<String> list = new ArrayList<>();
        list.add("handler3");
        return list;
    }

    @Handler(alias = "merge")
    public Map<String, Object> merge(Map<String, Object> result){
        List<String> stocks = (List<String>) result.get("stock");
        Map<String, String> $param = (Map<String, String>) result.get("$param");
        System.out.println("stocks " + stocks);
        System.out.println("$param " + $param);
        return new HashMap<>();
    }

    @Override
    protected ExecutorService getExecutorService() {
        return newWareTagExecutor;
    }

    @Override
    public void arrange() {
        this.begin("stock").then("price", "handler3", "merge").timeout(3000).end();
    }

}
而继承：

@EventExecutor(extendsAllResults = true)
public class SearchResultHandlerArrangerSubClass extends SearchResultHandlerArranger {

    private static final ExecutorService newWareTagExecutor = new ScheduledThreadPoolExecutor(20);

    @Handler
    public Map<String, Object> aliaia(Map<String, Object> result){
        return new HashMap<>();
    }

    @Handler
    public Map<String, Object> merge2(Map<String, Object> result){
        List<String> stocks = (List<String>) result.get("stock");
        List<String> prices = (List<String>) result.get("price");
        System.out.println("stocks " + stocks);
        System.out.println("prices " + prices);
        return new HashMap<>();
    }

    @Override
    protected ExecutorService getExecutorService() {
        return newWareTagExecutor;
    }

    @Override
    public void arrange() {
        this.begin("stock", "aliaia").then("handler3").then("merge2").timeout(3000).end();
    }
}
测试：

public class Test {

    public static void main(String[] args) {

        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("classpath:spring-config.xml");
        SearchResultHandlerArranger execute1 = (SearchResultHandlerArranger) context.getBean("searchResultHandlerArranger");
        ManyForOneEventArrangerDemo execute2 = (ManyForOneEventArrangerDemo) context.getBean("manyForOneEventArrangerDemo");
        OneForManyEventArrangerDemo execute3 = (OneForManyEventArrangerDemo) context.getBean("oneForManyEventArrangerDemo");
        SearchResultHandlerArrangerSubClass execute4 = (SearchResultHandlerArrangerSubClass) context.getBean("searchResultHandlerArrangerSubClass");

        Map<String, String> params = new HashMap<>();
        params.put("1", "demo");
        System.out.println(execute4.publish(params));

        execute1.shutdownGracefully();
        execute3.shutdownGracefully();
        execute2.shutdownGracefully();
        execute4.shutdownGracefully();
    }

}