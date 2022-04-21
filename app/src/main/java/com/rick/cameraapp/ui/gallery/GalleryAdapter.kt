package com.rick.cameraapp.ui.gallery

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.rick.cameraapp.MainActivity
import com.rick.cameraapp.Photo
import com.rick.cameraapp.R

class GalleryAdapter(private val activity: MainActivity, private val fragment: GalleryFragment):
RecyclerView.Adapter<RecyclerView.ViewHolder>(){

    var photos = listOf<Photo>()

    inner class GalleryViewHolder(itemView: View): RecyclerView.ViewHolder(itemView), View.OnClickListener {
        internal val image = itemView.findViewById<ImageView>(R.id.image)

        init {
            itemView.isClickable = true
            itemView.setOnClickListener(this)
            itemView.setOnLongClickListener {
                fragment.showPopup(it, photos[layoutPosition])
                return@setOnLongClickListener true
            }
        }

        override fun onClick(view: View) {
            // TODO: Navigate to the photo filter fragment
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return GalleryViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.image_preview, parent, false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val current = photos[position]
        with(holder as GalleryViewHolder){
            Glide.with(activity)
                .load(current.uri)
                .centerCrop()
                .into(this.image)
        }
    }

    override fun getItemCount(): Int = photos.size
}