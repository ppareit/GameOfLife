package be.ppareit.gameoflife

import android.R
import android.app.Activity
import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import org.xtendroid.annotations.AddLogTag
import android.text.TextWatcher
import android.text.Editable
import android.widget.Button
import java.io.File
import android.content.Intent
import android.net.Uri

@AddLogTag
class SaveToFileActivity extends Activity {

    AlertDialog mDialog;
    Button mSaveButton;

    override onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        val input = new EditText(this)
        input.addTextChangedListener(
            new TextWatcher() {
                override afterTextChanged(Editable s) {
                    mSaveButton.enabled = (s.length > 0)
                }

                override beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                override onTextChanged(CharSequence s, int start, int before, int count) {
                }
            })

        mDialog = new AlertDialog.Builder(this) //
        .setIcon(R.drawable.ic_menu_save) //
        .setTitle(be.ppareit.gameoflife.R.string.save_title) //
        .setView(input) //
        .setPositiveButton(be.ppareit.gameoflife.R.string.save,
            [ dialog, which |
                var dataDir = applicationInfo.dataDir
                var fileName = input.text.toString()
                if (!fileName.endsWith(".life")) fileName += ".life"
                var path = dataDir + "/" +fileName
                var file = new File(path)
                if (file.exists) {
                    Log.w(TAG, "File already exists")
                }
                var intent = new Intent()
                intent.data = Uri.fromFile(file);
                setResult(RESULT_OK, intent)
                finish()
            ]) //
        .setCancelable(true) //
        .setNegativeButton(R.string.cancel,
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

        mSaveButton = mDialog.getButton(AlertDialog.BUTTON_POSITIVE)
        mSaveButton.enabled = false

    }

}
