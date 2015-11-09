package com.example.karimghozlani.sym_lab2;

import android.app.Activity;
import android.widget.TextView;

import com.google.gson.Gson;

/**
 * Created by karimghozlani on 05.11.15.
 */
public class CommunicationEventListener extends Activity implements ICommunicationEventListener {

    TextView textArea;

    public CommunicationEventListener(TextView textArea) {
        this.textArea = textArea;
    }

    @Override
    public boolean handleServerResponse(final String response) {
        final RoomState roomState = new Gson().fromJson(response, RoomState.class);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textArea.setText(roomState.toString());
            }
        });
        return false;
    }
}
