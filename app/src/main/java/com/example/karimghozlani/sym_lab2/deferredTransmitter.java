package com.example.karimghozlani.sym_lab2;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Pair;

import java.util.ArrayList;
import java.util.List;

/**
 * Class to handle sending deferred requests
 *
 * @author Karim Ghozlani
 * @author Eleonore d'Agostino
 */
public class DeferredTransmitter {
    private List<Pair<String, String>> failedRequests = new ArrayList<>();
    private boolean isResentScheduled = false;
    private AsyncSendRequest asr;
    private Context context;

    /**
     * Main thread: sends messages if we have connection, waits 10s otherwise and tries again,
     * until we don't have any more requests scheduled.
     */
    private Thread thread = new Thread() {
        public void run() {
            while (isResentScheduled) {
                if (isConnected()) {
                    sendQueuedMessage();
                } else {
                    try {
                        thread.wait(10000);
                    } catch (Exception e) {
                        thread.interrupt();
                    }
                }
            }
        }
    };

    /**
     * Creates a new transmitter, assigns it an AsyncSendRequest, and tells said ASR to use this
     * transmitter when it runs into connectivity issues.
     * @param asr the AsyncSendRequest to use to send our requests
     * @param context the Activity context to use to check for connectivity
     */
    public DeferredTransmitter(AsyncSendRequest asr, Context context) {
        this.asr = asr;
        this.context = context;

        asr.setDeferredTransmitter(this);
    }

    /**
     * Checks whether network connectivity is available for our given context
     * @return true if we're connected, false otherwise
     */
    private boolean isConnected() {
        ConnectivityManager conn = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = conn.getActiveNetworkInfo();

        return (netInfo != null && netInfo.isConnected());
    }

    /**
     * Adds a request to our queue. If we didn't already have queued requests, we start the main
     * thread to send them.
     * @param request Pair composed of an HTTP request and a link to send it to
     */
    public void queueRequest(Pair<String, String> request) {
        failedRequests.add(request);

        if (!isResentScheduled) {
            isResentScheduled = true;
            thread.start();
        }
    }

    /**
     * Removes the oldest message in our request queue and attempts to send it. If our queue is empty,
     * we stop the main thread.
     */
    public void sendQueuedMessage() {
        Pair<String, String> request = failedRequests.remove(0);
        asr.sendRequest(request.first, request.second);

        if (failedRequests.isEmpty()) {
            isResentScheduled = false;
            try {
                thread.join();
            } catch (Exception e) {}
        }
    }

}
