package com.dicoding.mynotesapp.provider

import android.content.ContentProvider
import android.content.ContentValues
import android.content.Context
import android.content.UriMatcher
import android.database.Cursor
import android.net.Uri
import com.dicoding.mynotesapp.db.DatabaseContract.AUTHORITY
import com.dicoding.mynotesapp.db.DatabaseContract.NoteColumns.Companion.CONTENT_URI
import com.dicoding.mynotesapp.db.DatabaseContract.NoteColumns.Companion.TABLE_NAME
import com.dicoding.mynotesapp.db.NoteHelper
import com.dicoding.mynotesapp.entity.Note

class NoteProvider : ContentProvider() {

    companion object{
        private const val NOTE = 1
        private const val NOTE_ID = 2
        private val sUriMatcher = UriMatcher(UriMatcher.NO_MATCH)
        private lateinit var noteHelper: NoteHelper


        //UriMatcher, fungsinya adalah untuk membandingkan uri dengan nilai integer tertentu. Sebelum dibandingkan, kita perlu atur nilai tiap Uri-nya terlebih dahulu. Perhatikan nilai dari NOTE dan NOTE_ID yaitu 1 dan 2.Ini berfungsi untuk mempermudah dalam proses membandingkan obyek Uri yang akan di gunakan
        init {
            // content://com.dicoding.mynotesapp/note
            sUriMatcher.addURI(AUTHORITY, TABLE_NAME, NOTE)

            // content://com.dicoding.mynotesapp/note/id
            sUriMatcher.addURI(AUTHORITY, "$TABLE_NAME/#", NOTE_ID) // Path dengan id : tanda # menandakan seuatu kata
        }
    }
    override fun onCreate(): Boolean {
        noteHelper = NoteHelper.getInstance(context as Context)
        noteHelper.open()
        return true
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
        val deleted: Int = when(NOTE_ID) {
            sUriMatcher.match(uri) -> noteHelper.deleteById(uri.lastPathSegment.toString())
            else -> 0
        }

        context?.contentResolver?.notifyChange(CONTENT_URI,  null) //Untuk memberitahu kalau ada perubahan data.Fungsi ini akan mengirim pesan kepada semua aplikasi yang mengakses data dari content provider ini.

        return deleted
    }

    override fun getType(uri: Uri): String? {
        return null
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        val added: Long  = when(NOTE) {
            sUriMatcher.match(uri) -> noteHelper.insert(values)
            else -> 0
        }

        context?.contentResolver?.notifyChange(CONTENT_URI, null)

        return Uri.parse("$CONTENT_URI/$added")

    }

    override fun query(uri: Uri, projection: Array<String>?, selection: String?, selectionArgs: Array<String>?, sortOrder: String?): Cursor? {
        return when (sUriMatcher.match(uri)){
            NOTE -> noteHelper.queryAll() //Ketika obyek Uri yang digunakan cocok dengan nilai pada variabel NOTE atau int 1, maka query yang akan dijalankan adalah select semua data yang ada di dalam database.
            NOTE_ID -> noteHelper.queryById(uri.lastPathSegment.toString()) // Ketika obyek Uri yang digunakan cocok dengan nilai pada variable NOTE_ID atau int 2, maka query yang akan dijalankan adalah select satu data berdasarkan id. Nah, kita bisa mengambil id-nya dari obyek Uri dengan menggunakan fungsi getLastPathSegment() yang artinya ambil segment terakhir dari obyek Uri.
            else -> null
        }
    }

    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<String>?): Int {
        val updated : Int = when (NOTE_ID) {
            sUriMatcher.match(uri) -> noteHelper.update(uri.lastPathSegment.toString(), values)
            else -> 0
        }

        context?.contentResolver?.notifyChange(CONTENT_URI, null)

        return updated
    }

}