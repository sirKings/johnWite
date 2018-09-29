package com.ladrope.johnwhite.Model

class Year() {
    var income: Int? = null
    var expenses: Int? = null
    var id: String? = null


    constructor(id: String, income: Int?, expenses: Int?): this(){

        this.income = income
        this.expenses = expenses
        this.id = id
    }
}
