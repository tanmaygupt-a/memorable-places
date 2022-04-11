package com.example.memorableplaces;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    ListView placesListView;
    Intent mapIntent;

    public static ArrayList<String> places = new ArrayList<String>();
    public static ArrayList<LatLng> locations = new ArrayList<LatLng>();
    static ArrayAdapter<String> arrayAdapter;

    public void addPlaces(View view){
        mapIntent = new Intent(getApplicationContext(),MapsActivity.class);
        mapIntent.putExtra("addingPlace",true);
        startActivity(mapIntent);
    }
    public void visitPlaces(int position){
        mapIntent = new Intent(getApplicationContext(),MapsActivity.class);
        mapIntent.putExtra("placeNumber",position);
        mapIntent.putExtra("addingPlace",false);
        startActivity(mapIntent);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SharedPreferences sharedPreferences = this.getSharedPreferences("com.example.memorableplaces",Context.MODE_PRIVATE);
        ArrayList<String> latitudes = new ArrayList<>();
        ArrayList<String> longitudes = new ArrayList<>();
        places.clear();
        latitudes.clear();
        longitudes.clear();
        locations.clear();

        try {
            places = (ArrayList<String>) ObjectSerializer.deserialize(sharedPreferences.getString("places",ObjectSerializer.serialize(new ArrayList<String>())));
            latitudes = (ArrayList<String>) ObjectSerializer.deserialize(sharedPreferences.getString("lats",ObjectSerializer.serialize(new ArrayList<String>())));
            longitudes = (ArrayList<String>) ObjectSerializer.deserialize(sharedPreferences.getString("lons",ObjectSerializer.serialize(new ArrayList<String>())));

        } catch (IOException e) {
            e.printStackTrace();
        }
        if(places.size()>0 && latitudes.size()>0 && longitudes.size()>0){
            if(places.size() == latitudes.size() && places.size() == longitudes.size()){
                for(int i=0; i < places.size(); i++){
                    locations.add(new LatLng(Double.parseDouble(latitudes.get(i)), Double.parseDouble(longitudes.get(i))));
                }
            }
        }

        placesListView = (ListView) findViewById(R.id.listView);

        arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,places);
        placesListView.setAdapter(arrayAdapter);

        placesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                visitPlaces(position);
            }
        });
        placesListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                new AlertDialog.Builder(MainActivity.this).setIcon(android.R.drawable.ic_delete)
                        .setTitle("Delete").setMessage("Do you want to delete the selected location?")
                        .setPositiveButton("DELETE", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                places.remove(position);
                                locations.remove(position);
                                arrayAdapter.notifyDataSetChanged();
                                try {
                                    ArrayList<String> latitudes = new ArrayList<>();
                                    ArrayList<String> longitudes = new ArrayList<>();

                                    for(LatLng coord : MainActivity.locations){
                                        latitudes.add(Double.toString(coord.latitude));
                                        longitudes.add(Double.toString(coord.longitude));
                                    }
                                    sharedPreferences.edit().putString("places", ObjectSerializer.serialize(MainActivity.places)).apply();
                                    sharedPreferences.edit().putString("lats", ObjectSerializer.serialize(latitudes)).apply();
                                    sharedPreferences.edit().putString("lons", ObjectSerializer.serialize(longitudes)).apply();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }


                            }
                        }).setNegativeButton("CANCEL", null).show();

                return false;
            }
        });
    }
}