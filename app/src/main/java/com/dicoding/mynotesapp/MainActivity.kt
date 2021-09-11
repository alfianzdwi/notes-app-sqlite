package com.dicoding.mynotesapp

import android.content.Intent
import android.database.ContentObserver
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.os.PersistableBundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.dicoding.mynotesapp.adapter.NotesAdapter
import com.dicoding.mynotesapp.databinding.ActivityMainBinding
import com.dicoding.mynotesapp.db.DatabaseContract.NoteColumns.Companion.CONTENT_URI
import com.dicoding.mynotesapp.db.NoteHelper
import com.dicoding.mynotesapp.entity.Note
import com.dicoding.mynotesapp.helper.MappingHelper
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

        supportActionBar?.title = "Notes"
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
    private fun loadNotesAsync() {
        GlobalScope.launch(Dispatchers.Main) {
            binding.progressbar.visibility = View.VISIBLE
            val noteHelper = NoteHelper.getInstance(applicationContext)
            noteHelper.open()
            //Menggunakan fungsi async karena kita menginginkan nilai kembalian dari fungsi yang kita panggil
            val deferredNotes = async(Dispatchers.IO) {
                // CONTENT_URI = content://com.dicoding.picodiploma.mynotesapp/noteCursor. Di dalam background process terjadi penggunaan ContentResolver dengan pemanggilan getContentResolver.query(). parameter CONTENT_URI berarti string content://com.dicoding.mynotesapp/note. Content resolver akan meneruskan obyek Uri tersebut ke content provider dan tentunya akan masuk ke dalam metode query.
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
            noteHelper.close()
        }
    }

    //Untuk Menyimpan State/Value Saat Terjadi Perubahan Konfigurasi
    override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle) {
        super.onSaveInstanceState(outState, outPersistentState)
        outState.putParcelableArrayList(EXTRA_STATE, adapter.listNotes)
    }

    //Untuk Mendapatkan Nilai Balik Dari Activity Yang Di Jalankan Oleh MainActivity Dalam Hal Ini Adalah NoteAddUpdateActivity
   /* override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(data != null ){
            when(requestCode){
                //Kode Ini Dijalankan Pada Saat Menambahkan Catatan Baru
                NoteAddUpdateActivity.REQUEST_ADD -> if (resultCode == NoteAddUpdateActivity.RESULT_ADD) {
                    val note = data.getParcelableExtra<Note>(NoteAddUpdateActivity.EXTRA_NOTE) as Note

                    adapter.addItem(note) //Memanggil metode addItem yang berada di adapter dengan memasukan objek note sebagai argumen. Metode tersebut akan menjalankan notifyItemInserted dan penambahan arraylist-nya.
                    binding.rvNotes.smoothScrollToPosition(adapter.itemCount -1)

                    showSnackbarMessage("Satu item berhasil ditambahkan")
                }

                //Kode Ini Dijalankan Pada Saat Mengubah Catatan
                NoteAddUpdateActivity.REQUEST_UPDATE ->
                    when(resultCode){

                        //Kode Ini Dijalankan Pada Saat Button Update Di Tekan
                        NoteAddUpdateActivity.RESULT_UPDATE -> {
                            val note = data.getParcelableExtra<Note>(NoteAddUpdateActivity.EXTRA_NOTE) as Note
                            val position = data.getIntExtra(NoteAddUpdateActivity.EXTRA_POSITION, 0)
                            adapter.updateItem(position, note)
                            binding.rvNotes.smoothScrollToPosition(position)
                            showSnackbarMessage("Satu item berhasil diubah")
                        }

                        //Button Ini Dijalankan Pada Saat Menu Action Bar Delete Di Tekan
                        NoteAddUpdateActivity.RESULT_DELETE -> {
                            val position = data.getIntExtra(NoteAddUpdateActivity.EXTRA_POSITION, 0)
                            adapter.removeItem(position)
                            showSnackbarMessage("Satu item berhasil dihapus")
                        }
                    }
            }
        }
    }*/

    //Fungsi Untuk Menampilkan Snackbar
    private fun showSnackbarMessage(message: String) {
        Snackbar.make(binding.rvNotes, message, Snackbar.LENGTH_SHORT).show()
    }

}