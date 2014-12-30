package be.ppareit.android

import android.app.AlarmManager
import android.app.admin.DevicePolicyManager
import android.content.Context
import android.preference.Preference
import android.preference.PreferenceActivity
import android.os.PowerManager
import android.app.Dialog
import android.view.View
import android.content.SharedPreferences
import android.app.Activity
import android.view.LayoutInflater

class AndroidUtils {

    def static <T extends Preference> T findPref(PreferenceActivity it, CharSequence key) {
        return it.findPreference(key) as T;
    }

    def static <T extends View> T findView(Dialog dialog, int id) {
        return dialog.findViewById(id) as T;
    }

    def static <T extends View> T findView(Activity activity, int id) {
        return activity.findViewById(id) as T;
    }

    def static <T extends View> T findView(View view, int id) {
        return view.findViewById(id) as T;
    }

    static def <T> T getSystemService(Context context, Class<T> t) {
        var name = switch t {
            case AlarmManager : Context.ALARM_SERVICE
            case DevicePolicyManager : Context.DEVICE_POLICY_SERVICE
            case LayoutInflater : Context.LAYOUT_INFLATER_SERVICE
            case PowerManager : Context.POWER_SERVICE
        }
        return context.getSystemService(name) as T;
    }

    static def SharedPreferences getSharedPreferences(PreferenceActivity activity) {
        return activity.preferenceScreen.sharedPreferences
    }
}