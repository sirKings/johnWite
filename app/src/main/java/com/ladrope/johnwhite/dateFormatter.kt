package com.ladrope.johnwhite

import java.text.DateFormatSymbols
import java.util.*

fun getDate(str: kotlin.Long): String{

    val cal = Calendar.getInstance()
    cal.timeInMillis = str

    val mYear = cal.get(Calendar.YEAR)
    val mMnth = cal.get(Calendar.MONTH)
    val mDay = cal.get(Calendar.DAY_OF_MONTH)

    val mHour = cal.get(Calendar.HOUR_OF_DAY)
    val mMnt = cal.get(Calendar.MINUTE)
    val mTime = cal.get(Calendar.AM_PM)

    return mDay.toString() + " " + getMyMonth(mMnth) + " " + mYear.toString() +" "+mHour.toString() +":"+mMnt+" "+ getPm(mTime)

}

fun getMyMonth(num: Int): String{
    var month = ""
    val dfs = DateFormatSymbols()
    val months = dfs.shortMonths
    if (num >= 0 && num <= 11){
        month = months[num]
    }
    return month
}

fun getFullMonth(num: Int): String{
    var month = ""
    val dfs = DateFormatSymbols()
    val months = dfs.months
    if (num >= 0 && num <= 11){
        month = months[num]
    }
    return month
}

fun getPm(num: Int): String{
    var month = ""
    val dfs = DateFormatSymbols()
    val months = dfs.amPmStrings
    if (num >= 0 && num <= 1){
        month = months[num]
    }
    return month
}

fun getDayMonth(str: kotlin.Long?): String{
    val cal = Calendar.getInstance()
    if (str != null) {
        cal.timeInMillis = str

        val mMnth = cal.get(Calendar.MONTH)
        val mDay = cal.get(Calendar.DAY_OF_MONTH)

        return mDay.toString() + " " + getMyMonth(mMnth)
    }else{
        return ""
    }
}

fun getYear(str: kotlin.Long?): String{
    val cal = Calendar.getInstance()
    if (str != null) {
        cal.timeInMillis = str

        val mYear = cal.get(Calendar.YEAR)

        return mYear.toString()
    }else{
        return ""
    }
}

fun getMonth(str: kotlin.Long?): String{
    val cal = Calendar.getInstance()
    if (str != null) {
        cal.timeInMillis = str

        val mMonth = cal.get(Calendar.MONTH)

        return getFullMonth(mMonth)
    }else{
        return ""
    }
}

fun getProfit(income: Int?, expenses: Int?): Int{
    if (income == null && expenses == null){
        return 0
    }else if (income != null && expenses == null){
        return income
    }else if (income == null && expenses != null){
        return 0 - expenses
    }else{
        return income!! - expenses!!
    }
}