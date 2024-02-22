package demo;

import com.jd.sirector.rocket.annotation.Handler;
import com.jd.sirector.rocket.annotation.ManyForOne;
import com.jd.sirector.rocket.annotation.OneForMany;
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
 * @author : zhusong
 * @date : 2020-05-24 17:43
 **/

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
