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
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.example.tanushreechaubal.mobilephoneinventory.data.PhoneContract.PhoneEntry;

import java.io.ByteArrayOutputStream;

public class AddPhoneActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    private Uri mCurrentPhoneUri;
    private static final int EXISTING_PHONE_LOADER = 0;
    private EditText phoneName;
    private EditText phoneQuantity;
    private EditText phonePrice;
    private EditText phoneSupplier;
    private ImageView phoneImageSelected;
    private ImageButton uploadImage;
    private TextView imageTextView;
    private ImageView imageUploadedImageView;
    private TextView clickUploadButtonTextView;
    private static int RESULT_LOAD_IMAGE = 1;
    private boolean mPhoneHasChanged = false;
    private static int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 1001;

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
        setContentView(R.layout.activity_add_phone);

        Intent intent = getIntent();
        mCurrentPhoneUri = intent.getData();
        getLoaderManager().initLoader(EXISTING_PHONE_LOADER, null, this);

        phoneName = findViewById(R.id.edit_phone_name);
        phoneQuantity = findViewById(R.id.edit_phone_quantity);
        phonePrice = findViewById(R.id.edit_phone_price);
        phoneSupplier = findViewById(R.id.edit_phone_supplier);
        phoneImageSelected = findViewById(R.id.image_uploaded_imageView);

        phoneName.setOnTouchListener(mTouchListener);
        phoneQuantity.setOnTouchListener(mTouchListener);
        phonePrice.setOnTouchListener(mTouchListener);
        phoneSupplier.setOnTouchListener(mTouchListener);

        //Add phone image
        uploadImage = findViewById(R.id.upload_image_imageButton);
        uploadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, RESULT_LOAD_IMAGE);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (ContextCompat.checkSelfPermission(this,Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
            return;

        } else {

            if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
                Uri selectedImage = data.getData();
                String[] filePathColumn = {MediaStore.Images.Media.DATA};

                Cursor cursor = getContentResolver().query(selectedImage,
                        filePathColumn, null, null, null);
                cursor.moveToFirst();

                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                String picturePath = cursor.getString(columnIndex);
                cursor.close();

                uploadImage = findViewById(R.id.upload_image_imageButton);
                uploadImage.setVisibility(View.GONE);
                clickUploadButtonTextView = findViewById(R.id.click_to_upload_textView);
                clickUploadButtonTextView.setVisibility(View.GONE);
                imageTextView = findViewById(R.id.image_textView);
                imageTextView.setVisibility(View.GONE);
                imageUploadedImageView = findViewById(R.id.image_uploaded_imageView);
                imageUploadedImageView.setImageBitmap(BitmapFactory.decodeFile(picturePath));
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_add_phone_view, menu);
        return true;
    }

    public void savePhone(){

        String phoneNameString = phoneName.getText().toString().trim();
        String phoneQuantityString = phoneQuantity.getText().toString().trim();
        String phonePriceString = phonePrice.getText().toString().trim();
        String phoneSupplierString = phoneSupplier.getText().toString().trim();
        phoneImageSelected.buildDrawingCache();
        Bitmap image = phoneImageSelected.getDrawingCache();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 0, stream);
        byte[] imageInBytes = stream.toByteArray();

        if(mCurrentPhoneUri == null && TextUtils.isEmpty(phoneNameString) && TextUtils.isEmpty(phoneQuantityString) && TextUtils.isEmpty(phonePriceString)
                && TextUtils.isEmpty(phoneSupplierString)){
            return;
        }

        ContentValues values = new ContentValues();
        values.put(PhoneEntry.COLUMN_PHONE_NAME, phoneNameString);
        values.put(PhoneEntry.COLUMN_PHONE_QUANTITY, phoneQuantityString);
        values.put(PhoneEntry.COLUMN_PHONE_PRICE, phonePriceString);
        values.put(PhoneEntry.COLUMN_PHONE_SUPPLIER, phoneSupplierString);
        values.put(PhoneEntry.COLUMN_PHONE_IMAGE, imageInBytes);

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
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            case R.id.action_save:
                String phoneNameString = phoneName.getText().toString().trim();
                String phoneQuantityString = phoneQuantity.getText().toString().trim();
                String phonePriceString = phonePrice.getText().toString().trim();
                String phoneSupplierString = phoneSupplier.getText().toString().trim();
                phoneImageSelected.buildDrawingCache();
                if(!TextUtils.isEmpty(phoneNameString) && !TextUtils.isEmpty(phoneQuantityString) && !TextUtils.isEmpty(phonePriceString)
                        && !TextUtils.isEmpty(phoneSupplierString) && phoneImageSelected.getDrawable() != null){
                    savePhone();
                    finish();
                    return true;
                }
                else {
                    Toast.makeText(AddPhoneActivity.this,getText(R.string.all_fields_required_error_toast), Toast.LENGTH_SHORT).show();
                    return false;
                }
            case android.R.id.home:
                if(!mPhoneHasChanged){
                    NavUtils.navigateUpFromSameTask(AddPhoneActivity.this);
                    return  true;
                }
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                NavUtils.navigateUpFromSameTask(AddPhoneActivity.this);
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

    @Override
    public boolean onPrepareOptionsMenu(Menu menu){
        super.onPrepareOptionsMenu(menu);

        if(mCurrentPhoneUri == null){
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        Intent intent = getIntent();
        mCurrentPhoneUri = intent.getData();

        if(mCurrentPhoneUri ==  null){
            return null;
        }

        Log.v("URI","URI: " + mCurrentPhoneUri);

        String[] projection = {
                PhoneEntry._ID,
                PhoneEntry.COLUMN_PHONE_NAME,
                PhoneEntry.COLUMN_PHONE_QUANTITY,
                PhoneEntry.COLUMN_PHONE_PRICE,
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
            int nameColumnIndex = cursor.getColumnIndex(PhoneEntry.COLUMN_PHONE_NAME);
            int quantityColumnIndex = cursor.getColumnIndex(PhoneEntry.COLUMN_PHONE_QUANTITY);
            int priceColumnIndex = cursor.getColumnIndex(PhoneEntry.COLUMN_PHONE_PRICE);
            int supplierColumnIndex = cursor.getColumnIndex(PhoneEntry.COLUMN_PHONE_SUPPLIER);
            int imageColumnIndex = cursor.getColumnIndex(PhoneEntry.COLUMN_PHONE_IMAGE);

            String name = cursor.getString(nameColumnIndex);
            int quantityVal = cursor.getInt(quantityColumnIndex);
            String quantity = Integer.toString(quantityVal);
            int priceVal = cursor.getInt(priceColumnIndex);
            String price = Integer.toString(priceVal);
            int supplierVal = cursor.getInt(supplierColumnIndex);
            String supplier = Integer.toString(supplierVal);
            byte[] imageByteArray = cursor.getBlob(imageColumnIndex);
            Bitmap byteToImage = BitmapFactory.decodeByteArray(imageByteArray, 0, imageByteArray.length);

            phoneName.setText(name);
            phoneQuantity.setText(quantity);
            phonePrice.setText(price);
            phoneSupplier.setText(supplier);
            phoneImageSelected.setImageBitmap(byteToImage);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        phoneName.setText("");
        phoneQuantity.setText("");
        phonePrice.setText("");
        phoneSupplier.setText("");
        phoneImageSelected.setVisibility(View.GONE);
    }

}
