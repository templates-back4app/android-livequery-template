package com.example.livequeryandroid;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.livequery.ParseLiveQueryClient;
import com.parse.livequery.SubscriptionHandling;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private int numPokes;
    ParseLiveQueryClient parseLiveQueryClient = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initializeUI();
        initializeLiveQuery();
    }

    private void initializeUI() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    private void initializeLiveQuery() {
        // Init Live Query Client
        try {
            Toast.makeText(this, "Connection Established", Toast.LENGTH_SHORT).show();
            parseLiveQueryClient = ParseLiveQueryClient.Factory.getClient(new URI("wss://"));
        } catch (URISyntaxException e) {
            Toast.makeText(this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        //Subscribe to events
        if (parseLiveQueryClient != null) {
            ParseQuery<ParseObject> parseQuery = new ParseQuery("Message");
            parseQuery.whereEqualTo("destination", "pokelist");
            SubscriptionHandling<ParseObject> subscriptionHandling =
                    parseLiveQueryClient.subscribe(parseQuery);

            //This will be triggered when a new object is created
            subscriptionHandling.handleEvent(SubscriptionHandling.Event.CREATE, (query, object) -> {
                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(() -> {
                    TextView pokeText = findViewById(R.id.pokeText);
                    numPokes++;
                    if(numPokes == 1) {
                        pokeText.setText("Poked " + numPokes + " time");
                    }
                    else {
                        pokeText.setText("Poked " + numPokes + " times");
                    }
                });
            });

            //This will be triggered when a new object is created
            subscriptionHandling.handleEvent(SubscriptionHandling.Event.DELETE, (query, object) -> {
                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(() -> {
                    TextView pokeText = findViewById(R.id.pokeText);
                    numPokes--;
                    if(numPokes == 1) {
                        pokeText.setText("Poked " + numPokes + " time");
                    }
                    else {
                        pokeText.setText("Poked " + numPokes + " times");
                    }
                });
            });
        }

    }



    public void makePoke(View view) {
        //Creating new object for Message Class
        ParseObject entity = new ParseObject("Message");

        entity.put("destination", "pokelist");

        // Saves the new object.
        // Notice that the SaveCallback is totally optional!
        entity.saveInBackground(e -> {
            if (e==null){
                //Save was done
            }else{
                //Something went wrong
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    public void deletePoke(View view) {
        //Fetching object form Message Class
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Message");

        query.whereEqualTo("destination", "pokelist");

        // Saves the new object.
        // Notice that the SaveCallback is totally optional!
        query.getFirstInBackground((object, e) ->{
            if(e==null){
                //This is delete the object and trigger the delete event in live query
                object.deleteInBackground();
            }else{
                //Something went wrong
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}