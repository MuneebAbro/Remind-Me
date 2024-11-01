package com.muneeb.remindme.utility

import android.content.Context
import android.content.SharedPreferences

object PreferenceManager {

    private const val PREFERENCES_NAME = "StudentReminderPreferences"
    private const val KEY_USER_NAME = "user_name"

    private fun getPreferences(context : Context) : SharedPreferences {
        return context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
    }

    fun saveUserName(context : Context, userName : String) {
        val editor = getPreferences(context).edit()
        editor.putString(KEY_USER_NAME, userName)
        editor.apply()
    }

    fun getUserName(context : Context) : String? {
        return getPreferences(context).getString(KEY_USER_NAME, null)
    }
}
