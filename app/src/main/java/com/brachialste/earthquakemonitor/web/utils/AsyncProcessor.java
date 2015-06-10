package com.brachialste.earthquakemonitor.web.utils;

import java.util.Observer;
import java.util.Random;
import java.util.concurrent.Callable;

/**
 * Created by brachialste on 24/09/13.
 */
public class AsyncProcessor implements Callable<Object> {
    private Observer listener;

    private AsyncProcessor(Observer listener) {
        this.listener = listener;
    }

    public Object call() throws Exception {
        // some processing here which can take all kinds of time...
        int sleepTime = new Random().nextInt(2000);
        System.out.println("Sleeping for " + sleepTime + "ms");
        Thread.sleep(sleepTime);
        listener.update(null, null); // not standard usage, but good for a demo
        return null;
    }
}
