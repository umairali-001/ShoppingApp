package com.sprizen.uashoppingcenter.Adapters

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.sprizen.uashoppingcenter.Activities.ExploreActivity
import com.sprizen.uashoppingcenter.DATA_CLASS.SEARCH


class ResecntSearhesAdapter(context: Context, searchesList: MutableList<SEARCH>): RecyclerView
    .Adapter<ResecntSearhesAdapter.ViewHolder>(){

    var context : Context
    var searchesList : MutableList<SEARCH>



    var onItemClickListener : OnSearchItemClickListener

        init {
            this.context=context
            this.searchesList = searchesList
            this.onItemClickListener = context as ExploreActivity

            }

    override fun onCreateViewHolder(
        p0: ViewGroup,
        p1: Int,
    ): ResecntSearhesAdapter.ViewHolder {

        return ViewHolder(LayoutInflater.from(context).inflate(com.sprizen.uashoppingcenter.R
            .layout.adapter_resent_searches,null))
    }

    override fun onBindViewHolder(binding: ResecntSearhesAdapter.ViewHolder, position: Int) {

        binding.searchText.text = searchesList[position].recentSearch.toString()

        binding.itemView.setOnClickListener {

                onItemClickListener.onItemClick(searchesList[position].recentSearch.toString())

        }


    }

    override fun getItemCount(): Int {
        return searchesList.size

    }


    interface OnSearchItemClickListener{
        fun onItemClick(itemText : String)

    }

    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {

        var searchText = itemView.findViewById<TextView>(com.sprizen.uashoppingcenter.R.id.search_text)
    }
}