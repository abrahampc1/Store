package com.example.store

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.store.databinding.ItemStoreBinding

class StoreAdapter(private var stores: MutableList<Store>, private var listener: OnClickListener) :
RecyclerView.Adapter<StoreAdapter.ViewHolder>(){

    private lateinit var mContext : Context

    inner class ViewHolder(view : View) : RecyclerView.ViewHolder(view){
        val binding = ItemStoreBinding.bind(view)

        fun setListener(store: Store){
            binding.root.setOnClickListener{listener.OnClick(store)}
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        mContext = parent.context

        val view = LayoutInflater.from(mContext).inflate(R.layout.item_store, parent, false)

        return ViewHolder(view)
    }

    override fun getItemCount(): Int = stores.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val store = stores.get(position)

        with(holder){
            setListener(store)
            binding.TvName.text = store.Name
        }
    }
}