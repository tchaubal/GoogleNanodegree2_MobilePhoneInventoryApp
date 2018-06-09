package com.example.tanushreechaubal.mobilephoneinventory;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;
import com.example.tanushreechaubal.mobilephoneinventory.data.PhoneContract.PhoneEntry;

/**
 * Created by TanushreeChaubal on 5/27/18.
 */

public class PhoneCursorAdapter extends CursorAdapter{
    private Button saleButton;
    private TextView quantityTextView;

    public PhoneCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 /* flags */);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.activity_list_item, parent, false);
    }

    @Override
    public void bindView(final View view, final Context context, final Cursor cursor) {
        TextView nameTextView = view.findViewById(R.id.name_textView);
        quantityTextView = view.findViewById(R.id.current_quantity_textView);
        TextView priceTextView = view.findViewById(R.id.price_textView);
        int idColumnIndex = cursor.getColumnIndex(PhoneEntry._ID);
        final int col = cursor.getInt(idColumnIndex);
        int nameColumnIndex = cursor.getColumnIndex(PhoneEntry.COLUMN_PHONE_NAME);
        int quantityColumnIndex = cursor.getColumnIndex(PhoneEntry.COLUMN_PHONE_QUANTITY);
        int priceColumnIndex = cursor.getColumnIndex(PhoneEntry.COLUMN_PHONE_PRICE);
        String phoneName = cursor.getString(nameColumnIndex);
        String phoneCurrentQuantity = cursor.getString(quantityColumnIndex);
        int phoneCurrentPrice = cursor.getInt(priceColumnIndex);
        String phoneCurrentPriceString = Integer.toString(phoneCurrentPrice);

        nameTextView.setText(phoneName);
        quantityTextView.setText(phoneCurrentQuantity);
        priceTextView.setText(phoneCurrentPriceString);

        saleButton = view.findViewById(R.id.sale_button);
        final int position = cursor.getPosition();
        saleButton.setTag(position);
        quantityTextView.setTag(position);

        saleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = (Integer) v.getTag();
                ViewGroup row = (ViewGroup) v.getParent();
                TextView currentQuantityTextView;
                for (int itemPos = 0; itemPos < row.getChildCount(); itemPos++) {
                    View view = row.getChildAt(itemPos);
                    if(view.getId() == R.id.list_item_linear_layout){
                        ViewGroup linearLayoutViewGroup = (ViewGroup) view;
                        for(int linearPos = 0; linearPos < linearLayoutViewGroup.getChildCount(); linearPos++) {
                            View view1 = linearLayoutViewGroup.getChildAt(linearPos);
                            if(view1.getId() == R.id.relativeLayout_quantityTextViews){
                                ViewGroup relativeLayoutViewGroup = (ViewGroup) view1;
                                for(int relPos = 0; relPos < relativeLayoutViewGroup.getChildCount(); relPos++) {
                                    View view2 = relativeLayoutViewGroup.getChildAt(relPos);
                                    if (view2.getId() == R.id.current_quantity_textView) {
                                        currentQuantityTextView = (TextView) view2;
                                        String quantityToDecrease = currentQuantityTextView.getText().toString().trim();
                                        int quantityVal = Integer.parseInt(quantityToDecrease);
                                        if (quantityVal > 0) {
                                            quantityVal = quantityVal - 1;
                                            currentQuantityTextView.setText(Integer.toString(quantityVal));
                                            String currentQuantityString = currentQuantityTextView.getText().toString().trim();
                                            MobilePhoneInventoryMainActivity mobilePhoneInventoryMainActivity = (MobilePhoneInventoryMainActivity) context;
                                            mobilePhoneInventoryMainActivity.addToDB( Integer.valueOf(col), Integer.valueOf(currentQuantityString));
                                        }
                                        return;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        });
    }
}
