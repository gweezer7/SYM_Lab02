package com.example.karimghozlani.sym_lab2;

/**
 * Interface for the CommunicationEventListener object
 *
 * @author Karim Ghozlani
 * @author Eleonore d'Agostino
 */
public interface ICommunicationEventListener {
    boolean handleServerResponse(String response);
}
