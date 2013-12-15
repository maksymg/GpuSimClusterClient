package com.mgnyniuk.parallel;

/**
 * Created by maksym on 12/15/13.
 */
public class ThreadListener implements ThreadCompleteListener {

    public int quantityOfEndedThreads;

    @Override
    public void notifyOfThreadComplete(Thread thread) {
        System.out.println(thread.getName() + " ended!");
        quantityOfEndedThreads++;
    }
}
