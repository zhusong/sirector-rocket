package com.jd.sirector.rocket.proxy;

import java.util.Map;

/**
 * <p>arranger</p>
 *
 * @author : zhusong
 * @date : 2020-05-24 17:52
 **/
public interface Arranger {
    /**
     * arrange the event.
     */
    void arrange();

    /**
     * arranger name
     *
     * @return
     */
    String name();


    /**
     * publish
     *
     * @param params
     * @return
     */
    Object publish(Map<String, Object> params);

    void shutdownGracefully();
}
