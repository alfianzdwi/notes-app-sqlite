package com.dicoding.consumerapps

import android.content.Intent
import android.database.ContentObserver
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.os.PersistableBundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.dicoding.consumerapps.adapter.NotesAdapter
import com.dicoding.consumerapps.databinding.ActivityMainBinding
import com.dicoding.consumerapps.db.DatabaseContract.NoteColumns.Companion.CONTENT_URI
import com.dicoding.consumerapps.entity.Note
import com.dicoding.consumerapps.helper.MappingHelper
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {
    private lateinit var adapter: NotesAdapter

    private lateinit var binding: ActivityMainBinding

    companion object {
        private const val EXTRA_STATE = "EXTRA_STATE"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title = "Consumer Notes"
        binding.rvNotes.layoutManager = LinearLayoutManager(this)
        binding.rvNotes.setHasFixedSize(true)
        adapter = NotesAdapter(this)
        binding.rvNotes.adapter = adapter

        //Pada Saat Button + "Tambah" Di Klik Akan Beralih Ke NoteAddUpdateActivity
        binding.fabAdd.setOnClickListener {
            val intent = Intent( this@MainActivity, NoteAddUpdateActivity::class.java)
            startActivity(intent)
        }

        // Untuk menangkap pesan dari notfyChaged yang ada di class noteProvider jika ada perubahan data
        // Lalu membuat sebuah fungsi yang menjadi turunan ContentObserver supaya bisa melakukan fungsi observe
        // Setelah observer dibuat, kita daftarkan dengan menggunakan registerContentObserver. Maka ketika terjadi perubahan data, kelas onChange akan terpanggil dan melakukan aksi tertentu, misal di sini yaitu memanggil data lagi supaya data yang ditampilkan di list adalah data terbaru.
        val handlerThread = HandlerThread("DataObserver")
        handlerThread.start()
        val handler = Handler(handlerThread.looper)

        val myObserver = object : ContentObserver(handler) {
            override fun onChange(self: Boolean) {
                loadNotesAsync()
            }
        }

        contentResolver.registerContentObserver(CONTENT_URI, true, myObserver)

        //Untuk Menjaga State Saat Terjadi Perubahan Konfigurasi
        if(savedInstanceState == null){
            // proses ambil data
            loadNotesAsync()
        } else {
            savedInstanceState.getParcelableArrayList<Note>(EXTRA_STATE)
        }
    }


    //Fungsi ini digunakan untuk load data dari tabel dan dan kemudian menampilkannya ke dalam list secara asynchronous dengan menggunakan Background process
    private fun loadNotesAsync(){
        GlobalScope.launch(Dispatchers.Main) {
            binding.progressbar.visibility = View.VISIBLE
            //val noteHelper = NoteHelper.getInstance(applicationContext)

            //Menggunakan fungsi async karena kita menginginkan nilai kembalian dari fungsi yang kita panggil
            val deferredNotes = async(Dispatchers.IO) {
                // CONTENT_URI = content://com.dicoding.picodiploma.mynotesapp/noteCursor. Di dalam background process terjadi penggunaan ContentResolver dengan pemanggilan getContentResolver.query(). parameter CONTENT_URI berarti string content://com.dicoding.consumerapps/note. Content resolver akan meneruskan obyek Uri tersebut ke content provider dan tentunya akan masuk ke dalam metode query.
                val cursor = contentResolver.query(CONTENT_URI, null, null, null, null)
                MappingHelper.mapCursorToArrayList(cursor) //Untuk convert data dari cursor menjadi ArrayList.Mengubah menjadi ArrayList supaya bisa ditampilkan di dalam adapter
            }
            binding.progressbar.visibility = View.INVISIBLE
            val notes = deferredNotes.await() //Untuk mendapatkan nilai kembaliannya, kita menggunakan fungsi await().
            if (notes.size > 0) {
                adapter.listNotes = notes
            } else {
                adapter.listNotes = ArrayList()
                showSnackbarMessage("Tidak ada data saat ini")
            }

        }
    }

    //Untuk Menyimpan State/Value Saat Terjadi Perubahan Konfigurasi
    override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle) {
        super.onSaveInstanceState(outState, outPersistentState)
        outState.putParcelableArrayList(EXTRA_STATE, adapter.listNotes)
    }


    //Fungsi Untuk Menampilkan Snackbar
    private fun showSnackbarMessage(message: String) {
        Snackbar.make(binding.rvNotes, message, Snackbar.LENGTH_SHORT).show()
    }

}