package br.edu.ifsp.dmo2.redesocial

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import br.edu.ifsp.dmo2.redesocial.databinding.ActivityProfileBinding
import br.edu.ifsp.dmo2.redesocial.util.Base64Converter
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore

class ProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProfileBinding
    private val firebaseAuth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val galeria = registerForActivityResult(
            ActivityResultContracts.PickVisualMedia()
        ) { uri ->
            if (uri != null) {
                binding.imageProfilePic.setImageURI(uri)
            } else {
                Toast.makeText(this, "Nenhuma foto selecionada", Toast.LENGTH_LONG).show()
            }
        }
        binding.buttonChangePhoto.setOnClickListener {
            galeria.launch(
                PickVisualMediaRequest(
                    ActivityResultContracts.PickVisualMedia.ImageOnly
                )
            )
        }

        loadProfilePic()
        setOnClickListeners()
    }

    private fun loadProfilePic() {
        if (firebaseAuth.currentUser != null) {
            val db = Firebase.firestore
            val email = firebaseAuth.currentUser!!.email.toString()
            db.collection("usuarios").document(email).get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val document = task.result
                        val imageString = document.data!!["fotoPerfil"].toString()
                        val bitmap = Base64Converter.stringToBitmap(imageString)
                        binding.imageProfilePic.setImageBitmap(bitmap)
                        binding.editTextUserName.hint = document.data!!["username"].toString()
                        binding.editTextFullName.hint = document.data!!["nomeCompleto"].toString()
                    }
                }
        }
    }

    private fun setOnClickListeners() {
        //binding.buttonChangePhoto.setOnClickListener {  }

        binding.buttonSave.setOnClickListener {
            if (firebaseAuth.currentUser != null) {
                val email = firebaseAuth.currentUser!!.email.toString()
                val username = binding.editTextUserName.text.toString()
                val nomeCompleto = binding.editTextFullName.text.toString()
                val fotoPerfilString = Base64Converter.drawableToString(
                    binding.imageProfilePic.drawable
                )
                val db = Firebase.firestore
                val dados = hashMapOf(
                    "nomeCompleto" to nomeCompleto,
                    "username" to username,
                    "fotoPerfil" to fotoPerfilString
                )
                db.collection("usuarios").document(email)
                    .set(dados)
                    .addOnSuccessListener {
                        startActivity(Intent(this, HomeActivity::class.java))
                        finish()
                    }
            }
        }
    }
}