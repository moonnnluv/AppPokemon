package com.example.pokeapp.retrofit

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.pokeapp.AsyncActivity
import com.example.pokeapp.MainActivity
import com.example.pokeapp.R
import com.example.pokeapp.retrofit.api_retrofit.PokeApiService
import com.example.pokeapp.retrofit.api_retrofit.PokemonResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.Locale

class RetrofitActivity : AppCompatActivity() {

    private lateinit var etID: EditText
    private lateinit var btBuscar: Button
    private lateinit var tvResultado: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var service: PokeApiService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_retrofit)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom); insets
        }

        etID = findViewById(R.id.etID)
        btBuscar = findViewById(R.id.btBuscar)
        tvResultado = findViewById(R.id.tvResultado)
        progressBar = findViewById(R.id.progressBar)

        supportActionBar?.title = "PokeApp RetrofitActivity"

        val retrofit = Retrofit.Builder()
            .baseUrl("https://pokeapi.co/api/v2/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        service = retrofit.create(PokeApiService::class.java)

        btBuscar.setOnClickListener {
            val q = etID.text.toString().trim().lowercase(Locale.ROOT)
            if (q.isEmpty()) etID.error = getString(R.string.ingresa_id)
            else buscarPokemon(q)
        }
    }

    private fun buscarPokemon(query: String) {
        progressBar.visibility = View.VISIBLE
        tvResultado.text = getString(R.string.resultado)

        service.getPokemon(query).enqueue(object : Callback<PokemonResponse> {
            override fun onResponse(
                call: Call<PokemonResponse>,
                response: Response<PokemonResponse>
            ) {
                if (response.isSuccessful && response.body() != null) {
                    val p = response.body()!!
                    val tipos = p.types.sortedBy { it.slot }.joinToString(", ") { it.type.name }
                    val alturaM = p.height / 10.0
                    val pesoKg = p.weight / 10.0
                    tvResultado.text =
                        "${getString(R.string.resultado)} " +
                                "${getString(R.string.nombre)} ${p.name.replaceFirstChar { it.uppercase() }} | " +
                                "${getString(R.string.tipo)} $tipos\n" +
                                "ID: ${p.id} | Altura: ${alturaM} m | Peso: ${pesoKg} kg"
                } else {
                    tvResultado.text = "${getString(R.string.resultado)} Pokémon no encontrado (${response.code()})."
                }
                progressBar.visibility = View.GONE
            }

            override fun onFailure(call: Call<PokemonResponse>, t: Throwable) {
                tvResultado.text = "${getString(R.string.resultado)} Error de conexión."
                progressBar.visibility = View.GONE
            }
        })
    }

    private fun cambiarActividadesSalir() {
        // Botones para navegar a las otras pantallas
        btIrMain3 = findViewById(R.id.btIrMain3)
        btIrMain3.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        } // FIN del botón btIrMain3

        btIrAsync3 = findViewById(R.id.btIrAsync3)
        btIrAsync3.setOnClickListener {
            val intent = Intent(this, AsyncActivity::class.java)
            startActivity(intent)
        } // FIN del botón btIrAsync3

        btIrRetrofit3 = findViewById(R.id.btIrRetrofit3)
        btIrRetrofit3.setOnClickListener {
            val intent = Intent(this, RetrofitActivity::class.java)
            startActivity(intent)
        } // FIN del botón btIrRetrofit3

        // Cerrar App
        val btSalir3 = findViewById<Button>(R.id.btSalir3)
        btSalir3.setOnClickListener {
            finishAffinity()
        } // FIN del botón btSalir3
    }
}
