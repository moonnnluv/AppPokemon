package com.example.pokeapp.retrofit

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.pokeapp.R
import com.example.pokeapp.retrofit.api_retrofit.PokeApiService
import com.example.pokeapp.retrofit.api_retrofit.PokemonResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.Locale

class RetrofitActivity : AppCompatActivity() {

    private lateinit var etPokemonId: EditText
    private lateinit var btBuscarPokemon: Button
    private lateinit var tvResultadoPokemon: TextView
    private lateinit var progressBarPokemon: ProgressBar
    private lateinit var ivPokemon: ImageView   // 👈 nuevo

    private var apiService: PokeApiService? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_retrofit)

        etPokemonId = findViewById(R.id.etPokemonId)
        btBuscarPokemon = findViewById(R.id.btBuscarPokemon)
        tvResultadoPokemon = findViewById(R.id.tvResultadoPokemon)
        progressBarPokemon = findViewById(R.id.progressBarPokemon)
        ivPokemon = findViewById(R.id.ivPokemon) // 👈 inicialización de imagen

        supportActionBar?.title = "Pokédex Retrofit"

        setupRetrofit()

        btBuscarPokemon.setOnClickListener {
            val id = etPokemonId.text.toString()
            if (id.isNotEmpty()) {
                buscarPokemon(id.lowercase(Locale.ROOT))
            } else {
                Toast.makeText(this, "Ingresa un ID o nombre", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupRetrofit() {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://pokeapi.co/api/v2/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        apiService = retrofit.create(PokeApiService::class.java)
    }

    private fun buscarPokemon(id: String) {
        if (apiService == null) {
            tvResultadoPokemon.text = "Error: API no inicializada."
            return
        }

        progressBarPokemon.visibility = View.VISIBLE
        tvResultadoPokemon.text = "Buscando..."

        lifecycleScope.launch {
            try {
                val pokemon = obtenerPokemonDesdeApi(id)
                if (pokemon != null) {
                    mostrarPokemon(pokemon) // 👈 función para mostrar todo
                } else {
                    tvResultadoPokemon.text = "No se encontró el Pokémon"
                }
            } catch (e: Exception) {
                tvResultadoPokemon.text = "Error: ${e.message}"
                Log.e("RetrofitActivity", "Error al buscar Pokémon", e)
            } finally {
                progressBarPokemon.visibility = View.GONE
            }
        }
    }

    private suspend fun obtenerPokemonDesdeApi(id: String): PokemonResponse? {
        val service = apiService ?: return null
        return withContext(Dispatchers.IO) {
            try {
                val response = service.getPokemon(id)
                if (response.isSuccessful) {
                    response.body()
                } else null
            } catch (e: Exception) {
                null
            }
        }
    }

    private fun mostrarPokemon(pokemon: PokemonResponse) {
        val tipos = pokemon.types.joinToString(", ") { it.type.name }

        tvResultadoPokemon.text = """
            ID: ${pokemon.id}
            Nombre: ${pokemon.name.replaceFirstChar { it.uppercase() }}
            Altura: ${pokemon.height / 10.0} m
            Peso: ${pokemon.weight / 10.0} kg
            Tipos: $tipos
        """.trimIndent()

        Glide.with(this)
            .load(pokemon.sprites.front_default)
            .into(ivPokemon)
    }
}
