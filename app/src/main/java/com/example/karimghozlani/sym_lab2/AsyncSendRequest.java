package com.example.karimghozlani.sym_lab2;

import android.util.Base64;
import android.util.Log;
import android.util.Pair;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Implementation of asynchronous HTTP request sending.
 * Includes compression with Base64 encoding.
 *
 * @author Karim Ghozlani
 * @author Eleonore d'Agostino
 */
public class AsyncSendRequest implements IAsyncSendRequest {
    List<CommunicationEventListener> listeners = new ArrayList<>();
    DeferredTransmitter transmitter;

    /**
     * Sends an HTTP POST message to the given link, containing the given request.
     * It is compressed using GZIP, and encoded with Base64.
     * Once the response has been obtained, its contents are sent to all the currently subscribed
     * listeners.
     * If the request fails to send, we send it to the DeferredTransmitter to attempt sending again
     * later.
     * @param request content of the HTTP POST message
     * @param link link to send the request to
     */
    @Override
    public void sendRequest(final String request, final String link) {
        Log.d("AsyncSendRequest", "sendRequest method called");

        Thread thread = new Thread(){
            public void run() {
                try {
                    // get our HTTP connection set up
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
                        Log.d("AsyncSendRequest", "An exception occurred while compressing and/or encoding: " + e);
                        return;
                    }

                    // write request to our connection stream
                    OutputStream out = conn.getOutputStream();
                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out, "UTF-8"));
                    writer.write(compressedRequest);
                    writer.flush();
                    writer.close();
                    out.close();

                    // send
                    conn.connect();

                    // receive response
                    InputStream is = conn.getInputStream();
                    Log.d("AsyncSendRequest", "Connection code was " + conn.getResponseCode());

                    // Convert the InputStream into a string
                    String contentAsString = readIt(is);
                    String encodedRoomState = contentAsString.substring(
                            contentAsString.length() - compressedRequest.length() + 2, contentAsString.length());

                    // decode Base64 and unzip
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
                        Log.d("AsyncSendRequest", "An exception occurred while decoding and/or unzipping: " + e);
                        return;
                    }

                    // forward response to listeners
                    for (CommunicationEventListener l: listeners) {
                        Log.d("AsyncSendRequest", "Called listener: " + l);
                        l.handleServerResponse(sb.toString());
                    }
                } catch (Exception e) {
                    Log.d("AsyncSendRequest", "An exception occurred while attempting to send the request: " + e);
                    transmitter.queueRequest(Pair.create(request, link));
                }
            }

            // converts inputstream contents into a string
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

    public void setDeferredTransmitter(DeferredTransmitter dt) {
        transmitter = dt;
    }
}
