package com.dicoding.consumerapps

import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.dicoding.consumerapps.databinding.ActivityNoteAddUpdateBinding
import com.dicoding.consumerapps.db.DatabaseContract
import com.dicoding.consumerapps.db.DatabaseContract.NoteColumns.Companion.CONTENT_URI
import com.dicoding.consumerapps.db.DatabaseContract.NoteColumns.Companion.DATE
import com.dicoding.consumerapps.entity.Note
import com.dicoding.consumerapps.helper.MappingHelper
import java.text.SimpleDateFormat
import java.util.*

class NoteAddUpdateActivity : AppCompatActivity(), View.OnClickListener {

    private var isEdit = false
    private var note: Note? = null
    private var position: Int = 0
    private lateinit var uriWithId: Uri

    private lateinit var binding: ActivityNoteAddUpdateBinding

    companion object {
        const val EXTRA_NOTE = "extra_note"
        const val EXTRA_POSITION = "extra_position"
        const val ALERT_DIALOG_CLOSE = 10
        const val ALERT_DIALOG_DELETE = 20
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNoteAddUpdateBinding.inflate(layoutInflater)
        setContentView(binding.root)


        note = intent.getParcelableExtra(EXTRA_NOTE)
        if (note != null) {
            position = intent.getIntExtra(EXTRA_POSITION, 0)
            isEdit = true
        } else {
            note = Note()
        }

        val actionBarTitle: String
        val btnTitle: String

        //Kode Ini Dijalankan Jika IsEdit Bernilai True Berarti Jika Note Dalam Keadaan Ada Datanya/Tidak Null "Ini Pada Saat Kita Mengubah Catatan Yang Sudah Ada"
        if (isEdit) {
            uriWithId = Uri.parse(CONTENT_URI.toString() + "/" + note?.id) //Untuk membedakan di bagian URI,Yang Nanti digunakakan untuk mengaskes note berdasarkan id

            //Melakukan query berdasarkan id
            val cursor = contentResolver.query (uriWithId, null, null, null, null)
            if (cursor!= null ){
                note = MappingHelper.mapCursorToObject(cursor) //Untuk convert data dari cursor menjadi object. Kita perlu mengubahnya menjadi object supaya bisa ditampilkan di dalam teks.
                cursor.close()
            }

            actionBarTitle = "Ubah"
            btnTitle = "Update"

            note?.let {
                binding.edtTitle.setText(it.title)
                binding.edtDescription.setText(it.description)
            }
        //Kode Ini Akan Dijalankan Jika IsEdit Bernilai False Berart Jika Note Dalam Keadaan Tidak Ada Datanya/Null "Ini Pada Saat Kita Menambhkan Objek Catatan Baru"
        } else {
            actionBarTitle = " Tambah"
            btnTitle = "Simpan"
        }

        supportActionBar?.title = actionBarTitle
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.btnSubmit.text = btnTitle
        binding.btnSubmit.setOnClickListener(this)

    }

    override fun onClick(v: View?) {
        if (v?.id == R.id.btn_submit) {
            val title = binding.edtTitle.text.toString().trim()
            val desctiption = binding.edtDescription.text.toString().trim()

            if (title.isEmpty()) {
                binding.edtTitle.error = "Field Ini Tidak Boleh Kosong!!"
                return
            }

            note?.title = title
            note?.description = desctiption

            val intent = Intent()
            intent.putExtra(EXTRA_NOTE, note)
            intent.putExtra(EXTRA_POSITION, position)

            val values = ContentValues()
            values.put(DatabaseContract.NoteColumns.TITLE, title)
            values.put(DatabaseContract.NoteColumns.DESCRIPTION, desctiption)

            //Kode Ini Dijalankan Jika IsEdit Bernilai True
            if (isEdit) {

                contentResolver.update(uriWithId, values, null, null)
                Toast.makeText(this, "Satu Item Berhasil Diedit", Toast.LENGTH_SHORT).show()
                finish()

            //Kode Ini Dijalankan Jika IsEdit Bernilai False
            } else {
                values.put(DATE, getCurrentDate())
                contentResolver.insert(CONTENT_URI, values)
                Toast.makeText(this, "Satu item berhasil disimpan", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    //Fungsi Untuk Mendapatkan Waktu
    private fun getCurrentDate(): String{
        val dateFormat = SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault())
        val date = Date()

        return  dateFormat.format(date)
    }

    //Fungsi Untuk Menampilkan Item Menu Di ActionBar Dan Ini Hanya Akan Muncul Jika IsEdit Nilainya True
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        if (isEdit){
            menuInflater.inflate(R.menu.menu_form, menu)
        }
        return super.onCreateOptionsMenu(menu)
    }

    //Fungsi Ketika Item Yang Ada Di Menu Di Tekan/Di Pilih
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.action_delete -> showAlertDialog(ALERT_DIALOG_DELETE)
            android.R.id.home -> showAlertDialog(ALERT_DIALOG_CLOSE)
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        showAlertDialog(ALERT_DIALOG_CLOSE)
    }

    //Konfirmasi dialog sebelum proses batal atau hapus close = 10 delete = 20
    private fun showAlertDialog(type: Int) {
        val isDialogClose = type == ALERT_DIALOG_CLOSE
        val dialogTitle:String
        val dialogMessage: String

        if (isDialogClose) {
            dialogTitle = "Batal"
            dialogMessage = "Apakah anda ingin membatalkan perubahan pada form?"
        } else {
            dialogMessage = "Apakah anda yakin ingin menghapus item ini?"
            dialogTitle = "Hapus Note"
        }

        val alertDialogBuilder = AlertDialog.Builder(this)

        alertDialogBuilder.setTitle(dialogTitle)
        alertDialogBuilder
            .setMessage(dialogMessage)
            .setCancelable(false)
            .setPositiveButton("Ya") {_, _ ->
                if (isDialogClose){
                    finish()
                } else {
                    // Gunakan uriWithId untuk delete
                    // content://com.dicoding.picodiploma.mynotesapp/note/id
                    contentResolver.delete(uriWithId, null, null)
                    Toast.makeText(this, "Satu item berhasil dihapus", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
            .setNegativeButton("Tidak") {dialog, _ -> dialog.cancel()}

        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }
}