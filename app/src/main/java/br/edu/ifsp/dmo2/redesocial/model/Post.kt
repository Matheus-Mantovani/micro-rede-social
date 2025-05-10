package br.edu.ifsp.dmo2.redesocial.model

class Post() {

    var username: String? = null
    var cidade: String? = null
    var descricao: String? = null
    var imageString: String? = null

    constructor(username: String, cidade: String, descricao: String, foto: String) : this() {
        this.username = username
        this.cidade = cidade
        this.descricao = descricao
        this.imageString = foto
    }
}
