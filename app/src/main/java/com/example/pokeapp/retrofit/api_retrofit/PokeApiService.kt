package com.example.pokeapp.retrofit.api_retrofit

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface PokeApiService {
    @GET("pokemon/{idOrName}")
    fun getPokemon(@Path("idOrName") idOrName: String): Call<PokemonResponse>
}
