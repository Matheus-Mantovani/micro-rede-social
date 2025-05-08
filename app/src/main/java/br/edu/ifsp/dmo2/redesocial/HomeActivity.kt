package br.edu.ifsp.dmo2.redesocial

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import br.edu.ifsp.dmo2.redesocial.adapter.PostAdapter
import br.edu.ifsp.dmo2.redesocial.databinding.ActivityHomeBinding
import br.edu.ifsp.dmo2.redesocial.model.Post
import br.edu.ifsp.dmo2.redesocial.util.Base64Converter
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import androidx.core.view.isVisible

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private val firebaseAuth = FirebaseAuth.getInstance()
    private lateinit var adapter: PostAdapter
    private var posts = ArrayList<Post>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setOnClickListener()
    }

    private fun setOnClickListener() {
        binding.buttonCarregarFeed.setOnClickListener {
            val db = Firebase.firestore
            db.collection("posts").get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val document = task.result
                        posts = ArrayList<Post>()
                        for (document in document.documents) {
                            val imageString = document.data!!["imageString"].toString()
                            val bitmap = Base64Converter.stringToBitmap(imageString)
                            val descricao = document.data!!["descricao"].toString()
                            posts.add(Post(descricao, bitmap))
                        }
                        adapter = PostAdapter(posts.toTypedArray())
                        binding.recyclerView.layoutManager = LinearLayoutManager(this)
                        binding.recyclerView.adapter = adapter
                    }
                }
        }

        binding.buttonSair.setOnClickListener {
            firebaseAuth.signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        binding.buttonProfile.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
            finish()
        }

        binding.buttonNovoPost.setOnClickListener {
            startActivity(Intent(this, NewPostActivity::class.java))
            finish()
        }

        binding.buttonMenu.setOnClickListener {
            if(binding.buttonProfile.isVisible) {
                binding.buttonProfile.visibility = View.INVISIBLE
                binding.buttonSair.visibility = View.INVISIBLE
                binding.buttonNovoPost.visibility = View.INVISIBLE
            } else {
                binding.buttonProfile.visibility = View.VISIBLE
                binding.buttonSair.visibility = View.VISIBLE
                binding.buttonNovoPost.visibility = View.VISIBLE
            }
        }
    }
}