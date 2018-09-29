package com.ladrope.johnwhite

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.ladrope.johnwhite.Model.Entry
import com.ladrope.johnwhite.Model.Month
import com.ladrope.johnwhite.Model.Year
import kotlinx.android.synthetic.main.activity_entry_list.*
import kotlinx.android.synthetic.main.entry_row.view.*
import java.util.*

class EntryList : AppCompatActivity() {

    var newsRecyclerView: RecyclerView? = null
    var adapter: EntriesAdapter? = null
    var layoutManager: RecyclerView.LayoutManager? = null
    var options: FirebaseRecyclerOptions<Entry>? = null
    var mProgressBar: ProgressBar? =null
    var mErrorText: TextView? = null
    var mEmptyText: TextView? = null
    var currentMonth: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_entry_list)

        val month = intent.getStringExtra("month")
        val year = intent.getStringExtra("year")
        currentMonth = month

        newsRecyclerView = entrylist
        monthHeading.text = month.capitalize() +", " + year.capitalize()

        val simpleTouchhelper = ItemTouchHelper(simpleItemTouchCallback)
        simpleTouchhelper.attachToRecyclerView(newsRecyclerView)


//        mProgressBar = view.newsProgress
//        mErrorText = view.newsErrorText
//        mEmptyText = view.newsEmptyText


        val query = FirebaseDatabase.getInstance().reference.child("users").child(FirebaseAuth.getInstance().uid!!).child(year).child(EntryName).child(month).child(EntryName)
        setup(query)
    }


    fun setup(query: Query){
        Log.e("setup", "started")
        mErrorText?.visibility = View.GONE
        mEmptyText?.visibility = View.GONE
        options = FirebaseRecyclerOptions.Builder<Entry>()
                .setQuery(query, Entry::class.java)
                .build()
        adapter = EntriesAdapter(options!!, this)
        layoutManager = LinearLayoutManager(this)

        //set up recycler view
        newsRecyclerView!!.layoutManager = layoutManager
        newsRecyclerView!!.adapter = adapter

    }

    override fun onStart() {
        super.onStart()
        adapter?.startListening()
    }

    override fun onStop() {
        super.onStop()
        adapter?.stopListening()
    }


    inner class EntriesAdapter(options: FirebaseRecyclerOptions<Entry>, private val context: Context): FirebaseRecyclerAdapter<Entry, EntriesAdapter.ViewHolder>(options) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(context).inflate(R.layout.entry_row, parent, false)
            return ViewHolder(view)
        }


        override fun onDataChanged() {
            super.onDataChanged()
            mProgressBar?.visibility = View.GONE
            if (adapter?.itemCount == 0) {
                mEmptyText?.visibility = View.VISIBLE
            }
        }


        override fun onError(error: DatabaseError) {
            super.onError(error)
            mErrorText?.visibility = View.VISIBLE
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int, model: Entry) {
            holder.bindItem(model)
        }

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            fun bindItem(tip: Entry) {

                val card = itemView.card
                val dayMonth = itemView.month
                val year = itemView.year
                val desc = itemView.descriptions
                val amount = itemView.amount

                //val profitValue = getProfit(tip.income, tip.expenses)
                if (tip.type!!){
                    card.setBackgroundColor(resources.getColor(R.color.greenCard))
                }else{
                    card.setBackgroundColor(resources.getColor(R.color.redCard))
                }

                dayMonth.text = getDayMonth(tip.date)
                year.text = getYear(tip.date)
                desc.text = tip.desc
                amount.text = "NGN "+tip.amount

//                itemView.setOnClickListener {
//                    val intent = Intent(this@MonthsList, MonthsList::class.java)
//                    intent.putExtra("month", tip.id)
//                    startActivity(intent)
//                }
            }
        }
    }

    var simpleItemTouchCallback: ItemTouchHelper.SimpleCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT or ItemTouchHelper.DOWN or ItemTouchHelper.UP) {

        override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
            Toast.makeText(this@EntryList, "on Move", Toast.LENGTH_SHORT).show()
            return false
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, swipeDir: Int) {
            //Remove swiped item from list and notify the RecyclerView
            val position = viewHolder.adapterPosition
            //val id = adapter?.getRef(position)
            //arrayList.remove(position)
            callDelete(adapter?.getItem(position))
            adapter?.notifyDataSetChanged()
            //Log.e(position.toString(), id.toString())

        }
    }


    fun callDelete(entry: Entry?){
        val builder1 = AlertDialog.Builder(this)
        builder1.setTitle("Delete item")
        builder1.setCancelable(false)

        builder1.setPositiveButton(
                "Yes",
                DialogInterface.OnClickListener { dialog, id ->
                    deleteEntry(entry!!)

                })

        builder1.setNegativeButton(
                "No",
                DialogInterface.OnClickListener { dialog, id ->

                    adapter?.notifyDataSetChanged()
                })

        builder1.setNeutralButton("Edit", DialogInterface.OnClickListener{
            dialog, id ->
            selectedEntry = entry
            deleteEntry(entry!!)
            editItem(entry.id!!)
        })

        val alert = builder1.create()
        alert.show()
    }

    fun editItem(id: String){
        val intent = Intent(this, EditEntry::class.java)
        intent.putExtra("ref", id)
        startActivity(intent)
    }

    fun deleteEntry(entry: Entry){
        FirebaseDatabase.getInstance().reference
                .child("users")
                .child(FirebaseAuth.getInstance().uid!!)
                .child(getYear(entry.date))
                .child(EntryName)
                .child(getMonth(entry.date))
                .child(EntryName)
                .child(entry.id!!)
                .setValue(null)
                .addOnCompleteListener {
                    Toast.makeText(this, "Deleted", Toast.LENGTH_SHORT).show()
                }
        updateYearFile(entry)
    }

    fun updateYearFile(entry: Entry){
        FirebaseDatabase.getInstance().reference
                .child("users")
                .child(FirebaseAuth.getInstance().uid!!)
                .child(getYear(entry.date))
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onCancelled(p0: DatabaseError) {
                        //transactionFailed()
                    }

                    override fun onDataChange(p0: DataSnapshot) {
                        if (p0.exists()){
                            val year = p0.getValue(Year::class.java)

                            if (entry.type!!){
                                year!!.income = year.income!!.toInt() - entry.amount!!
                            }else{
                                year!!.expenses = year.expenses!!.toInt() - entry.amount!!
                            }

                            saveYear(year, entry)

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
                    //transactionFailed()
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
                        //transactionFailed()
                    }

                    override fun onDataChange(p0: DataSnapshot) {
                        if (p0.exists()){
                            val month = p0.getValue(Month::class.java)

                            if (entry.type!!){
                                month!!.income = month.income!!.toInt() - entry.amount!!
                            }else{
                                month!!.expenses = month.expenses!!.toInt() - entry.amount!!
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
                    //transactionFailed()
                }
        //saveEntry(entry)
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

    }
}
