package com.ladrope.johnwhite

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import com.ladrope.johnwhite.Model.Year
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.year_month_row.view.*

class HomeActivity : AppCompatActivity() {

    var newsRecyclerView: RecyclerView? = null
    var adapter: NewsAdapter? = null
    var layoutManager: RecyclerView.LayoutManager? = null
    var options: FirebaseRecyclerOptions<Year>? = null
    var mProgressBar: ProgressBar? =null
    var mErrorText: TextView? = null
    var mEmptyText: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        newsRecyclerView = yearList

//        val simpleTouchhelper = ItemTouchHelper(simpleItemTouchCallback)
//        simpleTouchhelper.attachToRecyclerView(newsRecyclerView)


//        mProgressBar = view.newsProgress
//        mErrorText = view.newsErrorText
//        mEmptyText = view.newsEmptyText


        val query = FirebaseDatabase.getInstance().reference.child("users").child(FirebaseAuth.getInstance().uid!!)
        setup(query)

        //FirebaseDatabase.getInstance().reference.child("users").setValue(null)

    }

    fun setup(query: Query){
        Log.e("setup", "started")
        mErrorText?.visibility = View.GONE
        mEmptyText?.visibility = View.GONE
        options = FirebaseRecyclerOptions.Builder<Year>()
                .setQuery(query, Year::class.java)
                .build()
        adapter = NewsAdapter(options!!, this)
        layoutManager = LinearLayoutManager(this)

        //set up recycler view
        newsRecyclerView!!.layoutManager = layoutManager
        newsRecyclerView!!.adapter = adapter

    }

    fun addEntry(view: View){
        val intent = Intent(this, AddEntry::class.java)
        startActivity(intent)
    }

    override fun onStart() {
        super.onStart()
        adapter?.startListening()
    }

    override fun onStop() {
        super.onStop()
        adapter?.stopListening()
    }


    inner class NewsAdapter(options: FirebaseRecyclerOptions<Year>, private val context: Context): FirebaseRecyclerAdapter<Year, NewsAdapter.ViewHolder>(options) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(context).inflate(R.layout.year_month_row, parent, false)
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

        override fun onBindViewHolder(holder: ViewHolder, position: Int, model: Year) {
            holder.bindItem(model)
        }

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            fun bindItem(tip: Year) {

                val heading = itemView.header
                val income = itemView.income
                val expenses = itemView.expenses
                val profit = itemView.profit
                val card = itemView.card
                val pl = itemView.pl

                val profitValue = getProfit(tip.income, tip.expenses)
                if (profitValue > 0){
                    card.setCardBackgroundColor(resources.getColor(R.color.greenCard))
                    pl.text = "Profit:"
                    pl.setTextColor(resources.getColor(R.color.greenCard))
                    profit.setTextColor(resources.getColor(R.color.greenCard))
                }else{
                    card.setCardBackgroundColor(resources.getColor(R.color.redCard))
                    pl.text = "Loss:"
                    pl.setTextColor(resources.getColor(R.color.redCard))
                    profit.setTextColor(resources.getColor(R.color.redCard))
                }

                heading.text = "Year "+tip.id
                income.text = "NGN "+tip.income
                expenses.text = "NGN "+tip.expenses
                profit.text = "NGN "+profitValue

                itemView.setOnClickListener {
                    val intent = Intent(this@HomeActivity, MonthsList::class.java)
                    intent.putExtra("year", tip.id)
                    startActivity(intent)
                }
            }
        }
    }


}
