package com.example.ojboba.inventoryapp;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.example.ojboba.inventoryapp.data.InventoryContract.InventoryEntry;
/**
 * Created by OjBoba on 2/22/2017.
 */

public class InventoryCursorAdapter extends CursorAdapter{

    public InventoryCursorAdapter(Context context, Cursor c) {
        super(context, c);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        // Inflate a list item view using the layout specified in list_item.xml
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // Find individual views that we want to modify in the list item layout
        TextView nameTextView = (TextView) view.findViewById(R.id.item_name);
        TextView priceTextView = (TextView) view.findViewById(R.id.item_price);
        TextView quantityTextView = (TextView) view.findViewById(R.id.item_quantity);

        // Find the columns of inventory attributes that we're interested in
        int nameColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_INVENTORY_NAME);
        int priceColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_INVENTORY_PRICE);
        int quantityColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_INVENTORY_QUANTITY);

        // Read the inventory attributes from the Cursor for the current inventory
        String InventoryName = cursor.getString(nameColumnIndex);
        String InventoryPrice = cursor.getString(priceColumnIndex);
        String InventoryQuantity = cursor.getString(quantityColumnIndex);

        // If the inventory supplier is empty string or null, then use some default text
        // that says "Unknown Supplier", so the TextView isn't blank.
        if (TextUtils.isEmpty(InventoryQuantity)) {
            InventoryQuantity = context.getString(R.string.unknown_supplier);
        }

        // Update the TextViews with the attributes for the current inventory
        nameTextView.setText(InventoryName);
        priceTextView.setText(InventoryPrice);
        quantityTextView.setText(InventoryQuantity);

    }
}
