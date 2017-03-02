package com.example.ojboba.inventoryapp.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by OjBoba on 2/22/2017.
 */

public final class InventoryContract {

    // To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.
    private InventoryContract(){
    }

    /**
     * The "Content authority" is a name for the entire content provider, similar to the
     * relationship between a domain name and its website.  A convenient string to use for the
     * content authority is the package name for the app, which is guaranteed to be unique on the
     * device.
     */
    public static final String CONTENT_AUTHORITY = "com.example.android.inventories";

    /**
     * Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
     * the content provider.
     */
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_INVENTORIES = "inventories";

    /**
     * Possible path (appended to base content URI for possible URI's)
     * For instance, content://com.example.android.inventories/inventories/ is a valid path for
     * looking at pet data. content://com.example.android.inventories/staff/ will fail,
     * as the ContentProvider hasn't been given any information on what to do with "staff".
     */

    /**
     * Inner class that defines constant values for the inventories database table.
     * Each entry in the table represents a single inventory.
     */
    public static final class InventoryEntry implements BaseColumns {

        /**
         * The content URI to access the inventory data in the provider
         */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_INVENTORIES);

        /**
         * The MIME type of the {@link #CONTENT_URI} for a list of inventories.
         */

        public static final String CONTENT_LIST_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_INVENTORIES;

        /**
         * The MIME type of the {@link #CONTENT_URI} for a single inventory.
         */

        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_INVENTORIES;

        /**
         * Name of database table for inventory
         */
        public final static String TABLE_NAME = "inventories";

        /**
         * Unique ID number for the pet (only for use in the database table).
         * Type: INTEGER
         */
        public final static String _ID = BaseColumns._ID;

        /**
         * Name of the inventory.
         * Type: TEXT
         */
        public final static String COLUMN_INVENTORY_NAME = "name";

        /**
         * Price of the inventory.
         * Type: INTEGER
         */
        public final static String COLUMN_INVENTORY_PRICE  = "price";

        /**
         * Supplier of the inventory.
         * Type: TEXT
         */
        public final static String COLUMN_INVENTORY_SUPPLIER = "supplier";

        /**
         * Weight of the pet.
         * Type: INTEGER
         */
        public final static String COLUMN_INVENTORY_QUANTITY = "quantity";


        public final static String COLUMN_INVENTORY_SALES = "sales";

        public final static String COLUMN_INVENTORY_PHOTO_URI = "uri";
    }

}
