package com.example.pokeapp.retrofit.api_retrofit

data class PokemonResponse(
    val id: Int,
    val name: String,
    val height: Int,
    val weight: Int,
    val types: List<TypeSlot>,
    val sprites: Sprites   // mostrar imagen
)

data class TypeSlot(
    val slot: Int,
    val type: TypeInfo
)

data class TypeInfo(
    val name: String
)

data class Sprites(
    val front_default: String,
    val back_default: String,
    val front_shiny: String
)

