package com.example.pokedex;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;

import com.example.pokedex.models.Pokemon;
import com.example.pokedex.models.PokemonResposta;
import com.example.pokedex.pokeapi.PokeapiService;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "POKÉDEX";

    private Retrofit retrofit;
    private RecyclerView recyclerView;
    private ListaPokemonAdapter listaPokemonAdapter;
    private int offset;
    private boolean carregar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        listaPokemonAdapter = new ListaPokemonAdapter(this);
        recyclerView.setAdapter(listaPokemonAdapter);
        recyclerView.setHasFixedSize(true);
        GridLayoutManager layoutManager = new GridLayoutManager(this,3);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if(dy>0){
                    int visibleItemCount = layoutManager.getChildCount();
                    int totalItemCount = layoutManager.getItemCount();
                    int pastVisibleItems = layoutManager.findFirstCompletelyVisibleItemPosition();

                    if(carregar){
                        if((visibleItemCount + pastVisibleItems)>=totalItemCount){
                            Log.i(TAG,"FIM");
                            carregar = false;
                            offset +=20;
                            obterDados(offset);
                        }
                    }
                }
            }
        });
        retrofit = new Retrofit.Builder()
                .baseUrl("https://pokeapi.co/api/v2/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        carregar = true;
        offset = 0;

        obterDados(offset);
    }

    private void obterDados(int offset){
        PokeapiService service =  retrofit.create(PokeapiService.class);
       Call<PokemonResposta> pokemonRespostaCall = service.obterListaPokemon(20,offset);

       pokemonRespostaCall.enqueue(new Callback<PokemonResposta>() {
           @Override
           public void onResponse(Call<PokemonResposta> call, Response<PokemonResposta> response) {
               carregar = true;
               if(response.isSuccessful()){
                   PokemonResposta pokemonResposta = response.body();
                   ArrayList<Pokemon> listaPokemon = pokemonResposta.getResults();

                   listaPokemonAdapter.adicionarListaPokemon(listaPokemon);
                   
               }else{
                   Log.e(TAG,"onResponse: "+ response.errorBody());
               }
           }

           @Override
           public void onFailure(Call<PokemonResposta> call, Throwable t) {
               carregar = true;
                Log.e(TAG,"onFailure: "+ t.getMessage());
           }
       });
    }
}