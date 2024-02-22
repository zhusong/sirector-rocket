package demo;

import com.jd.sirector.rocket.annotation.EventExecutor;
import com.jd.sirector.rocket.annotation.Handler;
import com.jd.sirector.rocket.proxy.AbstractArranger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;

/**
 * <p><execute也作为其handler的一部分/p>
 *     即生成一个前置的参数handler，把范型加入进来。
 *
 * @author : zhusong
 * @date : 2020-05-24 17:43
 **/

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

    @Handler(alias = "alibaba")
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
        Entries entries = this.begin("stock", "price").then("handler3");
        entries.after("handler3").then("alibaba").timeout(3000).end();
    }

    @Override
    public Boolean debug(){
        return true;
    }

    @Override
    public String classOutputPath(){
        return "/Users/zhusong5/Documents/mat_data";
    }

}
