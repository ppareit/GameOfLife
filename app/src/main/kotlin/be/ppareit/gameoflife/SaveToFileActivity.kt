package be.ppareit.gameoflife

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Button
import android.widget.EditText
import java.io.File

class SaveToFileActivity : Activity() {
    private lateinit var saveButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val input = EditText(this)
        input.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
                saveButton.isEnabled = s.isNotEmpty()
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) = Unit
        })

        val dialog = AlertDialog.Builder(this)
            .setIcon(android.R.drawable.ic_menu_save)
            .setTitle(R.string.save_title)
            .setView(input)
            .setPositiveButton(R.string.save) { _, _ ->
                var fileName = input.text.toString()
                if (!fileName.endsWith(".life")) fileName += ".life"
                val file = File(applicationInfo.dataDir, fileName)
                if (file.exists()) {
                    Log.w(TAG, "File already exists")
                }
                setResult(RESULT_OK, Intent().setData(Uri.fromFile(file)))
                finish()
            }
            .setNegativeButton(android.R.string.cancel) { dialogInterface, _ ->
                dialogInterface.dismiss()
                setResult(RESULT_CANCELED)
                finish()
            }
            .setOnCancelListener {
                setResult(RESULT_CANCELED)
                finish()
            }
            .create()

        dialog.show()
        saveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
        saveButton.isEnabled = false
    }

    companion object {
        private val TAG = SaveToFileActivity::class.java.simpleName
    }
}
