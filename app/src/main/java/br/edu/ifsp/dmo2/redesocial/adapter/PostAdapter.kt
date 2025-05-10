package br.edu.ifsp.dmo2.redesocial.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import br.edu.ifsp.dmo2.redesocial.R
import br.edu.ifsp.dmo2.redesocial.model.Post

class PostAdapter : RecyclerView.Adapter<PostViewHolder>() {

    private val postList = mutableListOf<Post>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.post_item, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        holder.bind(postList[position])
    }

    override fun getItemCount(): Int = postList.size

    fun addPosts(newPosts: List<Post>) {
        val start = postList.size
        postList.addAll(newPosts)
        notifyItemRangeInserted(start, newPosts.size)
    }

    fun clearPosts() {
        postList.clear()
        notifyDataSetChanged()
    }
}
