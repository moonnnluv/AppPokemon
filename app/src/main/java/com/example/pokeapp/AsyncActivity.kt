package com.example.pokeapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.pokeapp.retrofit.RetrofitActivity
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale

class AsyncActivity : AppCompatActivity() {

    private lateinit var etID2: EditText
    private lateinit var btBuscar2: Button
    private lateinit var tvResultado2: TextView
    private lateinit var progressBar2: ProgressBar

    private lateinit var btIrMain2: Button
    private lateinit var btIrAsync2: Button
    private lateinit var btIrRetrofit2: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_async)

        // Tu root en el XML es @+id/main2
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main2)) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom); insets
        }

        etID2 = findViewById(R.id.etID2)
        btBuscar2 = findViewById(R.id.btBuscar2)
        tvResultado2 = findViewById(R.id.tvResultado2)
        progressBar2 = findViewById(R.id.progressBar2)

        supportActionBar?.title = "PokeApp AsyncActivity"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        btBuscar2.setOnClickListener {
            val query = etID2.text.toString().trim().lowercase(Locale.ROOT)
            if (query.isEmpty()) {
                etID2.error = getString(R.string.ingresa_id)
            } else {
                realizarBusquedaConAsync(query)
            }
        }

        cambiarActividadesSalir()
    }

    // Orquesta: launch + async/await
    private fun realizarBusquedaConAsync(query: String) {
        progressBar2.visibility = View.VISIBLE
        tvResultado2.text = getString(R.string.resultado)

        lifecycleScope.launch {
            try {
                val deferredJson: Deferred<JSONObject?> = obtenerPokemonAsync(query)
                val obj = deferredJson.await()

                if (obj != null) {
                    val nombre = obj.optString("name", "—")
                    val id = obj.optInt("id", -1)
                    val alturaM = obj.optInt("height", 0) / 10.0
                    val pesoKg = obj.optInt("weight", 0) / 10.0

                    // tipos
                    val tiposArr = obj.optJSONArray("types")
                    val tipos = mutableListOf<String>()
                    if (tiposArr != null) {
                        for (i in 0 until tiposArr.length()) {
                            val t = tiposArr.getJSONObject(i)
                                .getJSONObject("type")
                                .optString("name", "")
                            if (t.isNotEmpty()) tipos.add(t)
                        }
                    }
                    val tiposStr = if (tipos.isEmpty()) "—" else tipos.joinToString(", ")

                    tvResultado2.text =
                        "${getString(R.string.resultado)} " +
                                "${getString(R.string.nombre)} ${nombre.replaceFirstChar { it.uppercase() }} | " +
                                "${getString(R.string.tipo)} $tiposStr\n" +
                                "ID: $id | Altura: ${alturaM} m | Peso: ${pesoKg} kg"
                } else {
                    tvResultado2.text = "${getString(R.string.resultado)} Pokémon no encontrado."
                }
            } catch (e: Exception) {
                tvResultado2.text = "${getString(R.string.resultado)} Error inesperado."
                Log.e("AsyncPoke", "Error en búsqueda async", e)
            } finally {
                progressBar2.visibility = View.GONE
            }
        }
    }

    // Devuelve el JSON del Pokémon en segundo plano
    private fun obtenerPokemonAsync(query: String): Deferred<JSONObject?> {
        return lifecycleScope.async(Dispatchers.IO) {
            try {
                val json = fetchPokemonJson(query)
                if (json != null) JSONObject(json) else null
            } catch (e: IOException) {
                Log.e("AsyncPoke", "IOException", e); null
            } catch (e: JSONException) {
                Log.e("AsyncPoke", "JSONException", e); null
            } catch (e: Exception) {
                Log.e("AsyncPoke", "Genérica", e); null
            }
        }
    }

    // Llamada HTTP a PokéAPI
    @Throws(IOException::class)
    private fun fetchPokemonJson(idOrName: String): String? {
        val url = URL("https://pokeapi.co/api/v2/pokemon/$idOrName")
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "GET"
        conn.connectTimeout = 10000
        conn.readTimeout = 10000
        return try {
            val code = conn.responseCode
            val stream = if (code in 200..299) conn.inputStream else conn.errorStream
            val reader = BufferedReader(InputStreamReader(stream))
            val sb = StringBuilder()
            var line: String?
            while (reader.readLine().also { line = it } != null) sb.append(line)
            reader.close()
            sb.toString()
        } finally { conn.disconnect() }
    }

    private fun cambiarActividadesSalir() {
        // Botones para navegar a las otras pantallas
        btIrMain2 = findViewById(R.id.btIrMain2)
        btIrMain2.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        } // FIN del botón btIrMain2

        btIrAsync2 = findViewById(R.id.btIrAsync2)
        btIrAsync2.setOnClickListener {
            val intent = Intent(this, AsyncActivity::class.java)
            startActivity(intent)
        } // FIN del botón btIrAsync2

        btIrRetrofit2 = findViewById(R.id.btIrRetrofit2)
        btIrRetrofit2.setOnClickListener {
            val intent = Intent(this, RetrofitActivity::class.java)
            startActivity(intent)
        } // FIN del botón btIrRetrofit2

        // Cerrar App
        val btSalir2 = findViewById<Button>(R.id.btSalir2)
        btSalir2.setOnClickListener {
            finishAffinity()
        } // FIN del botón btSalir2
    }
}
