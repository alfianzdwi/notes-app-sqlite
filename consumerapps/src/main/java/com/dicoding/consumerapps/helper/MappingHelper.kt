package com.dicoding.consumerapps.helper

import android.database.Cursor
import com.dicoding.consumerapps.db.DatabaseContract
import com.dicoding.consumerapps.entity.Note

//Kelas Untuk mengonversi dari Cursor ke Arraylist,karena nanti di adapter kita akan menggunakan arraylist, sedangkan di sini objek yang di kembalikan berupa Cursor
object MappingHelper {
    fun mapCursorToArrayList(notesCursor: Cursor?): ArrayList<Note>{
        val noteList = ArrayList<Note>()

        //Fungsi apply digunakan untuk menyederhanakan kode yang berulang. Misalnya notesCursor.geInt cukup ditulis getInt dan notesCursor.getColumnIndexOrThrow cukup ditulis getColumnIndexOrThrow
        notesCursor?.apply {
            //MoveToNext digunakan untuk memindahkan cursor ke baris selanjutnya. Di sini kita ambil datanya satu per satu dan dimasukkan ke dalam ArrayList.
            while (moveToNext()){
                val id = getInt(getColumnIndexOrThrow(DatabaseContract.NoteColumns._ID))
                val title = getString(getColumnIndexOrThrow(DatabaseContract.NoteColumns.TITLE))
                val description = getString(getColumnIndexOrThrow(DatabaseContract.NoteColumns.DESCRIPTION))
                val date = getString(getColumnIndexOrThrow(DatabaseContract.NoteColumns.DATE))
                noteList.add(Note(id, title, description, date))
            }
        }

        return noteList
    }

    fun mapCursorToObject(notesCursor: Cursor?): Note {
        var note = Note()
        notesCursor?.apply {
            moveToFirst()
            val id = getInt(getColumnIndexOrThrow(DatabaseContract.NoteColumns._ID))
            val title = getString(getColumnIndexOrThrow(DatabaseContract.NoteColumns.TITLE))
            val description = getString(getColumnIndexOrThrow(DatabaseContract.NoteColumns.DESCRIPTION))
            val date = getString(getColumnIndexOrThrow(DatabaseContract.NoteColumns.DATE))
            note = Note(id, title, description, date)
        }
        return note
    }
}