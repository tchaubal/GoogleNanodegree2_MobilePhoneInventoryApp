package com.example.tanushreechaubal.mobilephoneinventory;

import android.Manifest;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.example.tanushreechaubal.mobilephoneinventory.data.PhoneContract.PhoneEntry;

/**
 * Created by TanushreeChaubal on 5/27/18.
 */

public class DetailActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    private Uri mCurrentPhoneUri;
    private static final int EXISTING_PHONE_LOADER = 0;
    private TextView phoneQuantityAvailable;
    private int quantityPreFilled = 20;
    private TextView phoneSupplier;
    private Button increaseQuantity;
    private Button decreaseQuantity;
    private Button orderFromSupplier;
    private Button deleteRecord;
    private int quantityValue;
    private String supplier;
    private ImageView phoneImageSelected;
    private boolean mPhoneHasChanged = false;

    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mPhoneHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_details);

        Intent intent = getIntent();
        mCurrentPhoneUri = intent.getData();
        getLoaderManager().initLoader(EXISTING_PHONE_LOADER, null, this);

        phoneSupplier = findViewById(R.id.show_supplier_textView);
        phoneImageSelected = findViewById(R.id.details_imageView);

        phoneSupplier.setOnTouchListener(mTouchListener);
        phoneImageSelected.setOnTouchListener(mTouchListener);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_details_view, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            case R.id.action_save:
                savePhone();
                finish();
                return true;
            case android.R.id.home:
                if(!mPhoneHasChanged){
                    NavUtils.navigateUpFromSameTask(DetailActivity.this);
                    return  true;
                }
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                NavUtils.navigateUpFromSameTask(DetailActivity.this);
                            }
                        };
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showUnsavedChangesDialog(DialogInterface.OnClickListener discardButtonClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    public void savePhone(){

        phoneQuantityAvailable = findViewById(R.id.show_quantity_textView);
        phoneSupplier = findViewById(R.id.show_supplier_textView);
        String phoneChangedQuantityString = phoneQuantityAvailable.getText().toString().trim();
        String phoneChangedSupplierString = String.valueOf(phoneSupplier.getText().toString().trim());

        if(mCurrentPhoneUri == null && TextUtils.isEmpty(phoneChangedQuantityString) && TextUtils.isEmpty(phoneChangedSupplierString)){
            return;
        }

        ContentValues values = new ContentValues();
        values.put(PhoneEntry.COLUMN_PHONE_QUANTITY, phoneChangedQuantityString);
        values.put(PhoneEntry.COLUMN_PHONE_SUPPLIER, phoneChangedSupplierString);

        if(mCurrentPhoneUri == null){
            Uri newUri = getContentResolver().insert(PhoneEntry.CONTENT_URI, values);
            if(newUri == null){
                Toast.makeText(this, R.string.error_saving_phone, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.phone_saved_successfully) + newUri, Toast.LENGTH_SHORT).show();
            }
        } else {
            int editUri = getContentResolver().update(mCurrentPhoneUri, values, null,null);
            if(editUri == 0){
                Toast.makeText(this, R.string.error_editing_phone_details, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.phone_edited_successfully) + editUri, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Intent intent = getIntent();
        mCurrentPhoneUri = intent.getData();

        if(mCurrentPhoneUri ==  null){
            return null;
        }

        Log.v("URI","URI: " + mCurrentPhoneUri);

        String[] projection = {
                PhoneEntry._ID,
                PhoneEntry.COLUMN_PHONE_QUANTITY,
                PhoneEntry.COLUMN_PHONE_SUPPLIER,
                PhoneEntry.COLUMN_PHONE_IMAGE
        };

        return new CursorLoader(
                this,
                mCurrentPhoneUri,
                projection,
                null,
                null,
                null
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if(cursor == null || cursor.getCount() < 1){
            return;
        }

        if(cursor.moveToFirst()){
            phoneQuantityAvailable = findViewById(R.id.show_quantity_textView);
            phoneSupplier = findViewById(R.id.show_supplier_textView);
            phoneQuantityAvailable.setText(Integer.toString(quantityPreFilled));
            phoneImageSelected = findViewById(R.id.details_imageView);

            int quantityColumnIndex = cursor.getColumnIndex(PhoneEntry.COLUMN_PHONE_QUANTITY);
            int supplierColumnIndex = cursor.getColumnIndex(PhoneEntry.COLUMN_PHONE_SUPPLIER);
            int imageColumnIndex = cursor.getColumnIndex(PhoneEntry.COLUMN_PHONE_IMAGE);

            final String quantity = cursor.getString(quantityColumnIndex);
            supplier = cursor.getString(supplierColumnIndex);
            byte[] image = cursor.getBlob(imageColumnIndex);
            Bitmap byteToImage = BitmapFactory.decodeByteArray(image, 0, image.length);

            phoneQuantityAvailable.setText(quantity);
            phoneSupplier.setText(supplier);
            phoneImageSelected.setImageBitmap(byteToImage);

            increaseQuantity = findViewById(R.id.increase_quantity_button);
            decreaseQuantity = findViewById(R.id.decrease_quantity_button);

            quantityValue = Integer.parseInt(quantity);
            increaseQuantity.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(quantityValue > 0){
                        quantityValue = quantityValue + 1;
                        phoneQuantityAvailable.setText(Integer.toString(quantityValue));
                    } else {
                        return;
                    }
                }
            });

            decreaseQuantity.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(quantityValue > 0){
                        quantityValue = quantityValue - 1;
                        phoneQuantityAvailable.setText(Integer.toString(quantityValue));
                    } else {
                        return;
                    }
                }
            });

            orderFromSupplier = findViewById(R.id.order_button);
            Log.v("Phone Number", "Supplier phone number from db: "+supplier);
            orderFromSupplier.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(DetailActivity.this,"supplier number is: "+supplier, Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel: " + supplier));
                    startActivity(intent);
                }
            });

            deleteRecord = findViewById(R.id.delete_button);
            deleteRecord.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showDeleteConfirmationDialog();
                }
            });
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        phoneQuantityAvailable.setText("");
        phoneSupplier.setText("");
    }

    @Override
    public void onBackPressed(){
        if(!mPhoneHasChanged){
            super.onBackPressed();
            return;
        }

        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                };
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete_phone, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                deletePhone();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void deletePhone() {
        if (mCurrentPhoneUri != null) {
            int rowsDeleted = getContentResolver().delete(mCurrentPhoneUri, null, null);

            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(DetailActivity.this, getString(R.string.error_deleting_phone) + rowsDeleted, Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(DetailActivity.this, getString(R.string.phone_deleted_successfully) + rowsDeleted,
                        Toast.LENGTH_SHORT).show();
            }
        }
        finish();
    }
}
