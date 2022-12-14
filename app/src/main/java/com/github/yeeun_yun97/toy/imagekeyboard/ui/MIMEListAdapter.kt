package com.github.yeeun_yun97.toy.imagekeyboard.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.github.yeeun_yun97.toy.imagekeyboard.R
import com.github.yeeun_yun97.toy.imagekeyboard.data.retrofit.ImageItem

class MIMEListAdapter(private val listener: View.OnClickListener) :
    RecyclerView.Adapter<MIMEViewHolder>() {
    private var list: List<ImageItem> = listOf()

    fun setList(list: List<ImageItem>) {
        this.list = list
        this.notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MIMEViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_image, parent, false)
        return MIMEViewHolder(view)
    }

    override fun onBindViewHolder(holder: MIMEViewHolder, position: Int) {
        holder.setItem(list[position], listener)
    }

    override fun getItemCount() = list.size
}

class MIMEViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    fun setItem(item: ImageItem, listener: View.OnClickListener) {
        val imageView: ImageView = itemView as ImageView
        imageView.setTag(item.thumbnail)
        imageView.setOnClickListener(listener)
        var url = item.thumbnail
        if (url.startsWith("https://search.pstatic.net/common/?src="))
            url = url.slice(item.thumbnail.indexOfFirst { it == '=' }+1..url.length-1)

        Glide
            .with(itemView.context)
            .load(url)
            .centerCrop()
            .into(imageView)
    }
}