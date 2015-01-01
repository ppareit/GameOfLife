package be.ppareit.gameoflife

import android.app.Activity
import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import org.xtendroid.annotations.AddLogTag
import android.content.Intent

@AddLogTag
class LoadSeedActivity extends Activity {

    AlertDialog mDialog;

    override onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        val seeds = assets.list("life106")
        val seedsAdapter = new ArrayAdapter<String>(this,
            android.R.layout.select_dialog_item, seeds)

        mDialog = new AlertDialog.Builder(this) //
        .setIcon(R.drawable.ic_menu_archive) //
        .setTitle(R.string.select_seed) //
        .setAdapter(seedsAdapter,
            [ dialog, which |
                var intent = new Intent()
                intent.putExtra("seed", "life106/" + seeds.get(which));
                setResult(RESULT_OK, intent)
                finish()
            ]) //
        .setCancelable(true) //
        .setNegativeButton(android.R.string.cancel,
            [ dialog, which |
                dialog.dismiss()
                result = RESULT_CANCELED
                finish()
            ]) //
        .setOnCancelListener(
            [
                result = RESULT_CANCELED
                finish()
            ]) //
        .create()

        mDialog.show()

    }

}
