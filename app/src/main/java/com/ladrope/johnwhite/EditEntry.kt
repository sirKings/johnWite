package com.ladrope.johnwhite

import android.app.DatePickerDialog
import android.content.DialogInterface
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.ladrope.johnwhite.Model.Entry
import com.ladrope.johnwhite.Model.Month
import com.ladrope.johnwhite.Model.Year
import kotlinx.android.synthetic.main.activity_add_entry.*
import java.util.*

class EditEntry : AppCompatActivity() {

    var datePick: Button? = null
    var typePick: Button? = null
    var transAmount: EditText? = null
    var desc: EditText? = null
    var date: Long? = null
    var type: Boolean? = null
    var progress: ProgressBar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_entry)

        progress = progressBar2
        progress?.visibility = View.GONE

        datePick = datePicker
        typePick = typePicker
        transAmount = amount
        desc = Description

        datePick?.setOnClickListener {
            chooseDate()
        }

        typePick?.setOnClickListener {
            chooseType()
        }

        setUp()
    }

    fun setUp(){
        datePick?.text = getDate(selectedEntry?.date!!)
        date = selectedEntry?.date
        if (selectedEntry?.type!!){
            typePick?.text = "Income"
            type = true
        }else{
            typePick?.text = "Expenditure"
            type = false
        }
        transAmount?.setText(selectedEntry?.amount.toString())
        desc?.setText(selectedEntry?.desc)
    }

    fun chooseDate(){
        // Get Current Date
        val c = Calendar.getInstance()
        val mYear = c.get(Calendar.YEAR)
        val mMonth = c.get(Calendar.MONTH)
        val mDay = c.get(Calendar.DAY_OF_MONTH)
        val datePickerDialog = DatePickerDialog(this,
                object: DatePickerDialog.OnDateSetListener {
                    override fun onDateSet(view: DatePicker, year:Int,
                                           monthOfYear:Int, dayOfMonth:Int) {
                        datePick?.text = dayOfMonth.toString() + "/" + (monthOfYear + 1).toString() + "/" + year.toString()
                        val calendar = GregorianCalendar(year, monthOfYear+1, dayOfMonth)
                        date = calendar.timeInMillis
                    }
                }, mYear, mMonth, mDay)
        datePickerDialog.show()
    }

    fun chooseType(){
        val alert = AlertDialog.Builder(this)
        alert.setTitle("What type of transaction is this")
        alert.setPositiveButton(
                "Income",
                DialogInterface.OnClickListener { dialog, id ->
                    type = true
                    typePick?.text = "Income"
                    dialog.cancel()
                })

        alert.setNegativeButton(
                "Expenditure",
                DialogInterface.OnClickListener { dialog, id ->
                    type = false
                    typePick?.text = "Expenditure"
                    dialog.cancel()
                })
        alert.show()
    }

    fun saveTransaction(view: View){
        if (date == null){
            Toast.makeText(this, "Please select a date", Toast.LENGTH_SHORT).show()
        }else if (type == null){
            Toast.makeText(this, "Please select transaction type", Toast.LENGTH_SHORT).show()
        }else if (transAmount?.text.isNullOrEmpty()){
            Toast.makeText(this, "Please enter an amount", Toast.LENGTH_SHORT).show()
        }else if (desc?.text.isNullOrEmpty()){
            Toast.makeText(this, "Please enter a description", Toast.LENGTH_SHORT).show()
        }else{
            val entry = Entry()
            entry.desc = desc?.text.toString()
            entry.amount = transAmount?.text.toString().toInt()
            entry.date = date
            entry.type = type
            progress?.visibility = View.VISIBLE
            updateYearFile(entry)
        }

    }

    fun transactionFailed(){
        progress?.visibility = View.GONE
        Toast.makeText(this, "Could not save transaction", Toast.LENGTH_LONG).show()
    }

    fun updateYearFile(entry: Entry){
        FirebaseDatabase.getInstance().reference
                .child("users")
                .child(FirebaseAuth.getInstance().uid!!)
                .child(getYear(entry.date))
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onCancelled(p0: DatabaseError) {
                        transactionFailed()
                    }

                    override fun onDataChange(p0: DataSnapshot) {
                        if (p0.exists()){
                            val year = p0.getValue(Year::class.java)
                            if (entry.type!!){
                                year!!.income = year.income!!.toInt() + entry.amount!!
                            }else{
                                year!!.expenses = year.expenses!!.toInt() + entry.amount!!
                            }

                            saveYear(year, entry)
                        }else{
                            val year = Year()
                            year.id = getYear(entry.date)
                            if (entry.type!!){
                                year.income = entry.amount
                                year.expenses = 0
                            }else{
                                year.expenses = entry.amount
                                year.income = 0
                            }
                            saveYear(year, entry)
                        }
                    }
                })
    }

    fun saveYear(year: Year, entry: Entry){

        val updates = HashMap<String, Any>()
        updates.put("id", year.id!!)
        updates.put("income", year.income!!)
        updates.put("expenses", year.expenses!!)

        FirebaseDatabase.getInstance().reference
                .child("users")
                .child(FirebaseAuth.getInstance().uid!!)
                .child(year.id!!)
                .updateChildren(updates)
                .addOnCompleteListener {

                }
                .addOnFailureListener {
                    transactionFailed()
                }
        updateMonth(entry)
    }

    fun updateMonth(entry: Entry){
        FirebaseDatabase.getInstance().reference
                .child("users")
                .child(FirebaseAuth.getInstance().uid!!)
                .child(getYear(entry.date))
                .child(EntryName)
                .child(getMonth(entry.date))
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onCancelled(p0: DatabaseError) {
                        transactionFailed()
                    }

                    override fun onDataChange(p0: DataSnapshot) {
                        if (p0.exists()){
                            val month = p0.getValue(Month::class.java)
                            if (entry.type!!){
                                month!!.income = month.income!!.toInt() + entry.amount!!
                            }else{
                                month!!.expenses = month.expenses!!.toInt() + entry.amount!!
                            }

                            saveMonth(month, entry)
                        }else{
                            val month = Month()
                            month.id = getMonth(entry.date)
                            if (entry.type!!){
                                month.income = entry.amount
                                month.expenses = 0
                            }else{
                                month.expenses = entry.amount
                                month.income = 0
                            }
                            saveMonth(month, entry)
                        }
                    }
                })
    }

    fun saveMonth(year: Month, entry: Entry){
        val updates = HashMap<String, Any>()
        updates.put("id", year.id!!)
        updates.put("income", year.income!!)
        updates.put("expenses", year.expenses!!)

        FirebaseDatabase.getInstance().reference
                .child("users")
                .child(FirebaseAuth.getInstance().uid!!)
                .child(getYear(entry.date))
                .child(EntryName)
                .child(year.id!!)
                .updateChildren(updates)
                .addOnCompleteListener {

                }
                .addOnFailureListener {
                    transactionFailed()
                }
        saveEntry(entry)
    }

    fun saveEntry(entry: Entry){
        val ref =  FirebaseDatabase.getInstance().reference
                .child("users")
                .child(FirebaseAuth.getInstance().uid!!)
                .child(getYear(entry.date))
                .child(EntryName)
                .child(getMonth(entry.date))
                .child(EntryName)
        val key = ref.push().key
        entry.id = key
        ref.child(key!!).setValue(entry)
                .addOnFailureListener { transactionFailed() }
                .addOnCompleteListener {  }
        transactionSuccessfull()
    }

    fun transactionSuccessfull(){
        progress?.visibility = View.GONE
        Toast.makeText(this,"Entry Saved", Toast.LENGTH_SHORT).show()
        selectedEntry = null
        finish()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        selectedEntry = null
    }
}
