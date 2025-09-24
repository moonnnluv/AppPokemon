package com.example.pokeapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : AppCompatActivity() {

    private lateinit var etID: EditText // Declaración de la variable etID como una propiedad de la clase
    private lateinit var btBuscar: Button // Declaración de la variable btBuscar como una propiedad de la clase
    private lateinit var tvResultado: TextView // Declaración de la variable tvResultado como una propiedad de la clase
    private lateinit var progressBar: ProgressBar // Declaración de la variable progressBar como una propiedad de la clase

    private lateinit var btIrMain: Button // Declaración de la variable btIrMain como una propiedad de la clase
    private lateinit var btIrAsync: Button // Declaración de la variable btIrAsync como una propiedad de la clase
    private lateinit var btIrRetrofit: Button // Declaración de la variable btIrRetrofit como una propiedad de la clase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        etID = findViewById(R.id.etID) // Inicialización de la variable etID
        btBuscar = findViewById(R.id.btBuscar) // Inicialización de la variable btBuscar
        tvResultado = findViewById(R.id.tvResultado) // Inicialización de la variable tvResultado
        progressBar = findViewById(R.id.progressBar) // Inicialización de la variable progressBar

        // 1. Establece el título de la ActionBar
        supportActionBar?.title = "PokeApp MainActivity"

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        btBuscar.setOnClickListener { // Agregar un listener al botón btBuscar
            val query = etID.text.toString().trim().lowercase() // Obtener el texto del EditText
            if (query.isNotEmpty()) { // Verificar si la cadena no está vacía
                obtenerPokemonManualmente(query) // Llamar a la función para obtener el Pokémon
            } else {
                // Manejar el caso en el que la cadena está vacía
                Toast.makeText(this, getString(R.string.ingresa_id), Toast.LENGTH_SHORT).show()
            }
        } // FIN del botón btBuscar

        cambiarActividadesSalir() // Llama a la función para cambiar las actividades
    } // Fin del onCreate

    // Función para obtener datos de un Pokémon y mostrar resultado
    private fun obtenerPokemonManualmente(idOrName: String) {
        progressBar.visibility = View.VISIBLE // Mostrar la barra de progreso
        tvResultado.text = getString(R.string.resultado) // Limpiar/encabezado

        // Corrutina igual que el ejemplo de tu profe (lifecycleScope + context switch)
        lifecycleScope.launch {
            try {
                // Operación de red en un hilo de fondo (IO)
                val jsonResponse = withContext(Dispatchers.IO) {
                    fetchPokemonJson(idOrName) // Llamar a la función para obtener el JSON
                }

                // Parsear JSON y actualizar UI en el hilo principal
                if (jsonResponse != null) {
                    val obj = JSONObject(jsonResponse) // Parsear el JSON

                    val nombre = obj.optString("name", "—") // nombre
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

                    tvResultado.text = "${getString(R.string.resultado)} " +
                            "${getString(R.string.nombre)} $nombre | " +
                            "${getString(R.string.tipo)} $tiposStr"
                } else {
                    tvResultado.text = "${getString(R.string.resultado)} No se pudo obtener la información."
                }

            } catch (e: IOException) {
                tvResultado.text = "${getString(R.string.resultado)} Error de conexión. Verifica tu internet."
                e.printStackTrace()
            } catch (e: JSONException) {
                tvResultado.text = "${getString(R.string.resultado)} Error al parsear los datos."
                e.printStackTrace()
            } catch (e: Exception) {
                tvResultado.text = "${getString(R.string.resultado)} Ocurrió un error inesperado."
                e.printStackTrace()
            } finally {
                progressBar.visibility = View.GONE // Ocultar la barra de progreso
            }
        }
    } // Fin de obtenerPokemonManualmente

    // Esta función se ejecutará en un hilo de fondo gracias a withContext(Dispatchers.IO)
    @Throws(IOException::class)
    private fun fetchPokemonJson(idOrName: String): String? {
        val url = URL("https://pokeapi.co/api/v2/pokemon/$idOrName") // URL de PokéAPI
        val connection = url.openConnection() as HttpURLConnection // Abrir la conexión
        connection.requestMethod = "GET" // Método de solicitud
        connection.connectTimeout = 10000 // 10 segundos de timeout
        connection.readTimeout = 10000    // 10 segundos de timeout

        return try {
            val code = connection.responseCode
            val inputStream = if (code in 200..299) connection.inputStream else connection.errorStream
            val reader = BufferedReader(InputStreamReader(inputStream))
            val sb = StringBuilder()
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                sb.append(line)
            }
            reader.close()
            sb.toString()
        } finally {
            connection.disconnect() // Desconectar la conexión
        }
    } // Fin de fetchPokemonJson

    // Función para cambiar las actividades (igual patrón que tu profe)
    private fun cambiarActividadesSalir() {
        // Botones para navegar a las otras pantallas
        btIrMain = findViewById(R.id.btIrMain)
        btIrMain.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        } // FIN del botón btIrMain

        btIrAsync = findViewById(R.id.btIrAsync)
        btIrAsync.setOnClickListener {
            val intent = Intent(this, AsyncActivity::class.java)
            startActivity(intent)
        } // FIN del botón btIrAsync

        btIrRetrofit = findViewById(R.id.btIrRetrofit)
        btIrRetrofit.setOnClickListener {
            val intent = Intent(this, RetrofitActivity::class.java)
            startActivity(intent)
        } // FIN del botón btIrRetrofit

        // Cerrar App
        val btSalir = findViewById<Button>(R.id.btSalir)
        btSalir.setOnClickListener {
            finishAffinity()
        } // FIN del botón btSalir
    }
} // Fin de la clase MainActivity
