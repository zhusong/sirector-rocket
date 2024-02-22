package demo.extend;

import com.jd.sirector.rocket.annotation.EventExecutor;
import com.jd.sirector.rocket.annotation.Handler;
import com.jd.sirector.rocket.proxy.AbstractArranger;
import demo.SearchResultHandlerArranger;

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
