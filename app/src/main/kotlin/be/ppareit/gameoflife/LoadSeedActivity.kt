package be.ppareit.gameoflife

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter

class LoadSeedActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val seeds = assets.list("life106") ?: emptyArray()
        val seedsAdapter = ArrayAdapter(this, android.R.layout.select_dialog_item, seeds)

        AlertDialog.Builder(this)
            .setIcon(R.drawable.ic_menu_archive)
            .setTitle(R.string.select_seed)
            .setAdapter(seedsAdapter) { _, which ->
                setResult(RESULT_OK, Intent().putExtra("seed", "life106/${seeds[which]}"))
                finish()
            }
            .setNegativeButton(android.R.string.cancel) { dialog, _ ->
                dialog.dismiss()
                setResult(RESULT_CANCELED)
                finish()
            }
            .setOnCancelListener {
                setResult(RESULT_CANCELED)
                finish()
            }
            .show()
    }
}
