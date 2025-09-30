package com.example.pokeapp.retrofit.api_retrofit

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface PokeApiService {
    @GET("pokemon/{id}")
    suspend fun getPokemon(@Path("id") id: String): Response<PokemonResponse>
}
