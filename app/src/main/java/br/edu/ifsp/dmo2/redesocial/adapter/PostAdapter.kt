package br.edu.ifsp.dmo2.redesocial.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import br.edu.ifsp.dmo2.redesocial.R
import br.edu.ifsp.dmo2.redesocial.model.Post

class PostAdapter(private val posts: Array<Post>) :
    RecyclerView.Adapter<PostAdapter.ViewHolder>() {
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtUsername: TextView = view.findViewById(R.id.textview_username)
        val txtCidade: TextView = view.findViewById(R.id.textview_cidade)
        val imgPost: ImageView = view.findViewById(R.id.image_post_item)
        val txtDescricao: TextView = view.findViewById(R.id.textview_post_item)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.post_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return posts.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.txtUsername.text = posts[position].getUsername()
        holder.txtCidade.text = posts[position].getCidade()
        holder.imgPost.setImageBitmap(posts[position].getFoto())
        holder.txtDescricao.text = posts[position].getDescricao()
    }
}