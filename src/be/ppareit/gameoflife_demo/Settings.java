package be.ppareit.gameoflife_demo;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.preference.PreferenceManager;

public class Settings {
    private static final String OPTION_MINIMUM = "UNDERPOPULATION_VARIABLE";
    private static final String OPTION_MINIMUM_DEFAULT = "2";
    private static final String OPTION_MAXIMUM = "OVERPOPULATION_VARIABLE";
    private static final String OPTION_MAXIMUM_DEFAULT = "3";
    private static final String OPTION_SPAWN = "SPAWN_VARIABLE";
    private static final String OPTION_SPAWN_DEFAULT = "3";
    private static final String OPTION_ANIMATION_SPEED = "ANIMATION_SPEED";
    private static final String OPTION_ANIMATION_SPEED_DEFAULT = "10";
    private static final String OPTION_DISPLAY_THEME = "DISPLAY_THEME";
    private static final String OPTION_DISPLAY_THEME_DEFAULT = "0";

    public static void resetPopulationSettings() {
        Context context = GolApplication.getAppContext();
        SharedPreferences sprefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor pedit = sprefs.edit();
        pedit.putString(OPTION_MINIMUM, OPTION_MINIMUM_DEFAULT);
        pedit.putString(OPTION_MAXIMUM, OPTION_MAXIMUM_DEFAULT);
        pedit.putString(OPTION_SPAWN, OPTION_SPAWN_DEFAULT);
        pedit.commit();
    }

    public static int getMinimumVariable() {
        Context context = GolApplication.getAppContext();
        SharedPreferences sprefs = PreferenceManager.getDefaultSharedPreferences(context);
        return Integer.parseInt(sprefs.getString(OPTION_MINIMUM, OPTION_MINIMUM_DEFAULT));
    }

    public static int getMaximumVariable() {
        Context context = GolApplication.getAppContext();
        SharedPreferences sprefs = PreferenceManager.getDefaultSharedPreferences(context);
        return Integer.parseInt(sprefs.getString(OPTION_MAXIMUM, OPTION_MAXIMUM_DEFAULT));
    }

    public static int getSpawnVariable() {
        Context context = GolApplication.getAppContext();
        SharedPreferences sprefs = PreferenceManager.getDefaultSharedPreferences(context);
        return Integer.parseInt(sprefs.getString(OPTION_SPAWN, OPTION_SPAWN_DEFAULT));
    }

    public static int getAnimationSpeed() {
        Context context = GolApplication.getAppContext();
        SharedPreferences sprefs = PreferenceManager.getDefaultSharedPreferences(context);
        return Integer.parseInt(sprefs.getString(OPTION_ANIMATION_SPEED,
                OPTION_ANIMATION_SPEED_DEFAULT));
    }

    public static int getDisplayTheme() {
        Context context = GolApplication.getAppContext();
        SharedPreferences sprefs = PreferenceManager.getDefaultSharedPreferences(context);
        // first get the index of the theme
        String themeNdx = sprefs.getString(OPTION_DISPLAY_THEME,
                OPTION_DISPLAY_THEME_DEFAULT);
        int ndx = Integer.parseInt(themeNdx);
        // now convert that index to a real resource
        Resources res = context.getResources();
        TypedArray themes = res.obtainTypedArray(R.array.themes);
        return themes.getResourceId(ndx, R.array.dark_theme);
    }

}
