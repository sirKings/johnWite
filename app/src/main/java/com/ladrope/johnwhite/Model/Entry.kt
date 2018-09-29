package com.ladrope.johnwhite.Model

class Entry() {
    var amount: Int? = null
    var date: Long? = null
    var id: String? = null
    var desc: String? = null
    var type: Boolean? = null


    constructor(id: String, amount: Int?, date: Long?, desc: String?, type: Boolean?): this(){

        this.amount = amount
        this.date  = date
        this.id = id
        this.desc = desc
        this.type = type
    }
}