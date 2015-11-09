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

    public DeferredTransmitter(AsyncSendRequest asr, Context context) {
        this.asr = asr;
        this.context = context;

        asr.setDeferredTransmitter(this);
    }

    private boolean isConnected() {
        ConnectivityManager conn = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = conn.getActiveNetworkInfo();

        return (netInfo != null && netInfo.isConnected());
    }

    public void queueRequest(Pair<String, String> request) {
        failedRequests.add(request);

        if (!isResentScheduled) {
            isResentScheduled = true;
            thread.start();
        }
    }

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
