package test;


import demo.ManyForOneEventArrangerDemo;
import demo.OneForManyEventArrangerDemo;
import demo.SearchResultHandlerArranger;
import demo.extend.SearchResultHandlerArrangerSubClass;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.HashMap;
import java.util.Map;

/**
 * Hello world!
 */
public class Test {

    public static void main(String[] args) {

        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("classpath:spring-config.xml");
//        SearchResultHandlerArranger execute1 = (SearchResultHandlerArranger) context.getBean("searchResultHandlerArranger");
        ManyForOneEventArrangerDemo execute2 = (ManyForOneEventArrangerDemo) context.getBean("manyForOneEventArrangerDemo");
//        OneForManyEventArrangerDemo execute3 = (OneForManyEventArrangerDemo) context.getBean("oneForManyEventArrangerDemo");
//        SearchResultHandlerArrangerSubClass execute4 = (SearchResultHandlerArrangerSubClass) context.getBean("searchResultHandlerArrangerSubClass");



        Map<String, String> params = new HashMap<>();
        params.put("1", "demo");
        System.out.println(execute2.publish(params));
//
//        execute1.shutdownGracefully();
//        execute3.shutdownGracefully();
        execute2.shutdownGracefully();
//        execute4.shutdownGracefully();
    }

}
