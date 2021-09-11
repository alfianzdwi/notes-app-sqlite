package com.dicoding.consumerapps.db

import android.net.Uri
import android.provider.BaseColumns

object DatabaseContract {
    const val AUTHORITY = "com.dicoding.mynotesapp" //Variabel AUTHORITY merupakan base authority yang akan kita gunakan untuk mengidentifikasi bahwa provider NoteProvider milik MyNotesApp yang akan diakses.Jika Anda perhatikan di dalam AndroidManifest dari MyNotesApp menggunakan authorities com.dicoding.consumerapps.
    const val SCHEME = "content"

    class NoteColumns: BaseColumns{
        companion object {
            const val TABLE_NAME = "note"
            const val _ID = "_id"
            const val TITLE = "title"
            const val DESCRIPTION = "description"
            const val DATE = "date"

            // Untuk membuat URI content://com.dicoding.consumerapps/note Atau akan digunakan untuk identifikasi provider mana yang akan di akses.
            // Variabel CONTENT_URI: Di sini kita menggabungkan base authority dengan scheme dan nama tabel, nanti string yang akan tercipta adalah "content://com.dicoding.consumerapps/note". Artinya dari string "content://com.dicoding.consumerapps/note" berarti kita akan mencoba untuk akses data tabel Note dari provider NoteProvider.
            val CONTENT_URI: Uri = Uri.Builder().scheme(SCHEME)
                .authority(AUTHORITY)
                .appendPath(TABLE_NAME)
                .build()
        }
    }
}