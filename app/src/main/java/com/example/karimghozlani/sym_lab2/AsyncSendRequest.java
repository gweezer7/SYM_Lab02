package com.example.karimghozlani.sym_lab2;

import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

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

                    // compress and Base64 encode
                    String compressedRequest;
                    try {
                        ByteArrayOutputStream os = new ByteArrayOutputStream();
                        GZIPOutputStream gzos = new GZIPOutputStream(os);
                        gzos.write(request.getBytes());
                        gzos.close();
                        compressedRequest = Base64.encodeToString(os.toByteArray(), Base64.DEFAULT);
                    } catch (IOException e) {
                        Log.d("AsyncSendRequest", "An exception occurred: " + e);
                        return;
                    }

                    OutputStream out = conn.getOutputStream();
                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out, "UTF-8"));
                    writer.write(compressedRequest);
                    writer.flush();
                    writer.close();
                    out.close();

                    // send
                    conn.connect();

                    // response received
                    InputStream is = conn.getInputStream();
                    Log.d("AsyncSendRequest", "Connection code was " + conn.getResponseCode());

                    // Convert the InputStream into a string
                    String contentAsString = readIt(is);

                    String encodedRoomState = contentAsString.substring(
                            contentAsString.length() - compressedRequest.length() + 2, contentAsString.length());


                    // decode Base64
                    // unzip
                    StringBuilder sb = new StringBuilder();
                    try {
                        byte[] base64DecodedResponse = Base64.decode(encodedRoomState, Base64.DEFAULT);
                        ByteArrayInputStream inputStream = new ByteArrayInputStream(base64DecodedResponse);
                        GZIPInputStream gzis = new GZIPInputStream(inputStream);
                        InputStreamReader reader = new InputStreamReader(gzis);
                        BufferedReader in = new BufferedReader(reader);

                        String read;
                        while ((read = in.readLine()) != null) {
                            sb.append(read);
                        }
                        gzis.close();
                    } catch (IOException e) {
                        Log.d("AsyncSendRequest", "An exception occurred: " + e);
                        return;
                    }


                    // forward response to listeners
                    for (CommunicationEventListener l: listeners) {
                        Log.d("AsyncSendRequest", "Called listener: " + l);
                        l.handleServerResponse(sb.toString());
                    }
                } catch (Exception e) {
                    Log.d("AsyncSendRequest", "An exception occurred: " + e);
                    // todo schedule retry


                }
            }

            public String readIt(InputStream stream) throws IOException {
                BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
                StringBuilder result = new StringBuilder();
                String line;
                while((line = reader.readLine()) != null) {
                    result.append(line);
                }
                return result.toString();
            }
        };

        thread.start();
    }

    @Override
    public void addCommunicationListener(CommunicationEventListener listener) {
        if (!listeners.contains(listener)) listeners.add(listener);
    }
}
