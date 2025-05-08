package br.edu.ifsp.dmo2.redesocial.model

import android.graphics.Bitmap

class Post(private val username: String, private val cidade: String, private val descricao: String, private val foto: Bitmap) {
    fun getUsername(): String {
        return username
    }

    fun getCidade(): String {
        return cidade
    }

    fun getDescricao(): String {
        return descricao
    }

    fun getFoto(): Bitmap {
        return foto
    }
}