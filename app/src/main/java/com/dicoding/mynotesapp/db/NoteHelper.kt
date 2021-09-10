package com.dicoding.mynotesapp.db


import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import com.dicoding.mynotesapp.db.DatabaseContract.NoteColumns.*
import com.dicoding.mynotesapp.db.DatabaseContract.NoteColumns.Companion.TABLE_NAME
import com.dicoding.mynotesapp.db.DatabaseContract.NoteColumns.Companion._ID

//Berkas Untuk DML
class NoteHelper(context: Context) {
    private var databaseHelper: DatabaseHelper = DatabaseHelper(context)
    private lateinit var database: SQLiteDatabase

    companion object {
        private const val DATABASE_TABLE = TABLE_NAME


        //Metode yang nantinya akan digunakan untuk menginisiasi database.Menggunakan sebuah pattern yang bernama Singleton Pattern. Dengan singleton sebuah objek hanya bisa memiliki sebuah instance. Sehingga tidak terjadi duplikasi instance. Synchronized di sini dipakai untuk menghindari duplikasi instance di semua Thread, karena bisa saja kita membuat instance di Thread yang berbeda.
        private var INSTANCE: NoteHelper? = null
        fun getInstance(context: Context): NoteHelper =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: NoteHelper(context)
            }
    }

    //Metode untuk membuka koneksi ke database
    @Throws(SQLException::class)
    fun open() {
        database = databaseHelper.writableDatabase
    }

    //Metode untuk menutup koneksi ke database
    fun close() {
        databaseHelper.close()

        if (database.isOpen) database.close()
    }

    //Metode Untuk Menampilkan Semua Data/Catatan Dari SQLite
    fun queryAll(): Cursor {
        return database.query(
            DATABASE_TABLE,
            null,
            null,
            null,
            null,
            null,
            "$_ID ASC")
    }

    //Metode Untuk Mendapatkan Catatan Berdasarkan Id Dari SQLite
    fun queryById(id: String): Cursor {
        return database.query(
            DATABASE_TABLE,
            null,
            "$_ID = ?",
            arrayOf(id),
            null,
            null,
            null,
            null
        )
    }

    //Metode Untuk Menambahkan Data Ke Database SQLite
    fun insert(values: ContentValues?): Long{
        return database.insert(DATABASE_TABLE, null, values)
    }

    //Metode Untuk Mengubah Data Dari Database SQLite
    fun update(id: String, values: ContentValues?): Int {
        return  database.update(DATABASE_TABLE, values, "$_ID = ?", arrayOf(id))
    }

    //Metode Untuk Menghapus Data Dari Database SQLite
    fun deleteById(id: String): Int{
        return database.delete(DATABASE_TABLE, "$_ID = $id", null)
    }
}