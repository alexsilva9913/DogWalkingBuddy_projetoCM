package pt.ie.dogwalkingbuddy.adapter

import android.widget.ImageView
import com.bumptech.glide.Glide

fun ImageView.loadImg(url : String) {
    Glide.with(context)
        .load(url)
        .into(this)
}