package com.example.mynote.firebase;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.mynote.models.User;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class RecentAccountManager {

    private static final String PREF_NAME = "recent_accounts_pref";
    private static final String KEY_RECENT_ACCOUNTS = "recent_accounts";

    public static void saveRecentAccount(Context context, User user) {
        if (context == null || user == null || user.getUid() == null || user.getUid().isEmpty()) {
            return;
        }

        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String json = prefs.getString(KEY_RECENT_ACCOUNTS, "[]");

        try {
            JSONArray oldArray = new JSONArray(json);
            JSONArray newArray = new JSONArray();

            JSONObject currentObj = new JSONObject();
            currentObj.put("uid", user.getUid());
            currentObj.put("fullName", user.getFullName() != null ? user.getFullName() : "");
            currentObj.put("email", user.getEmail() != null ? user.getEmail() : "");
            currentObj.put("createdAt", user.getCreatedAt());

            newArray.put(currentObj);

            for (int i = 0; i < oldArray.length(); i++) {
                JSONObject obj = oldArray.getJSONObject(i);
                String oldUid = obj.optString("uid", "");

                if (!oldUid.equals(user.getUid()) && newArray.length() < 2) {
                    newArray.put(obj);
                }
            }

            prefs.edit().putString(KEY_RECENT_ACCOUNTS, newArray.toString()).apply();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static List<User> getRecentAccounts(Context context) {
        List<User> result = new ArrayList<>();
        if (context == null) return result;

        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String json = prefs.getString(KEY_RECENT_ACCOUNTS, "[]");

        try {
            JSONArray array = new JSONArray(json);
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);

                User user = new User();
                user.setUid(obj.optString("uid", ""));
                user.setFullName(obj.optString("fullName", ""));
                user.setEmail(obj.optString("email", ""));
                user.setCreatedAt(obj.optLong("createdAt", 0));

                result.add(user);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }
}