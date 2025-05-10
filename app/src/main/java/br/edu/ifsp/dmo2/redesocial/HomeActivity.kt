package br.edu.ifsp.dmo2.redesocial

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import br.edu.ifsp.dmo2.redesocial.adapter.PostAdapter
import br.edu.ifsp.dmo2.redesocial.databinding.ActivityHomeBinding
import br.edu.ifsp.dmo2.redesocial.databinding.DialogFiltroBinding
import br.edu.ifsp.dmo2.redesocial.model.Post
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val postAdapter = PostAdapter()
    private var isLoading = false
    private var lastVisible: DocumentSnapshot? = null
    private var filtroCidade: String? = null
    private var isFiltering = false
    private val PAGE_SIZE = 5

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setOnClickListener()
        setupRecyclerView()
        loadPosts()
    }

    private fun setOnClickListener() {
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
            val visible = binding.buttonProfile.isVisible
            binding.buttonProfile.visibility = if (visible) View.INVISIBLE else View.VISIBLE
            binding.buttonSair.visibility = if (visible) View.INVISIBLE else View.VISIBLE
            binding.buttonNovoPost.visibility = if (visible) View.INVISIBLE else View.VISIBLE
        }

        binding.buttonFiltrar.setOnClickListener {
            val dialogBinding = DialogFiltroBinding.inflate(layoutInflater)

            AlertDialog.Builder(this)
                .setTitle("FILTRO")
                .setView(dialogBinding.root)
                .setPositiveButton("Confirmar") { _, _ ->
                    filtroCidade = dialogBinding.inputCidade.text.toString().trim()
                    isFiltering = true
                    lastVisible = null
                    postAdapter.clearPosts()
                    binding.buttonRemoverFiltro.visibility = View.VISIBLE
                    binding.buttonFiltrar.visibility = View.GONE
                    loadPosts()
                }
                .create()
                .show()
        }

        binding.buttonRemoverFiltro.setOnClickListener {
            filtroCidade = null
            isFiltering = false
            lastVisible = null
            postAdapter.clearPosts()
            binding.buttonRemoverFiltro.visibility = View.GONE
            binding.buttonFiltrar.visibility = View.VISIBLE
            loadPosts()
        }
    }

    private fun setupRecyclerView() {
        val layoutManager = LinearLayoutManager(this)
        binding.recyclerView.layoutManager = layoutManager
        binding.recyclerView.adapter = postAdapter

        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(rv: RecyclerView, dx: Int, dy: Int) {
                if (!rv.canScrollVertically(1) && !isLoading) {
                    loadPosts()
                }
            }
        })
    }

    private fun loadPosts() {
        isLoading = true

        var query = firestore.collection("posts")
            .orderBy("descricao", Query.Direction.ASCENDING)
            .limit(PAGE_SIZE.toLong())

        if(isFiltering && !filtroCidade.isNullOrEmpty()) {
            query = firestore.collection("posts")
                .whereEqualTo("cidade", filtroCidade)
                .orderBy("descricao", Query.Direction.ASCENDING)
                .limit(PAGE_SIZE.toLong())
        }

        lastVisible?.let {
            query = query.startAfter(it)
        }

        query.get().addOnSuccessListener { snapshot ->
            val posts = snapshot.toObjects(Post::class.java)

            if (snapshot.documents.isNotEmpty()) {
                lastVisible = snapshot.documents.last()
            }

            postAdapter.addPosts(posts)
            isLoading = false
        }.addOnFailureListener {
            isLoading = false
        }
    }
}
