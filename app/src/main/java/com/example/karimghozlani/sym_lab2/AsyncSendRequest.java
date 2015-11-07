package com.example.karimghozlani.sym_lab2;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by karimghozlani on 05.11.15.
 */
public class AsyncSendRequest implements IAsyncSendRequest {
    List<CommunicationEventListener> listeners = new ArrayList<>();

    @Override
    public void sendRequest(final String request, final String link) {
        Log.d("AsyncSendRequest", "sendRequest method called");

        Thread thread = new Thread(){
            public void run() {
                try {
                    URL url = new URL(link);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setReadTimeout(10000);
                    conn.setConnectTimeout(15000);
                    conn.setRequestMethod("POST");
                    conn.setDoOutput(true);

                    OutputStream out = conn.getOutputStream();
                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out, "UTF-8"));
                    writer.write(request);
                    writer.flush();
                    writer.close();
                    out.close();

                    conn.connect();
                    Log.d("AsyncSendRequest", "Connection code was " + conn.getResponseCode());

                    for (CommunicationEventListener l: listeners) {
                        Log.d("AsyncSendRequest", "Called listener: " + l);
                        l.handleServerResponse(conn.getResponseMessage());
                    }
                } catch (Exception e) {
                    Log.d("AsyncSendRequest", "An exception occurred: " + e);
                }
            }
        };

        thread.start();
    }

    @Override
    public void addCommunicationListener(CommunicationEventListener listener) {
        if (!listeners.contains(listener)) listeners.add(listener);
    }
}
