package com.example.ojboba.inventoryapp;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.ojboba.inventoryapp.data.InventoryContract.InventoryEntry;

/**
 * Created by OjBoba on 2/22/2017.
 */

public class InventoryCursorAdapter extends CursorAdapter {

    public InventoryCursorAdapter(Context context, Cursor c) {
        super(context, c);
    }

    private static final String LOG_TAG = EditorActivity.class.getSimpleName();


    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        // Inflate a list item view using the layout specified in list_item.xml
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor) {

        // Tracks down quantity for each list item per ID
        final int itemId = cursor.getInt(cursor.getColumnIndexOrThrow(InventoryEntry._ID));
        final int itemQuantity = cursor.getInt(cursor.getColumnIndex(InventoryEntry.COLUMN_INVENTORY_QUANTITY));
        final int itemSold = cursor.getInt(cursor.getColumnIndex(InventoryEntry.COLUMN_INVENTORY_SALES));

        // Find individual views that we want to modify in the list item layout
        TextView nameTextView = (TextView) view.findViewById(R.id.item_name);
        TextView priceTextView = (TextView) view.findViewById(R.id.item_price);
        final TextView quantityTextView = (TextView) view.findViewById(R.id.item_quantity);
        final TextView soldTextView = (TextView) view.findViewById(R.id.sold_items);

        // Find the columns of inventory attributes that we're interested in
        int nameColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_INVENTORY_NAME);
        int priceColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_INVENTORY_PRICE);
        int quantityColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_INVENTORY_QUANTITY);
        int soldColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_INVENTORY_SALES);


        // Read the inventory attributes from the Cursor for the current inventory
        String InventoryName = cursor.getString(nameColumnIndex);
        String InventoryPrice = cursor.getString(priceColumnIndex);
        String InventoryQuantity = cursor.getString(quantityColumnIndex);
        String InventorySold = cursor.getString(soldColumnIndex);


        // Update the TextViews with the attributes for the current inventory
        nameTextView.setText("Item Name: " + InventoryName);
        priceTextView.setText("Item Price: $" + InventoryPrice);
        quantityTextView.setText("Item Quantity: " + InventoryQuantity);
        soldTextView.setText("Sold: " + InventorySold);

        Button salesButton = (Button)view.findViewById(R.id.salesButton);

        salesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e(LOG_TAG, "Sale button was pressed");
                ContentValues values = new ContentValues();

                if (itemQuantity > 0) {
                    int newItemQuantity;
                    newItemQuantity = (itemQuantity - 1);
                    values.put(InventoryEntry.COLUMN_INVENTORY_QUANTITY, newItemQuantity);
                    int newSoldQuantity;
                    newSoldQuantity = (itemSold +1);
                    values.put(InventoryEntry.COLUMN_INVENTORY_SALES, newSoldQuantity);
                    Uri uri = ContentUris.withAppendedId(InventoryEntry.CONTENT_URI, itemId);
                    context.getContentResolver().update(uri, values, null, null);
                }
                context.getContentResolver().notifyChange(InventoryEntry.CONTENT_URI, null);
            }
        });
    }
}
