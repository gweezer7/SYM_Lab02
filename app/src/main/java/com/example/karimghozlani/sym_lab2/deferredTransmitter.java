package com.example.karimghozlani.sym_lab2;

import android.util.Pair;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by karimghozlani on 09.11.15.
 */
public class deferredTransmitter {
    private List<Pair<String, String>> failedRequests = new LinkedList<>();
    private boolean isResentScheduled = false;

    public void scheduleResent(Pair<String, String> request) {
        failedRequests.add(request);
        if (!isResentScheduled) {
            isResentScheduled = true;
            // todo thread task to check connectivity and trigger retry if connectivity found
        }
    }

    public void sendQueuedMessages() {
        for (Pair<String, String> failedRequest : failedRequests) {
            AsyncSendRequest asr = new AsyncSendRequest();
            // todo find a way to pass the text area
            asr.addCommunicationListener(new CommunicationEventListener(null));
            asr.sendRequest(failedRequest.first, failedRequest.second);
        }
        isResentScheduled = false;
        // todo stop the thread
    }

}
