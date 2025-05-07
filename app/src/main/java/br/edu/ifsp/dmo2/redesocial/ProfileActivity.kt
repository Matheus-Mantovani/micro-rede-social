package br.edu.ifsp.dmo2.redesocial

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import br.edu.ifsp.dmo2.redesocial.databinding.ActivityProfileBinding
import br.edu.ifsp.dmo2.redesocial.databinding.DialogMudarSenhaBinding
import br.edu.ifsp.dmo2.redesocial.util.Base64Converter
import com.google.firebase.Firebase
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

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

        loadProfile()
        setOnClickListeners()
    }

    private fun loadProfile() {
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
                        binding.layoutUsername.hint = document.data!!["username"].toString()
                        binding.layoutFullname.hint = document.data!!["nomeCompleto"].toString()
                    }
                }
        }
    }

    private fun setOnClickListeners() {
        binding.buttonAlterarSenha.setOnClickListener {
            val dialogBinding = DialogMudarSenhaBinding.inflate(layoutInflater)

            AlertDialog.Builder(this)
                .setTitle("Alterar senha")
                .setView(dialogBinding.root)
                .setPositiveButton("Confirmar") { dialog, _ ->
                    if(firebaseAuth.currentUser != null) {
                        val user = firebaseAuth.currentUser
                        val senhaAtual = dialogBinding.editTextSenhaAtual.text.toString()
                        val novaSenha = dialogBinding.editTextNovaSenha.text.toString()
                        val repetirNovaSenha = dialogBinding.editTextRepetirNovaSenha.text.toString()

                        val credential = EmailAuthProvider.getCredential(user?.email!!, senhaAtual)

                        if(camposSenhaPreenchidos(dialogBinding)) {
                            user.reauthenticate(credential)
                                .addOnCompleteListener { authTask ->
                                    if(authTask.isSuccessful) {
                                        if(novaSenha == repetirNovaSenha) {
                                            user.updatePassword(novaSenha)
                                                .addOnCompleteListener { updateTask ->
                                                    if(updateTask.isSuccessful) {
                                                        Toast.makeText(this, "Senha alterada com sucesso!", Toast.LENGTH_LONG).show()
                                                    } else {
                                                        Toast.makeText(this, "Erro ao alterar senha, tente novamente mais tarde", Toast.LENGTH_LONG).show()
                                                        Log.e("ERRO", updateTask.exception.toString())
                                                    }
                                                }
                                        } else {
                                            Toast.makeText(this, "As novas senhas estão diferentes", Toast.LENGTH_LONG).show()
                                        }
                                    } else {
                                        Toast.makeText(this, "Senha incorreta", Toast.LENGTH_LONG).show()
                                    }
                                }
                        } else {
                            Toast.makeText(this, "Preencha todos os campos para alterar sua senha", Toast.LENGTH_LONG).show()
                        }
                    }

                }
                .setNegativeButton("Cancelar") { dialog, _ ->
                    dialog.dismiss()
                }
                .create()
                .show()
        }

        binding.buttonSave.setOnClickListener {
            if (firebaseAuth.currentUser != null) {
                lifecycleScope.launch {
                    var username = binding.editTextUserName.text.toString()
                    var nomeCompleto = binding.editTextFullName.text.toString()
                    val email = firebaseAuth.currentUser!!.email.toString()
                    val fotoPerfilString = Base64Converter.drawableToString(
                        binding.imageProfilePic.drawable
                    )

                    val db = Firebase.firestore

                    if (nomeCompleto.isBlank()) {
                        nomeCompleto = recuperarCampo(email, "nomeCompleto")
                    }

                    if (username.isBlank()) {
                        username = recuperarCampo(email, "username")
                    }

                    val dados = hashMapOf(
                        "nomeCompleto" to nomeCompleto,
                        "username" to username,
                        "fotoPerfil" to fotoPerfilString
                    )
                    db.collection("usuarios").document(email)
                        .set(dados)
                        .addOnSuccessListener {
                            Toast.makeText(this@ProfileActivity, "Perfil atualizado com sucesso!", Toast.LENGTH_LONG).show()
                            startActivity(Intent(this@ProfileActivity, HomeActivity::class.java))
                            finish()
                        }
                }
            }
        }

        binding.buttonVoltar.setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }
    }

    private fun camposSenhaPreenchidos(dialogBinding: DialogMudarSenhaBinding): Boolean {
        return dialogBinding.editTextSenhaAtual.text.toString().isNotBlank() && dialogBinding.editTextNovaSenha.text.toString().isNotBlank() && dialogBinding.editTextRepetirNovaSenha.text.toString().isNotBlank()
    }

    private suspend fun recuperarCampo(email: String, campo: String): String {
        val doc = Firebase.firestore.collection("usuarios").document(email).get().await()
        return doc.getString(campo).orEmpty()
    }

}