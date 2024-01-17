package com.sahin.kotlinmaps2.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.sahin.kotlinmaps2.databinding.RecyclerRowBinding
import com.sahin.kotlinmaps2.model.Place
import com.sahin.kotlinmaps2.view.MapsActivity

class PlaceAdapter(val placeList : List<Place>): RecyclerView.Adapter<PlaceAdapter.PlaceHolder>() {
    class PlaceHolder(val binding: RecyclerRowBinding):RecyclerView.ViewHolder(binding.root){}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaceHolder {
        val binding = RecyclerRowBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return PlaceHolder(binding)
    }

    override fun getItemCount(): Int {
        return  placeList.size
    }

    override fun onBindViewHolder(holder: PlaceHolder, position: Int) {
        holder.binding.reyclerViewTextView.text = placeList.get(position).name

        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context,MapsActivity::class.java)
            intent.putExtra("selectedPlace",placeList.get(position))
            intent.putExtra("info","old")
            holder.itemView.context.startActivity(intent)

        }

    }
}