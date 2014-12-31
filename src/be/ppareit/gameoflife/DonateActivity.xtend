package be.ppareit.gameoflife

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import com.anjlab.android.iab.v3.BillingProcessor
import com.anjlab.android.iab.v3.TransactionDetails
import org.xtendroid.annotations.AddLogTag

import static extension org.xtendroid.utils.AlertUtils.*

@AddLogTag
class DonateActivity extends Activity implements BillingProcessor.IBillingHandler {

    static final String LICENSE_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAo9T0ADKVdBRPknvs1jRv8z8LMHIbYeDwI8Ji0Eo8s7vi4ubkH1vJKwao1PXfwpyWE0H8dOXAzfsFGnMeFcmNm2Aj9YCfItL60GkBWkS1bLQeIcem+8mk7UbWMKbLeF7ZYzV3ruglLJqYdwgbFV0KFXOlaCho448LwZPlWjtTWQLZ6CyrHkmyt5oHtxDEcucosLagxrtqrA8oFyq7/2775jQUteu4PFL7pYQZuDZXgjQN4gdCoE3ekEdiw/CmbBTVwcURThOf2hyc5YglNMJONuTyuSXW7R+cqddof5hOQqas+1g0z9z5e2z8Qp7Bbm31EDVbvnT/LBXI7/Qw8QvxrQIDAQAB"

    BillingProcessor mBillingProcessor;

    override onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBillingProcessor = new BillingProcessor(this, LICENSE_KEY, this)

        val donate_options = new ArrayAdapter<String>(this,
            android.R.layout.select_dialog_singlechoice,
            resources.getStringArray(R.array.donate_entries))
        val donate_values = resources.getStringArray(R.array.donate_entries)

        new AlertDialog.Builder(this) //
        .setIcon(R.drawable.ic_action_favorite) //
        .setTitle(R.string.donate) //
        .setAdapter(donate_options,
            [ dialog, which |
                Log.d(TAG, "Selected : " + which)
                mBillingProcessor.purchase(this, donate_values.get(which))
            ]) //
        .setCancelable(true) //
        .setNegativeButton(android.R.string.cancel,
            [ dialog, which |
                dialog.dismiss()
                finish()
            ]) //
        .setOnCancelListener(
            [
                finish()
            ]) //
        .create() //
        .show()
    }

    override onDestroy() {
        Log.d(TAG, "Destroy called")
        mBillingProcessor?.release()
        super.onDestroy()
    }

    override onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "Activity result called")
        if(!mBillingProcessor.handleActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    override onBillingError(int errorCode, Throwable error) {
        Log.d(TAG, "Billing error, error code: " + errorCode)
        finish()
    }

    override onBillingInitialized() {
        Log.d(TAG, "Billing initialized")
    }

    override onProductPurchased(String productId, TransactionDetails details) {
        Log.d(TAG, "Product purchased, productId: " + productId)
        mBillingProcessor.consumePurchase(productId)
        toast(getString(R.string.donated_thank_user))
        finish()
    }

    override onPurchaseHistoryRestored() {
        Log.d(TAG, "Purchase history restored")
    }

}
