package edu.msu.carro228.pickup.Utility;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.HashSet;
import java.util.Set;

public class PrefrencesUtility {

    private static SharedPreferences settings = null;

    private static String MINE = "pickup.settings.mine";
    private static String JOINED = "pickup.settings.joined";

    public static void writeJoined(Context context, Set<String> joined){
        init(context);
        SharedPreferences.Editor editor = settings.edit();
        editor.putStringSet(JOINED, joined);
        editor.commit();
    }

    public static void writeMine(Context context, Set<String> mine){
        init(context);
        SharedPreferences.Editor editor = settings.edit();
        editor.putStringSet(MINE, mine);
        editor.commit();
    }

    public static Set<String> readJoined(Context context){
        init(context);
        return settings.getStringSet(JOINED, new HashSet<String>());
    }

    public static Set<String> readMine(Context context){
        init(context);
        return settings.getStringSet(MINE, new HashSet<String>());
    }

    private static void init(Context context){
        if (settings == null){
            settings = PreferenceManager.getDefaultSharedPreferences(context);
        }
    }
}
