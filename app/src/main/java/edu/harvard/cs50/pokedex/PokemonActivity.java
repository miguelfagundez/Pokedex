package edu.harvard.cs50.pokedex;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;

public class PokemonActivity extends AppCompatActivity {
    private TextView nameTextView;
    private TextView numberTextView;
    private TextView type1TextView;
    private TextView type2TextView;
    private TextView tvDescription;
    private Button button;
    private ImageView imagePokemon;
    private String url;
    private String descriptionUrl;
    private String imageUrl;
    private int position;
    private RequestQueue requestQueue;

    private String prefsName = "SharedPreference Pokemon";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pokemon);

        requestQueue = Volley.newRequestQueue(getApplicationContext());
        url = getIntent().getStringExtra("url");
        position = getIntent().getIntExtra("position",0);
        nameTextView = findViewById(R.id.pokemon_name);
        numberTextView = findViewById(R.id.pokemon_number);
        type1TextView = findViewById(R.id.pokemon_type1);
        type2TextView = findViewById(R.id.pokemon_type2);
        button = findViewById(R.id.btnCatchIt);
        imagePokemon = findViewById(R.id.ivPokemonImage);

        descriptionUrl = "https://pokeapi.co/api/v2/pokemon-species/" + (position + 1);
        tvDescription = findViewById(R.id.tvDescription);

        load();
        checkCatch();
    }

    public void load() {
        type1TextView.setText("");
        type2TextView.setText("");

        // First request - Name, id, image
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    nameTextView.setText(response.getString("name").toUpperCase());
                    numberTextView.setText(String.format("#%03d", response.getInt("id")));
                    JSONObject sprites = response.getJSONObject("sprites");
                    imageUrl = sprites.getString("front_default");
                    new DownloadSpriteTask().execute(imageUrl);

                    JSONArray typeEntries = response.getJSONArray("types");
                    for (int i = 0; i < typeEntries.length(); i++) {
                        JSONObject typeEntry = typeEntries.getJSONObject(i);
                        int slot = typeEntry.getInt("slot");
                        String type = typeEntry.getJSONObject("type").getString("name");

                        if (slot == 1) {
                            type1TextView.setText(type);
                        }
                        else if (slot == 2) {
                            type2TextView.setText(type);
                        }
                    }
                } catch (JSONException e) {
                    Log.e("cs50", "Pokemon json error", e);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("cs50", "Pokemon details error", error);
            }
        });

        requestQueue.add(request);

        // Second request - description
        JsonObjectRequest request2 = new JsonObjectRequest(Request.Method.GET, descriptionUrl, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray descriptionArray = response.getJSONArray("flavor_text_entries");
                    //JSONObject descriptionObject = descriptionArray.getJSONObject(0);
                    tvDescription.setText(descriptionArray.getJSONObject(0).getString("flavor_text"));
                    /*nameTextView.setText(response.getString("name"));
                    numberTextView.setText(String.format("#%03d", response.getInt("id")));
                    JSONObject sprites = response.getJSONObject("sprites");
                    imageUrl = sprites.getString("front_default");
                    new DownloadSpriteTask().execute(imageUrl);

                    JSONArray typeEntries = response.getJSONArray("types");
                    for (int i = 0; i < typeEntries.length(); i++) {
                        JSONObject typeEntry = typeEntries.getJSONObject(i);
                        int slot = typeEntry.getInt("slot");
                        String type = typeEntry.getJSONObject("type").getString("name");

                        if (slot == 1) {
                            type1TextView.setText(type);
                        }
                        else if (slot == 2) {
                            type2TextView.setText(type);
                        }
                    }*/
                } catch (Exception e) {
                    Log.e("cs50", "Pokemon json error", e);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("cs50", "Pokemon description error", error);
            }
        });

        requestQueue.add(request2);
    }

    public void checkCatch(){
        if (PokedexAdapter.originalData.get(position).getCatchPokemon()){
            button.setText("Release");
        }else{
            button.setText("Catch");
        }
    }

    public void toggleCatch(View view) {
        SharedPreferences preferences = getApplicationContext().getSharedPreferences(prefsName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        if (PokedexAdapter.originalData.get(position).getCatchPokemon()){
            PokedexAdapter.originalData.get(position).setCatchPokemon(false);
            editor.putBoolean(PokedexAdapter.originalData.get(position).getName(), false);
            button.setText("Catch");
        }else{
            PokedexAdapter.originalData.get(position).setCatchPokemon(true);
            editor.putBoolean(PokedexAdapter.originalData.get(position).getName(), true);
            button.setText("Release");
        }

        editor.apply();
    }

    private class DownloadSpriteTask extends AsyncTask<String, Void, Bitmap> {
        @Override
        protected Bitmap doInBackground(String... strings) {
            try {
                URL url = new URL(strings[0]);
                return BitmapFactory.decodeStream(url.openStream());
            }
            catch (IOException e) {
                Log.e("cs50", "Download sprite error", e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            // load the bitmap into the ImageView!
            imagePokemon.setImageBitmap(bitmap);
        }
    }
}
