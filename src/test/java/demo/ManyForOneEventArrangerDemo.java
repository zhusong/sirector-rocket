package demo;

import com.jd.sirector.rocket.annotation.Handler;
import com.jd.sirector.rocket.annotation.ManyForOne;
import com.jd.sirector.rocket.proxy.AbstractArranger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;

/**
 * <p></p>
 *
 * 有没有办法在运行时调用相应的方法，根据传入的ClassType来做。可是他会经过编译，编译是没有这个方法的。
 *
 * @author : zhusong
 * @date : 2020-05-24 17:43
 **/

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
