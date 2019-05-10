package com.example.saurabh.weatherapp

import android.annotation.SuppressLint
import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.view_json.view.*

class CustomAdapter(val data:ArrayList<jsonValues>): RecyclerView.Adapter<CustomAdapter.ViewHolder>() {
    @SuppressLint("InflateParams")
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val li=parent.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val holder=li.inflate(R.layout.view_json,null)
        return ViewHolder(holder)
    }

    override fun getItemCount(): Int =data.size

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.itemView.temp.text=data[position].temp+ 0x00B0.toChar()+"C"
        holder.itemView.city.text=data[position].city
    }

    class ViewHolder(itemView: View):RecyclerView.ViewHolder(itemView) {
    }
}