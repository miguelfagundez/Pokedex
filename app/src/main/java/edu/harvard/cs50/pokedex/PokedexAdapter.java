package edu.harvard.cs50.pokedex;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PokedexAdapter extends RecyclerView.Adapter<PokedexAdapter.PokedexViewHolder> implements Filterable {

    //List<Pokemon> filteredPokemon = new ArrayList<>();
    public static List<Pokemon> originalData = new ArrayList<>();
    int currentPosition = 0;
    private Context context;
    private String prefsName = "SharedPreference Pokemon";

    @Override
    public Filter getFilter() {
        return new PokemonFilter();
    }

    private class PokemonFilter extends Filter{

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {

            String pokemonConstrain = constraint.toString().toLowerCase();
            FilterResults results = new FilterResults();

            final List<Pokemon> tempList = originalData;
            int size = tempList.size();
            List<Pokemon> filteredList = new ArrayList<>(size);
            String filterableName;

            for(int i = 0; i < size; i++){
                filterableName = tempList.get(i).getName();
                if (filterableName.toLowerCase().contains(pokemonConstrain)){
                    filteredList.add(tempList.get(i));
                }
            }

            results.values = filteredList; // you need to create this variable!
            results.count = filteredList.size();
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            pokemon = (List<Pokemon>) results.values;
            notifyDataSetChanged();
        }
    }

    public class PokedexViewHolder extends RecyclerView.ViewHolder {
        public LinearLayout containerView;
        public TextView textView;
        public int position;

        PokedexViewHolder(View view) {
            super(view);

            containerView = view.findViewById(R.id.pokedex_row);
            textView = view.findViewById(R.id.pokedex_row_text_view);

            containerView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Pokemon current = (Pokemon) containerView.getTag();
                    Intent intent = new Intent(v.getContext(), PokemonActivity.class);
                    intent.putExtra("url", current.getUrl());
                    intent.putExtra("position", position);
                    v.getContext().startActivity(intent);
                }
            });
        }
    }

    private List<Pokemon> pokemon = new ArrayList<>();
    private RequestQueue requestQueue;

    PokedexAdapter(Context context) {
        this.context = context;
        requestQueue = Volley.newRequestQueue(context);
        Log.d("TAG", "onCreate: Size1 = " + originalData.size());
        loadPokemon();
        Log.d("TAG", "onCreate: Size2 = " + originalData.size());
    }

    public void loadPokemon() {
        String url = "https://pokeapi.co/api/v2/pokemon?limit=151";

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray results = response.getJSONArray("results");
                    for (int i = 0; i < results.length(); i++) {
                        JSONObject result = results.getJSONObject(i);
                        String name = result.getString("name");
                        pokemon.add(new Pokemon(
                            name.substring(0, 1).toUpperCase() + name.substring(1),
                            result.getString("url")
                        ));
                        originalData.add(new Pokemon(
                                name.substring(0, 1).toUpperCase() + name.substring(1),
                                result.getString("url")
                        ));
                    }

                    SharedPreferences preferences = context.getSharedPreferences(prefsName, Context.MODE_PRIVATE);
                    int size = originalData.size();
                    boolean pokemonCatch = false;
                    for (int i = 0; i < size; i++){
                        pokemonCatch = preferences.getBoolean(originalData.get(i).getName(),false);
                        originalData.get(i).setCatchPokemon(pokemonCatch);
                        pokemon.get(i).setCatchPokemon(pokemonCatch);
                    }

                    notifyDataSetChanged();
                } catch (JSONException e) {
                    Log.e("cs50", "Json error", e);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("cs50", "Pokemon list error", error);
            }
        });

        requestQueue.add(request);
        Log.d("LIST", "loadPokemon: List number of items = " + pokemon.size());
    }

    @NonNull
    @Override
    public PokedexViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.pokedex_row, parent, false);

        return new PokedexViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PokedexViewHolder holder, int position) {
        Pokemon current = pokemon.get(position);
        holder.textView.setText(current.getName());
        holder.containerView.setTag(current);
        holder.position = position;
    }

    @Override
    public int getItemCount() {
        return pokemon.size();
    }
}
