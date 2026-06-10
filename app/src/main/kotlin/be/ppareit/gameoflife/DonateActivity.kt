package be.ppareit.gameoflife

import android.app.Activity
import android.app.AlertDialog
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast

class DonateActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val donateOptions = ArrayAdapter(
            this,
            android.R.layout.select_dialog_singlechoice,
            resources.getStringArray(R.array.donate_entries),
        )

        AlertDialog.Builder(this)
            .setIcon(R.drawable.ic_action_favorite)
            .setTitle(R.string.donate)
            .setAdapter(donateOptions) { _, _ ->
                Toast.makeText(this, R.string.donated_thank_user, Toast.LENGTH_SHORT).show()
                finish()
            }
            .setNegativeButton(android.R.string.cancel) { dialog, _ ->
                dialog.dismiss()
                finish()
            }
            .setOnCancelListener { finish() }
            .show()
    }
}
