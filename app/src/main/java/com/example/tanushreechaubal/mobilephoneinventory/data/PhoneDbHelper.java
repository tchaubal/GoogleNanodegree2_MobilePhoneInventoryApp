package com.example.tanushreechaubal.mobilephoneinventory.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import com.example.tanushreechaubal.mobilephoneinventory.data.PhoneContract.PhoneEntry;

/**
 * Created by TanushreeChaubal on 5/27/18.
 */

public class PhoneDbHelper extends SQLiteOpenHelper {
    public static final String DB_NAME = "phoneinventory.db";
    public static final int DB_VERSION = 1;
    public static final String LOG_TAG = PhoneDbHelper.class.getSimpleName();

    public PhoneDbHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String SQL_CREATE_PHONE_TABLE =  "CREATE TABLE " + PhoneEntry.TABLE_NAME + " ("
                + PhoneEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + PhoneEntry.COLUMN_PHONE_NAME + " TEXT NOT NULL, "
                + PhoneEntry.COLUMN_PHONE_QUANTITY + " INTEGER, "
                + PhoneEntry.COLUMN_PHONE_PRICE + " INTEGER NOT NULL DEFAULT 0, "
                + PhoneEntry.COLUMN_PHONE_SUPPLIER + " INTEGER, "
                + PhoneEntry.COLUMN_PHONE_IMAGE + " BLOB );";

        db.execSQL(SQL_CREATE_PHONE_TABLE);

        Log.v(LOG_TAG, SQL_CREATE_PHONE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {}
}
