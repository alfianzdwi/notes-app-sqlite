package com.dicoding.mynotesapp

import android.view.View

//Kelas ini bertugas membuat item seperti CardView bisa diklik di dalam adapter. Caranya lakukan penyesuaian pada kelas event OnClickListener. Alhasil kita bisa mengimplementasikan interface listener yang baru bernama OnItemClickCallback. Kelas tersebut dibuat untuk menghindari nilai final dari posisi yang tentunya sangat tidak direkomendasikan.
class CustomOnItemClickListener(private val position: Int, private val onItemClickCallback: OnItemClickCallback): View.OnClickListener {

    override fun onClick(v: View) {
        onItemClickCallback.onItemClicked(v , position)
    }

    interface OnItemClickCallback {
        fun onItemClicked(view: View, position: Int)
    }
}