package com.example.tanushreechaubal.mobilephoneinventory;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.example.tanushreechaubal.mobilephoneinventory.data.PhoneContract.PhoneEntry;

public class MobilePhoneInventoryMainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    private  PhoneCursorAdapter pAdapter;
    private static final int PHONE_LOADER = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mobile_phone_inventory_main);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MobilePhoneInventoryMainActivity.this, AddPhoneActivity.class);
                startActivity(intent);
            }
        });

        ListView phoneListView = findViewById(R.id.list);

        View emptyView = findViewById(R.id.empty_list_view);
        phoneListView.setEmptyView(emptyView);

        pAdapter = new PhoneCursorAdapter(this,null);
        phoneListView.setAdapter(pAdapter);

        phoneListView.setItemsCanFocus(true);

        phoneListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(final AdapterView<?> adapterView, View view, final int position, long id) {
                Intent intent =  new Intent(MobilePhoneInventoryMainActivity.this, DetailActivity.class);
                Uri currentPhoneUri = ContentUris.withAppendedId(PhoneEntry.CONTENT_URI, id);
                intent.setData(currentPhoneUri);
                startActivity(intent);
            }
        });

        getLoaderManager().initLoader(PHONE_LOADER, null, this);
    }

    public void addToDB(int colID, int quantity){
        ContentValues values = new ContentValues();
        values.put(PhoneEntry.COLUMN_PHONE_QUANTITY, quantity);
        Uri updateUri = ContentUris.withAppendedId(PhoneEntry.CONTENT_URI, colID);
        int rowsAffected = getContentResolver().update(updateUri, values, null, null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main_list_view, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            case R.id.action_delete_all_entries:
                deleteAllPhones();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void deleteAllPhones(){
        int rowsDeleted = getContentResolver().delete(PhoneEntry.CONTENT_URI, null, null);
        Log.v("MainActivity", rowsDeleted + " rows deleted from phone database.");
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {

        String[] projection = {
                PhoneEntry._ID,
                PhoneEntry.COLUMN_PHONE_NAME,
                PhoneEntry.COLUMN_PHONE_QUANTITY,
                PhoneEntry.COLUMN_PHONE_PRICE,
                PhoneEntry.COLUMN_PHONE_SUPPLIER
        };

        return new CursorLoader(
                MobilePhoneInventoryMainActivity.this,
                PhoneEntry.CONTENT_URI,
                projection,
                null,
                null,
                null
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        pAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        pAdapter.swapCursor(null);
    }

}
