package com.example.tanushreechaubal.mobilephoneinventory.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;
import com.example.tanushreechaubal.mobilephoneinventory.data.PhoneContract.PhoneEntry;

/**
 * Created by TanushreeChaubal on 5/27/18.
 */

public class PhoneProvider extends ContentProvider{
    private PhoneDbHelper mPhoneDbHelper;
    public static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    public static final int PHONES = 100, PHONE_ID = 200;
    public static final String LOG_TAG = PhoneProvider.class.getSimpleName();

    /**
     * Initialize the provider and the database helper object.
     */
    @Override
    public boolean onCreate() {
        mPhoneDbHelper = new PhoneDbHelper(getContext());
        return true;
    }

    static{
        final UriMatcher matcher = sUriMatcher;
        matcher.addURI(PhoneContract.CONTENT_AUTHORITY, "/phones", PHONES);
        matcher.addURI(PhoneContract.CONTENT_AUTHORITY, "/phones/#", PHONE_ID);
    }

    /**
     * Perform the query for the given URI. Use the given projection, selection, selection arguments, and sort order.
     */
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {

        // Get readable database
        SQLiteDatabase database = mPhoneDbHelper.getReadableDatabase();

        Cursor cursor = null;

        // Figure out if the URI matcher can match the URI to a specific code
        int match = sUriMatcher.match(uri);
        switch (match) {
            case PHONES:
                cursor = database.query(PhoneEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, null);
                break;
            case PHONE_ID:
                selection = PhoneEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };

                cursor = database.query(PhoneEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }
        // Set notification URI on the Cursor,
        // so we know what content URI the Cursor was created for.
        // If the data at this URI changes, then we know we need to update the Cursor.
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    /**
     * Insert new data into the provider with the given ContentValues.
     */
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PHONES:
                return insertPhone(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    /**
     * Insert a pet into the database with the given content values. Return the new content URI
     * for that specific row in the database.
     */
    private Uri insertPhone(Uri uri, ContentValues values) {

        //Data validations
        // Check that the name is not null
        String name = values.getAsString(PhoneEntry.COLUMN_PHONE_NAME);
        if (name == null) {
            throw new IllegalArgumentException("Phone requires a name");
        }

        Integer quantity = values.getAsInteger(PhoneEntry.COLUMN_PHONE_QUANTITY);
        if (quantity == null) {
            throw new IllegalArgumentException("Phone requires some quantity");
        }

        Integer price = values.getAsInteger(PhoneEntry.COLUMN_PHONE_PRICE);
        if (price != null && price < 0) {
            throw new IllegalArgumentException("Phone requires valid price");
        }

        byte[] imageInByteArray = values.getAsByteArray(PhoneEntry.COLUMN_PHONE_IMAGE);
        if(imageInByteArray == null){
            throw new IllegalArgumentException("Phone requires an image");
        }

        SQLiteDatabase db = mPhoneDbHelper.getWritableDatabase();
        long id = db.insert(PhoneEntry.TABLE_NAME, null, values);

        if(id == -1){
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }

        getContext().getContentResolver().notifyChange(uri, null);

        return ContentUris.withAppendedId(uri, id);
    }

    /**
     * Updates the data at the given selection and selection arguments, with the new ContentValues.
     */
    @Override
    public int update(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PHONES:
                return updatePhone(uri, contentValues, selection, selectionArgs);
            case PHONE_ID:
                selection = PhoneEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                return updatePhone(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    private int updatePhone(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        if (values.containsKey(PhoneEntry.COLUMN_PHONE_NAME)) {
            String name = values.getAsString(PhoneEntry.COLUMN_PHONE_NAME);
            if (name == null) {
                throw new IllegalArgumentException("Phone requires a name");
            }
        }

        if (values.containsKey(PhoneEntry.COLUMN_PHONE_QUANTITY)) {
            Integer quantity = values.getAsInteger(PhoneEntry.COLUMN_PHONE_QUANTITY);
            if (quantity == null) {
                throw new IllegalArgumentException("Phone requires some quantity");
            }
        }

        if (values.containsKey(PhoneEntry.COLUMN_PHONE_PRICE)) {
            Integer price = values.getAsInteger(PhoneEntry.COLUMN_PHONE_PRICE);
            if (price == null) {
                throw new IllegalArgumentException("Phone requires valid price");
            }
        }

        if (values.size() == 0) {
            return 0;
        }

        SQLiteDatabase db = mPhoneDbHelper.getWritableDatabase();
        //return db.update(PetsEntry.TABLE_NAME, values, selection, selectionArgs);

        int rowsUpdated = db.update(PhoneEntry.TABLE_NAME, values, selection, selectionArgs);
        if(rowsUpdated != 0){
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsUpdated;
    }

    /**
     * Delete the data at the given selection and selection arguments.
     */
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase database = mPhoneDbHelper.getWritableDatabase();

        int rowsDeleted;

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PHONES:
                rowsDeleted = database.delete(PhoneEntry.TABLE_NAME, selection,selectionArgs);
                break;
            case PHONE_ID:
                selection = PhoneEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = database.delete(PhoneEntry.TABLE_NAME, selection,selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }

        if(rowsDeleted != 0){
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    @Override
    public String getType(Uri uri) {

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PHONES:
                return PhoneEntry.CONTENT_LIST_TYPE;
            case PHONE_ID:
                return PhoneEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }
}
