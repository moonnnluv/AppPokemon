package com.miapp.appdivisaskotlinbasico.ActivitysNormales

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
import com.example.pokeapp.MainActivity
import com.miapp.appdivisaskotlinbasico.R
import com.miapp.appdivisaskotlinbasico.Retrofit.RetrofitActivity
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

class AsyncActivity : AppCompatActivity() {

    private lateinit var etID2: EditText        // (antes etClp2)
    private lateinit var btBuscar2: Button      // (antes btCalcular2)
    private lateinit var tvResultado2: TextView
    private lateinit var progressBar2: ProgressBar

    private lateinit var btIrMain2: Button
    private lateinit var btIrAsync2: Button
    private lateinit var btIrRetrofit2: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_async)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // IDs NUEVOS (cámbialos en tu XML)
        etID2 = findViewById(R.id.etID2)
        btBuscar2 = findViewById(R.id.btBuscar2)
        tvResultado2 = findViewById(R.id.tvResultado2)
        progressBar2 = findViewById(R.id.progressBar2)

        // 1. Título ActionBar (como el profe)
        supportActionBar?.title = "PokeApp AsyncActivity"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        btBuscar2.setOnClickListener {
            val query = etID2.text.toString().trim().lowercase()
            if (query.isNotEmpty()) {
                realizarBusquedaConAsync(query)   // igual patrón: orquesta con launch + async
            } else {
                Toast.makeText(this, getString(R.string.ingresa_id), Toast.LENGTH_SHORT).show()
            }
        }

        cambiarActividadesSalir()
    } // Fin onCreate

    // ===== Orquestador estilo profe: launch -> async(IO) -> await =====
    private fun realizarBusquedaConAsync(idOrName: String) {
        progressBar2.visibility = View.VISIBLE
        tvResultado2.text = getString(R.string.resultado)

        lifecycleScope.launch {
            Log.d("AsyncPoke", "launch: iniciar búsqueda con async")
            try {
                Log.d("AsyncPoke", "Llamando a obtenerPokemonAsync()...")
                val deferredJson: Deferred<String?> = obtenerPokemonAsync(idOrName)

                Log.d("AsyncPoke", "Esperando await() del JSON…")
                val json = deferredJson.await()
                Log.d("AsyncPoke", "JSON recibido: ${json?.take(60)}...")

                if (json != null) {
                    try {
                        val obj = JSONObject(json)
                        val nombre = obj.optString("name", "—")

                        // tipos
                        val tiposArray = obj.optJSONArray("types")
                        val tipos = mutableListOf<String>()
                        if (tiposArray != null) {
                            for (i in 0 until tiposArray.length()) {
                                val tipoObj = tiposArray.getJSONObject(i)
                                val tipo = tipoObj.getJSONObject("type").optString("name", "")
                                if (tipo.isNotEmpty()) tipos.add(tipo)
                            }
                        }
                        val tiposStr = if (tipos.isEmpty()) "—" else tipos.joinToString(", ")

                        tvResultado2.text = "${getString(R.string.resultado)} " +
                                "${getString(R.string.nombre)} $nombre | " +
                                "${getString(R.string.tipo)} $tiposStr"

                    } catch (e: JSONException) {
                        tvResultado2.text = "${getString(R.string.resultado)} Error al parsear los datos."
                        Log.e("AsyncPoke", "JSONException parseando JSON", e)
                    }
                } else {
                    tvResultado2.text = "${getString(R.string.resultado)} No se pudo obtener la información."
                }

            } catch (e: Exception) {
                tvResultado2.text = "${getString(R.string.resultado)} Ocurrió un error inesperado."
                Log.e("AsyncPoke", "Excepción en realizarBusquedaConAsync", e)
            } finally {
                progressBar2.visibility = View.GONE
                Log.d("AsyncPoke", "Fin realizarBusquedaConAsync")
            }
        }
        Log.d("AsyncPoke", "realizarBusquedaConAsync retornó (launch activo)")
    }

    /**
     * Igual que el ejemplo del profe: función que usa async(IO) y devuelve un Deferred<String?>
     * con el JSON de PokéAPI o null si hay error.
     */
    private fun obtenerPokemonAsync(idOrName: String): Deferred<String?> {
        return lifecycleScope.async(Dispatchers.IO) {
            Log.d("AsyncPoke", "Dentro de async(IO) en obtenerPokemonAsync() - hilo: ${Thread.currentThread().name}")
            try {
                fetchPokemonJson(idOrName)
            } catch (e: IOException) {
                Log.e("AsyncPoke", "IOException en async", e)
                null
            } catch (e: Exception) {
                Log.e("AsyncPoke", "Excepción genérica en async", e)
                null
            }
        }
    }

    // === Equivalente a fetchIndicadoresJson(), pero para PokéAPI ===
    @Throws(IOException::class)
    private fun fetchPokemonJson(idOrName: String): String? {
        Log.d("AsyncPoke", "fetchPokemonJson() en hilo: ${Thread.currentThread().name}")
        val url = URL("https://pokeapi.co/api/v2/pokemon/$idOrName")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.connectTimeout = 10000
        connection.readTimeout = 10000

        return try {
            val code = connection.responseCode
            val stream = if (code in 200..299) connection.inputStream else connection.errorStream
            val reader = BufferedReader(InputStreamReader(stream))
            val sb = StringBuilder()
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                sb.append(line)
            }
            reader.close()
            sb.toString()
        } finally {
            connection.disconnect()
        }
    }

    // ===== Navegación (mismo patrón del profe, solo IDs con sufijo 2) =====
    private fun cambiarActividadesSalir() {
        btIrMain2 = findViewById(R.id.btIrMain2)
        btIrMain2.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        btIrAsync2 = findViewById(R.id.btIrAsync2)
        btIrAsync2.setOnClickListener {
            val intent = Intent(this, AsyncActivity::class.java)
            startActivity(intent)
        }

        btIrRetrofit2 = findViewById(R.id.btIrRetrofit2)
        btIrRetrofit2.setOnClickListener {
            val intent = Intent(this, RetrofitActivity::class.java)
            startActivity(intent)
        }

        val btSalir2 = findViewById<Button>(R.id.btSalir2)
        btSalir2.setOnClickListener { finishAffinity() }
    }
}
