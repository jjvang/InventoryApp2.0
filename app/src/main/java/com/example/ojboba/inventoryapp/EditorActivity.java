package com.example.ojboba.inventoryapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ojboba.inventoryapp.data.InventoryContract.InventoryEntry;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by OjBoba on 2/22/2017.
 */

public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = EditorActivity.class.getSimpleName();

//----------------------------------Variables-------------------------------------------------------

    int quantity;
    int inputQuantity = 0;
    float calculatePrice = 0;
//-----------------------------PRIVATE Views/URI + ETC----------------------------------------------
    /** Identifier for the inventories data loader */
    private static final int EXISTING_PET_LOADER = 0;

    /** Content URI for the existing inventory (null if it's a new inventory) */
    private Uri mCurrentProductUri;

    /** EditText field to enter the product's name */
    private EditText mNameEditText;

    /** EditText field to enter the product's price */
    private EditText mPriceEditText;

    /** EditText field to enter the product's supplier */
    private EditText mSupplierEditText;

    /** EditText field to enter the product's quantity */
    private EditText mQuantityEditText;

    private TextView mAddQuantityButton;

    private TextView mSubQuantityButton;

    private TextView mOrderShipmentButton;

    private TextView mChooseImageButton;

    private TextView mQuantityTextView;

    private EditText mShipmentQuantity;

    private ImageView mProductImage;

    private static final int SEND_MAIL_REQUEST = 1;
    private static final int PICK_IMAGE_REQUEST = 1;

    private Uri mUri = Uri.parse("");
    String photoUri = mUri.toString();

    /** Boolean flag that keeps track of whether the product has been edited (true) or not (false) */
    private boolean mProductHasChanged = false;

//-------------------Checks if any EditText has been clicked/touched--------------------------------
    /**
     * OnTouchListener that listens for any user touches on a View, implying that they are modifying
     * the view, and we change the mProductHasChanged boolean to true.
     */
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mProductHasChanged = true;
            return false;
        }
    };
//----------------------------------onCreate Starts-------------------------------------------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        // Examine the intent that was used to launch this activity,
        // in order to figure out if we're creating a new product or editing an existing one.
        Intent intent = getIntent();
        mCurrentProductUri = intent.getData();

        // If the intent DOES NOT contain a product content URI, then we know that we are
        // creating a new inventory.
        // This will happen when you click on the FAB button since it will not contain a URI
        if (mCurrentProductUri == null) {
            // This is a new inventory, so change the app bar to say "Add a product"
            setTitle(getString(R.string.editor_activity_title_new_product));

            // Invalidate the options menu, so the "Delete" menu option can be hidden.
            // (It doesn't make sense to delete a product that hasn't been created yet.)
            invalidateOptionsMenu();
        } else {
            // Otherwise this is an existing product, so change app bar to say "Edit the product"
            setTitle(getString(R.string.editor_activity_title_edit_product));

            // Initialize a loader to read the product data from the database
            // and display the current values in the editor
            getLoaderManager().initLoader(EXISTING_PET_LOADER, null, this);
        }

        // Find all relevant views that we will need to read user input from
        mProductImage = (ImageView) findViewById(R.id.itemImage);
        mNameEditText = (EditText) findViewById(R.id.inputName);
        mPriceEditText = (EditText) findViewById(R.id.inputPrice);
        mSupplierEditText = (EditText) findViewById(R.id.inputSupplier);
        mQuantityEditText = (EditText) findViewById(R.id.inputQuantity);
        mShipmentQuantity = (EditText) findViewById(R.id.inputShipmentQuantity);

        mQuantityTextView = (TextView) findViewById(R.id.quantity_text_view);
        mChooseImageButton = (TextView) findViewById(R.id.chooseImage);
        mAddQuantityButton = (TextView) findViewById(R.id.addQuantity);
        mSubQuantityButton = (TextView) findViewById(R.id.subQuantity);
        mOrderShipmentButton = (TextView) findViewById(R.id.orderShipment);

        // Setup OnTouchListeners on all the input fields, so we can determine if the user
        // has touched or modified them. This will let us know if there are unsaved changes
        // or not, if the user tries to leave the editor without saving.
        mNameEditText.setOnTouchListener(mTouchListener);
        mPriceEditText.setOnTouchListener(mTouchListener);
        mSupplierEditText.setOnTouchListener(mTouchListener);
        mQuantityEditText.setOnTouchListener(mTouchListener);
        mChooseImageButton.setOnTouchListener(mTouchListener);

//--------------------------------ADD QUANTITY BUTTON-----------------------------------------------
        mAddQuantityButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                try{
                    inputQuantity = Integer.parseInt(mQuantityEditText.getText().toString());
                    quantity = quantity + inputQuantity;
                    displayQuantity(quantity);
                }catch (NumberFormatException e){
                    mQuantityEditText.setError("Add Quantity");
                }
            }
        });
//--------------------------------SUB QUANTITY BUTTON-----------------------------------------------
        mSubQuantityButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                try{
                    inputQuantity = Integer.parseInt(mQuantityEditText.getText().toString());
                    quantity = quantity - inputQuantity;
                    if (quantity < 0){
                        quantity = quantity + inputQuantity;
                        Toast.makeText(EditorActivity.this, "Quantity cannot be less than 0", Toast.LENGTH_SHORT).show();
                    }else{
                        displayQuantity(quantity);
                    }
                }catch (NumberFormatException e){
                    mQuantityEditText.setError("Add Quantity");
                }
            }
        });
//------------------------------ORDER SHIPMENT BUTTON-----------------------------------------------
        mOrderShipmentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String priceInput = mPriceEditText.getText().toString();
                String nameInput = mNameEditText.getText().toString();
                String supplierInput = mSupplierEditText.getText().toString();
                String shipmentQuantityInput = mShipmentQuantity.getText().toString();
                String uriString = mUri.toString().trim();


                if(nameInput.matches("")){
                    mNameEditText.setError("Add Name");
                }
                if (priceInput.matches("")){
                    mPriceEditText.setError("Add Price");
                }
                if (supplierInput.matches("")) {
                    mSupplierEditText.setError("Add Supplier");
                }
                if (shipmentQuantityInput.matches("")){
                    mShipmentQuantity.setError("Add Quantity");
                }

                if (uriString.matches("")){
                    Snackbar.make(mChooseImageButton, "Image not selected", Snackbar.LENGTH_LONG)
                            .setAction("Select", new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    imageCode();
                                }
                            }).show();
                }

                if(TextUtils.isEmpty(priceInput) || TextUtils.isEmpty(nameInput) || TextUtils.isEmpty(supplierInput) || TextUtils.isEmpty(shipmentQuantityInput)) {
                    Toast.makeText(EditorActivity.this, "Please input all item information", Toast.LENGTH_SHORT).show();
                }else{
                    calculatePrice = Integer.parseInt(shipmentQuantityInput) * Float.parseFloat(mPriceEditText.getText().toString());
                    Intent intent = new Intent(Intent.ACTION_SENDTO);
                    intent.setData(Uri.parse("mailto:")); // only email apps should handle this
                    intent.putExtra(Intent.EXTRA_SUBJECT, "Purchase Shipment for this Item :" + nameInput);
                    intent.putExtra(Intent.EXTRA_TEXT, createOrderSummary(mNameEditText, mShipmentQuantity, calculatePrice));
                    if (intent.resolveActivity(getPackageManager()) != null) {
                        startActivity(intent);
                    }
                }
            }
        });
//----------------------------------onCreate ENDS---------------------------------------------------

//-----------------------------ON CLICK IMAGE BUTTON------------------------------------------------
        mChooseImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageCode();
            }
        });
//-------------CATCHES/CHECKS REQUEST CODE TO SEE IF INTENT WAS REALLY OPENED-----------------------
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        // The ACTION_OPEN_DOCUMENT intent was sent with the request code READ_REQUEST_CODE.
        // If the request code seen here doesn't match, it's the response to some other intent,
        // and the below code shouldn't run at all.

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            // The document selected by the user won't be returned in the intent.
            // Instead, a URI to that document will be contained in the return intent
            // provided to this method as a parameter.  Pull that uri using "resultData.getData()"

            if (resultData != null) {
                mUri = resultData.getData();
                Log.i(LOG_TAG, "Uri: " + mUri.toString());

                photoUri = mUri.toString();

//                mTextView.setText(mUri.toString());
                mProductImage.setImageBitmap(getBitmapFromUri(mUri));
            }
        } else if (requestCode == SEND_MAIL_REQUEST && resultCode == Activity.RESULT_OK) {

        }
    }
//-------------CATCHES/CHECKS REQUEST CODE TO SEE IF INTENT WAS REALLY OPENED-----------------------
    public Bitmap getBitmapFromUri(Uri uri) {

        if (uri == null || uri.toString().isEmpty())
            return null;

        // Get the dimensions of the View
        int targetW = mProductImage.getWidth();
        int targetH = mProductImage.getHeight();

        InputStream input = null;
        try {
            input = this.getContentResolver().openInputStream(uri);

            // Get the dimensions of the bitmap
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            bmOptions.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(input, null, bmOptions);
            input.close();

            int photoW = bmOptions.outWidth;
            int photoH = bmOptions.outHeight;

            // Determine how much to scale down the image
            int scaleFactor = Math.min(photoW / targetW, photoH / targetH);

            // Decode the image file into a Bitmap sized to fill the View
            bmOptions.inJustDecodeBounds = false;
            bmOptions.inSampleSize = scaleFactor;
            bmOptions.inPurgeable = true;

            input = this.getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(input, null, bmOptions);
            input.close();
            return bitmap;

        } catch (FileNotFoundException fne) {
            Log.e(LOG_TAG, "Failed to load image.", fne);
            return null;
        } catch (Exception e) {
            Log.e(LOG_TAG, "Failed to load image.", e);
            return null;
        } finally {
            try {
                input.close();
            } catch (IOException ioe) {

            }
        }
    }
//-----------------------------INTENT TO CHOOSE IMAGE-----------------------------------------------
    private void imageCode(){
        Intent intent;

        if (Build.VERSION.SDK_INT < 19) {
            intent = new Intent(Intent.ACTION_GET_CONTENT);
        } else {
            intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
        }

        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

//-----------------------------ORDER SHIPMENT INTENT------------------------------------------------
    private String createOrderSummary(EditText name, EditText quantity, float calculatePrice){
        String intentMessage = "Item Name:" + name.getText().toString();
        intentMessage += "\nQuantity: " + quantity.getText().toString();
        intentMessage = intentMessage + "\nTotal Cost: $" + calculatePrice;
        intentMessage = intentMessage + "\nThank You!";
        return intentMessage;
    }
//-----------------------------ADD TO QUANTITY BUTTON-----------------------------------------------
    public void increment (View view){
        if (quantity == 100){
            // Gives error message to user
            Toast.makeText(EditorActivity.this, "You can not have more than 100 items", Toast.LENGTH_SHORT).show();
            // Exits method since quantity should stay at 100
            return;
        }
        quantity = quantity +1;
        displayQuantity(quantity);
    }
//-----------------------------SUBTRACT TO QUANTITY BUTTON------------------------------------------
    public void decrement (View view) {
        if (quantity == 0){
            // Gives error message to user
            Toast.makeText(EditorActivity.this, "You can not have less than 0 items", Toast.LENGTH_SHORT).show();
            // Exits method since quantity should stay at 1
            return;
        }
        quantity = quantity - 1;
        displayQuantity(quantity);
    }
//---------------------------DISPLAY NEW QUANTITY METHOD--------------------------------------------
    private void displayQuantity(int number) {
        mQuantityTextView.setText("" + number);
    }

//---------------------------CREATE MENU ITEM-------------------------------------------------------
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    /**
     * This method is called after invalidateOptionsMenu(), so that the
     * menu can be updated (some menu items can be hidden or made visible).
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new inventory, hide the "Delete" menu item.
        if (mCurrentProductUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                // Save item to database
                saveInventory();
                // Exit activity
//                finish();
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                // Pop up confirmation dialog for deletion
                showDeleteConfirmationDialog();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // If the item hasn't changed, continue with navigating up to parent activity
                // which is the {@link MainActivity}.
                if (!mProductHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }

                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };

                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
//---------------------------CREATE MENU ITEM ENDS--------------------------------------------------


//-------------------------------SAVE INVENTORY-----------------------------------------------------
    /**
     * Get user input from editor and save pet into database.
     */
    private void saveInventory() {
        // Read from input fields
        // Use trim to eliminate leading or trailing white space
        String nameString = mNameEditText.getText().toString().trim();
        String priceString = mPriceEditText.getText().toString().trim();
        String supplierString = mSupplierEditText.getText().toString().trim();
        String quantityString = mQuantityTextView.getText().toString().trim();
        String uriString = mUri.toString().trim();


        // Check if this is supposed to be a new pet
        // and check if all the fields in the editor are blank
        // This code returns the code without creating a list view if nothing is entered
        if (mCurrentProductUri == null &&
                TextUtils.isEmpty(nameString) && TextUtils.isEmpty(priceString) &&
                TextUtils.isEmpty(supplierString) && uriString.matches("") && quantityString.matches("0")) {
            // Since no fields were modified, we can return early without creating a new pet.
            // No need to create ContentValues and no need to do any ContentProvider operations.
            finish();
            return;
        }


        if (uriString.matches("")){
            Snackbar.make(mChooseImageButton, "Image not selected", Snackbar.LENGTH_LONG)
                    .setAction("Select", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent;

                            if (Build.VERSION.SDK_INT < 19) {
                                intent = new Intent(Intent.ACTION_GET_CONTENT);
                            } else {
                                intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                                intent.addCategory(Intent.CATEGORY_OPENABLE);
                            }

                            intent.setType("image/*");
                            startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
                        }
                    }).show();
        }

        if (nameString.matches("")){
            mNameEditText.setError("Add Name");
        }
        if (quantityString.matches("0")){
            Toast.makeText(EditorActivity.this, "Quantity Cannot be Zero", Toast.LENGTH_SHORT).show();
        }
        if(priceString.matches("")){
            mPriceEditText.setError("Add Price");
        }
        if (supplierString.matches("")) {
            mSupplierEditText.setError("Add Supplier");
        }

        if(TextUtils.isEmpty(nameString) || TextUtils.isEmpty(priceString) || TextUtils.isEmpty(supplierString) || uriString.matches("") || quantityString.matches("0")) {
//            Toast.makeText(EditorActivity.this, "Please input all item information", Toast.LENGTH_SHORT).show();
        }else {

            // Create a ContentValues object where column names are the keys,
            // and pet attributes from the editor are the values.
            ContentValues values = new ContentValues();
            values.put(InventoryEntry.COLUMN_INVENTORY_NAME, nameString);
//        values.put(InventoryEntry.COLUMN_INVENTORY_PRICE, priceString);
            values.put(InventoryEntry.COLUMN_INVENTORY_SUPPLIER, supplierString);
            // If the weight is not provided by the user, don't try to parse the string into an
            // integer value. Use 0 by default.
            // This also helps the app not crash because it cannot convert a blank space into an integer

            float decimalPrice = Float.parseFloat(priceString);

            values.put(InventoryEntry.COLUMN_INVENTORY_PRICE, decimalPrice);
            values.put(InventoryEntry.COLUMN_INVENTORY_QUANTITY, quantityString);
            values.put(InventoryEntry.COLUMN_INVENTORY_PHOTO, uriString);

            // Determine if this is a new or existing pet by checking if mCurrentPetUri is null or not
            if (mCurrentProductUri == null) {
                // This is a NEW pet, so insert a new pet into the provider,
                // returning the content URI for the new pet.
                Uri newUri = getContentResolver().insert(InventoryEntry.CONTENT_URI, values);

                // Show a toast message depending on whether or not the insertion was successful.
                if (newUri == null) {
                    // If the new content URI is null, then there was an error with insertion.
                    Toast.makeText(this, getString(R.string.editor_insert_item_failed),
                            Toast.LENGTH_SHORT).show();
                } else {
                    // Otherwise, the insertion was successful and we can display a toast.
                    Toast.makeText(this, getString(R.string.editor_insert_item_successful),
                            Toast.LENGTH_SHORT).show();
                }
            } else {
                // Otherwise this is an EXISTING pet, so update the pet with content URI: mCurrentPetUri
                // and pass in the new ContentValues. Pass in null for the selection and selection args
                // because mCurrentPetUri will already identify the correct row in the database that
                // we want to modify.
                int rowsAffected = getContentResolver().update(mCurrentProductUri, values, null, null);

                // Show a toast message depending on whether or not the update was successful.
                if (rowsAffected == 0) {
                    // If no rows were affected, then there was an error with the update.
                    Toast.makeText(this, getString(R.string.editor_update_item_failed),
                            Toast.LENGTH_SHORT).show();
                } else {
                    // Otherwise, the update was successful and we can display a toast.
                    Toast.makeText(this, getString(R.string.editor_update_item_successful),
                            Toast.LENGTH_SHORT).show();
                }
            }

            finish();
        }
    }

//-----------------------------onBackPressed DialogInterface----------------------------------------
    /**
     * This method is called when the back button is pressed.
     */
    @Override
    public void onBackPressed() {
        // If the pet hasn't changed, continue with handling back button press
        if (!mProductHasChanged) {
            super.onBackPressed();
            return;
        }

        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }
//---------------------showUnsavedChangesDialog Method----------------------------------------------
    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
//---------------------showDeleteConfirmationDialog Method------------------------------------------
    /**
     * Prompt the user to confirm that they want to delete this pet.
     */
    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the pet.
                deletePet();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
//----------------------------------deletePet Method------------------------------------------------
    /**
     * Perform the deletion of the pet in the database.
     */
    private void deletePet() {
        // Only perform the delete if this is an existing pet.
        if (mCurrentProductUri != null) {
            // Call the ContentResolver to delete the pet at the given content URI.
            // Pass in null for the selection and selection args because the mCurrentPetUri
            // content URI already identifies the pet that we want.
            int rowsDeleted = getContentResolver().delete(mCurrentProductUri, null, null);

            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, getString(R.string.editor_delete_item_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_delete_item_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
        // Close the activity
        finish();
    }
//--------------------------------LOADER SET UP-----------------------------------------------------
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Since the editor shows all pet attributes, define a projection that contains
        // all columns from the pet table
        String[] projection = {
                InventoryEntry._ID,
                InventoryEntry.COLUMN_INVENTORY_NAME,
                InventoryEntry.COLUMN_INVENTORY_PRICE,
                InventoryEntry.COLUMN_INVENTORY_SUPPLIER,
                InventoryEntry.COLUMN_INVENTORY_QUANTITY,
                InventoryEntry.COLUMN_INVENTORY_PHOTO,
//                InventoryEntry.COLUMN_INVENTORY_SALES
        };

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,   // Parent activity context
                mCurrentProductUri,         // Query the content URI for the current pet
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null);                  // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Bail early if the cursor is null or there is less than 1 row in the cursor
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

            // Proceed with moving to the first row of the cursor and reading data from it
            // (This should be the only row in the cursor)
            if (cursor.moveToFirst()) {
                // Find the columns of pet attributes that we're interested in
                int nameColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_INVENTORY_NAME);
                int priceColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_INVENTORY_PRICE);
                int supplierColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_INVENTORY_SUPPLIER);
                int quantityColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_INVENTORY_QUANTITY);
                int photoColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_INVENTORY_PHOTO);

                // Extract out the value from the Cursor for the given column index
                String name = cursor.getString(nameColumnIndex);
                String supplier = cursor.getString(supplierColumnIndex);

                float price = cursor.getFloat(priceColumnIndex);
                int quantityNumber = cursor.getInt(quantityColumnIndex);
                String currentUri = cursor.getString(photoColumnIndex);


                // Update the views on the screen with the values from the database
                mNameEditText.setText(name);
                mSupplierEditText.setText(supplier);
                mPriceEditText.setText(Float.toString(price));
                mQuantityTextView.setText("" + quantityNumber);
                quantity = quantityNumber;
                photoUri = currentUri;
                mUri = Uri.parse(photoUri);
                mProductImage.setImageBitmap(getBitmapFromUri(mUri));
            }
        }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // If the loader is invalidated, clear out all the data from the input fields.
        mNameEditText.setText("");
        mPriceEditText.setText("");
        mSupplierEditText.setText("");
        mQuantityEditText.setText("");
        mShipmentQuantity.setText("");
    }
//-----------------------------LOADER SET UP FINISHED-----------------------------------------------
}
