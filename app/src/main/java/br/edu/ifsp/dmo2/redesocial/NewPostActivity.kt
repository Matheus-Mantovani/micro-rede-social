package br.edu.ifsp.dmo2.redesocial

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import br.edu.ifsp.dmo2.redesocial.databinding.ActivityNewPostBinding
import br.edu.ifsp.dmo2.redesocial.util.Base64Converter
import br.edu.ifsp.dmo2.redesocial.util.LocalizacaoHelper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class NewPostActivity : AppCompatActivity(), LocalizacaoHelper.Callback {
    private lateinit var binding: ActivityNewPostBinding
    private val firebaseAuth = FirebaseAuth.getInstance()
    private lateinit var galeria: ActivityResultLauncher<PickVisualMediaRequest>
    private val LOCATION_PERMISSION_REQUEST_CODE = 1001
    private var endereco: Address? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewPostBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupGalleryLauncher()
        setOnClickListener()
    }

    override fun onLocalizacaoRecebida(endereco: Address) {
        this.endereco = endereco
    }


    override fun onErro(mensagem: String) {
        System.out.println(mensagem)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(
            requestCode, permissions,
            grantResults
        )
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE &&
            grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            solicitarLocalizacao()
        } else {
            Toast.makeText(
                this, "Permissão de localização negada",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun solicitarLocalizacao() {
        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            )
            != PackageManager.PERMISSION_GRANTED
            &&
            ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_COARSE_LOCATION
            )
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        } else {
            val localizacaoHelper = LocalizacaoHelper(applicationContext)
            localizacaoHelper.obterLocalizacaoAtual(this)
        }
    }

    private fun solicitarLocalizacao(callback: (Address?) -> Unit) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        } else {
            val localizacaoHelper = LocalizacaoHelper(applicationContext)
            localizacaoHelper.obterLocalizacaoAtual(object : LocalizacaoHelper.Callback {
                override fun onLocalizacaoRecebida(endereco: Address) {
                    callback(endereco)
                }

                override fun onErro(mensagem: String) {
                    callback(null)
                }
            })
        }
    }


    private fun setupGalleryLauncher() {
        galeria = registerForActivityResult(
            ActivityResultContracts.PickVisualMedia()
        ) { uri ->
            if (uri != null) {
                binding.imageNewPost.setImageURI(uri)
            } else {
                Toast.makeText(this, "Nenhuma foto selecionada", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun setOnClickListener() {
        binding.buttonVoltar.setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }

        binding.buttonChangePhoto.setOnClickListener {
            galeria.launch(
                PickVisualMediaRequest(
                    ActivityResultContracts.PickVisualMedia.ImageOnly
                )
            )
        }

        binding.buttonPostar.setOnClickListener {
            binding.loadingOverlay.visibility = View.VISIBLE
            binding.buttonPostar.isEnabled = false

            val imagem = Base64Converter.drawableToString(binding.imageNewPost.drawable)
            val descricao = binding.inputDescricao.text.toString()

            if (binding.checkboxIncluirEndereco.isChecked) {
                solicitarLocalizacao { endereco ->
                    val cidade = endereco?.subAdminArea
                    enviarPost(imagem, descricao, cidade)
                }
            } else {
                enviarPost(imagem, descricao, null)
            }
        }
    }

    private fun enviarPost(imagem: String, descricao: String, cidade: String?) {
        val user = firebaseAuth.currentUser
        if (user != null) {
            CoroutineScope(Dispatchers.Main).launch {
                try {
                    val email = user.email!!
                    val documentSnapshot = Firebase.firestore
                        .collection("usuarios")
                        .document(email)
                        .get()
                        .await()

                    val username = documentSnapshot.getString("username") ?: ""

                    val dados = hashMapOf(
                        "username" to username,
                        "descricao" to descricao,
                        "cidade" to cidade,
                        "imageString" to imagem
                    )

                    Firebase.firestore.collection("posts").document().set(dados).await()

                    Toast.makeText(this@NewPostActivity, "Post enviado com sucesso!", Toast.LENGTH_LONG).show()
                    startActivity(Intent(this@NewPostActivity, HomeActivity::class.java))
                    finish()

                } catch (e: Exception) {
                    Toast.makeText(this@NewPostActivity, "Erro: ${e.message}", Toast.LENGTH_LONG).show()
                } finally {
                    binding.loadingOverlay.visibility = View.GONE
                    binding.buttonPostar.isEnabled = true
                }
            }
        }
    }

}