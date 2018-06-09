package com.example.tanushreechaubal.mobilephoneinventory.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by TanushreeChaubal on 5/27/18.
 */

public final class PhoneContract {
    public static final String CONTENT_AUTHORITY = "com.example.tanushreechaubal.mobilephoneinventory";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_PHONES = "phones";

    //To avoid initializing this class, creating an empty constructor
    private PhoneContract(){}

    public final static class PhoneEntry implements BaseColumns{

        public static final String TABLE_NAME = "phones";

        public final static String _ID = BaseColumns._ID;

        public final static String COLUMN_PHONE_NAME = "name";
        public final static String COLUMN_PHONE_QUANTITY = "quantity";
        public final static String COLUMN_PHONE_PRICE = "price";
        public final static String COLUMN_PHONE_SUPPLIER = "supplier";
        public final static String COLUMN_PHONE_IMAGE = "image";

        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_PHONES);

        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PHONES;

        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PHONES;

    }
}
