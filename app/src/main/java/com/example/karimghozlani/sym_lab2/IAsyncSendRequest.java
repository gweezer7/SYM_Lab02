package com.example.karimghozlani.sym_lab2;

/**
 * Interface for the asynchronous HTTP request sending
 *
 * @author Karim Ghozlani
 * @author Eleonore d'Agostino
 */
public interface IAsyncSendRequest {
    void sendRequest(String request, String link);
    void addCommunicationListener(CommunicationEventListener listener);
}
