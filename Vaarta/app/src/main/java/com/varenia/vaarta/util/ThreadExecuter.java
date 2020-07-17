package com.varenia.vaarta.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadExecuter {

    private static final ExecutorService threadPool = Executors.newCachedThreadPool();

    public static void runButNotOn(Runnable task, Thread notOn){
        if(Thread.currentThread() == notOn){
            threadPool.submit(task);
        }
        else{
            task.run();
        }
    }

}
