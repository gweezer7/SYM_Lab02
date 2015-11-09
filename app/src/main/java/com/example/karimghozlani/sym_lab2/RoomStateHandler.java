package com.example.karimghozlani.sym_lab2;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.zip.GZIPOutputStream;

/**
 * Created by karimghozlani on 08.11.15.
 */
public class RoomStateHandler extends Activity {
    private TextView textArea;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textArea = (TextView) findViewById(R.id.textView);
        Button signIn = (Button) findViewById(R.id.send_button);
        signIn.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                sendRoomState();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_sym_com_manager, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void sendRoomState() {
        // check that we have connectivity on device
        ConnectivityManager conn = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = conn.getActiveNetworkInfo();

        if (netInfo != null && netInfo.isConnected()) {
            // generate room state
            RoomState roomState = RoomState.generateRoomState();

            // serialize it
            String serializedRoomState = new Gson().toJson(roomState);


            // send room state to server
            AsyncSendRequest asr = new AsyncSendRequest();
            asr.addCommunicationListener(new CommunicationEventListener(textArea));
            asr.sendRequest(serializedRoomState, "http://moap.iict.ch:8080/Moap/Basic");
        } else {
            Toast.makeText(this, "Connection failed", Toast.LENGTH_SHORT).show();
            // todo add failed request to deferred transmitter stack
        }

    }
}
