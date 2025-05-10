package br.edu.ifsp.dmo2.redesocial.adapter

import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import br.edu.ifsp.dmo2.redesocial.R
import br.edu.ifsp.dmo2.redesocial.model.Post
import br.edu.ifsp.dmo2.redesocial.util.Base64Converter

class PostViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    private val txtUsername: TextView = view.findViewById(R.id.textview_username)
    private val txtCidade: TextView = view.findViewById(R.id.textview_cidade)
    private val imgPost: ImageView = view.findViewById(R.id.image_post_item)
    private val txtDescricao: TextView = view.findViewById(R.id.textview_post_item)

    fun bind(post: Post) {
        txtUsername.text = post.username ?: "Usuário"
        txtCidade.text = post.cidade ?: ""
        txtDescricao.text = post.descricao ?: "Sem descrição"

        post.imageString?.let { base64 ->
            val bitmap = Base64Converter.stringToBitmap(base64)
            if (bitmap != null) {
                imgPost.setImageBitmap(bitmap)
            } else {
                imgPost.setImageResource(R.drawable.image_256dp)
            }
        }
    }

}