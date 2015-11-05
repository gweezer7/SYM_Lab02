package com.example.karimghozlani.sym_lab2;

/**
 * Created by karimghozlani on 05.11.15.
 */
public interface IAsyncSendRequest {

    public void sendRequest(String request, String link);
    public void addCommunicationListener(CommunicationEventListener listener);
}
