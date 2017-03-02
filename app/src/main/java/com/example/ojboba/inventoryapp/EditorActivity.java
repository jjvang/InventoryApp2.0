package com.example.ojboba.inventoryapp;

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
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ojboba.inventoryapp.data.InventoryContract.InventoryEntry;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by OjBoba on 2/22/2017.
 */

public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    /** Identifier for the inventories data loader */
    private static final int EXISTING_PET_LOADER = 0;

    int quantity = 1;
    int inputQuantity = 0;
    float calculatePrice = 0;
    final int REQUEST_CODE_GALLERY = 999;

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

    private ImageView productImage;

    String mCurrentPhotoPath;

    static final int REQUEST_TAKE_PHOTO = 1;

    static final int REQUEST_IMAGE_CAPTURE = 1;

    /** Boolean flag that keeps track of whether the product has been edited (true) or not (false) */
    private boolean mProductHasChanged = false;

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
        productImage = (ImageView) findViewById(R.id.itemImage);
        mNameEditText = (EditText) findViewById(R.id.inputName);
        mPriceEditText = (EditText) findViewById(R.id.inputPrice);
        mSupplierEditText = (EditText) findViewById(R.id.inputSupplier);
        mQuantityEditText = (EditText) findViewById(R.id.inputQuantity);
        mShipmentQuantity = (EditText) findViewById(R.id.inputShipmentQuantity);

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

        mAddQuantityButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                try{
                    inputQuantity = Integer.parseInt(mQuantityEditText.getText().toString());
                    quantity = quantity + inputQuantity;
                    displayQuantity(quantity);
                }catch (NumberFormatException e){
                    mQuantityEditText.setError("Add Quantity");
//                    Toast.makeText(EditorActivity.this, "Please Input Quantity", Toast.LENGTH_SHORT).show();
                }

            }
        });

        // Unable to use a if/else statement because it errors saying that inputQuantity is null or empty
        // Unable to check if the string is empty, not sure why that is the actual case
        // THIS ACTUALLY WORKS, THE CODE BELOW
//        String quanString = mQuantityEditText.getText().toString();
//        if (quanString.matches("")){
//            Toast.makeText(EditorActivity.this, "Please Input Quantity", Toast.LENGTH_SHORT).show();
//        }
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
//                    Toast.makeText(EditorActivity.this, "Please Input Quantity", Toast.LENGTH_SHORT).show();
                }
//                String quanString = mQuantityEditText.getText().toString();
////                if (Integer.parseInt(mQuantityEditText.getText().toString()) == 0)
//                if (quanString.matches("")){
//                    Toast.makeText(EditorActivity.this, "Please Input Quantity", Toast.LENGTH_SHORT).show();
//                }
//                else{
//                    inputQuantity = Integer.parseInt(mQuantityEditText.getText().toString());
//                    quantity = quantity - inputQuantity;
//                    if (quantity < 0){
//                        quantity = quantity + inputQuantity;
//                        Toast.makeText(EditorActivity.this, "Quantity cannot be less than 0", Toast.LENGTH_SHORT).show();
//                    }else{
//                        displayQuantity(quantity);
//                    }
//                }

            }
        });


        mOrderShipmentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                try{
//                    calculatePrice = quantity * Integer.parseInt(mPriceEditText.getText().toString());
//                    Intent intent = new Intent(Intent.ACTION_SENDTO);
//                    intent.setData(Uri.parse("mailto:")); // only email apps should handle this
//                    intent.putExtra(Intent.EXTRA_SUBJECT, "Purchase Shipment for :" + mNameEditText);
//                    if (intent.resolveActivity(getPackageManager()) != null) {
//                        startActivity(intent);
//                    }
//                }catch (NumberFormatException e){
//                    Toast.makeText(EditorActivity.this, "Please input all item information", Toast.LENGTH_SHORT).show();
//                }catch (NullPointerException e){
//                    Toast.makeText(EditorActivity.this, "Please input all item information", Toast.LENGTH_SHORT).show();
//                }
//            }
                String priceInput = mPriceEditText.getText().toString();
                String nameInput = mNameEditText.getText().toString();
                String supplierInput = mSupplierEditText.getText().toString();
                String shipmentQuantityInput = mShipmentQuantity.getText().toString();

                if (priceInput.matches("")){
                    mPriceEditText.setError("Add Price");
                }
                if(nameInput.matches("")){
                    mNameEditText.setError("Add Name");
                }
                if (supplierInput.matches("")) {
                    mSupplierEditText.setError("Add Supplier");
                }
                if (shipmentQuantityInput.matches("")){
                    mShipmentQuantity.setError("Add Quantity");
                }
                if(TextUtils.isEmpty(priceInput) || TextUtils.isEmpty(nameInput) || TextUtils.isEmpty(supplierInput) || TextUtils.isEmpty(shipmentQuantityInput)) {
                    Toast.makeText(EditorActivity.this, "Please input all item information", Toast.LENGTH_SHORT).show();
                }else{
                    calculatePrice = Integer.parseInt(shipmentQuantityInput) * Integer.parseInt(mPriceEditText.getText().toString());
                    Intent intent = new Intent(Intent.ACTION_SENDTO);
                    intent.setData(Uri.parse("mailto:")); // only email apps should handle this
                    intent.putExtra(Intent.EXTRA_SUBJECT, "Purchase Shipment for :" + nameInput);
                    intent.putExtra(Intent.EXTRA_TEXT, createOrderSummary(mNameEditText, mShipmentQuantity, calculatePrice));
                    if (intent.resolveActivity(getPackageManager()) != null) {
                        startActivity(intent);
                    }
                }
            }
        });

//-----------------------------ON CLICK IMAGE BUTTON------------------------------------------------
        mChooseImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchTakePictureIntent();
//                ActivityCompat.requestPermissions(
//                        EditorActivity.this,
//                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
//                        REQUEST_CODE_GALLERY
//                );
            }
        });

    }

//-----------------------------TAKE PHOTO WITH THE CAMERA APP---------------------------------------

    public void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }
//------------------------ADVANCE WAY TO TAKE PHOTO WITH THE CAMERA APP-----------------------------
//    private void dispatchTakePictureIntent() {
//        // MediaStore is database to where images are stored and linked, a type of content provider
//        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//        // Ensure that there's a camera activity to handle the intent
//        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
//            // Create the File where the photo should go
//            File photoFile = null;
//            try {
//                photoFile = createImageFile();
//            } catch (IOException ex) {
//                // Error occurred while creating the Fi
//                Toast.makeText(getApplicationContext(), "Something Wrong While Taking Photos", Toast.LENGTH_SHORT).show();
//            }
//            // Continue only if the File was successfully created
//            if (photoFile != null) {
//                Uri photoURI = FileProvider.getUriForFile(this,
//                        "com.example.android.fileprovider",
//                        photoFile);
//                // photo is to be stored at the photoURI
//                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI).putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
//                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
//            }
//        }
//    }

    //---------------------------SAVE PHOTO WITH UNIQUE TIME STAMP--------------------------------------
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }
    //----------------------Retrieves data and displays it to an ImageView------------------------------
    // This override method is the results you get from the camera when you take the photo
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK && data != null) {
            // Bundle returns the value associated with the given key, in this case the image Intent URI
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            productImage.setImageBitmap(imageBitmap);
        }

//        else if(requestCode == REQUEST_CODE_GALLERY && resultCode == RESULT_OK && data != null){
//            Uri uri = data.getData();
//
//            try {
//                InputStream inputStream = getContentResolver().openInputStream(uri);
//
//                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
//                productImage.setImageBitmap(bitmap);
//
//            } catch (FileNotFoundException e) {
//                e.printStackTrace();
//            }
//        }
//
//        super.onActivityResult(requestCode, resultCode, data);
    }
//-----------------------------ADD THE PHOTO TO A GALLERY-------------------------------------------
//    The following example method demonstrates how to invoke the system's media scanner to add your
//    photo to the Media Provider's database, making it available in the Android Gallery application
//    and to other apps.

    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(mCurrentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }

    //-----------------------------DECODE A SCALED IMAGE------------------------------------------------
    private void setPic() {
        // Get the dimensions of the View
        int targetW = productImage.getWidth();
        int targetH = productImage.getHeight();

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = Math.min(photoW/targetW, photoH/targetH);

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        productImage.setImageBitmap(bitmap);
    }
//-----------------------------ORDER SHIPMENT INTENT------------------------------------------------
    private String createOrderSummary(EditText name, EditText quantity, float calculatePrice){
        String intentMessage = "Name:" + name.getText().toString();
        intentMessage += "\nQuantity: " + quantity.getText().toString();
        intentMessage = intentMessage + "\nTotal: $" + calculatePrice;
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
        mQuantityTextView = (TextView) findViewById(R.id.quantity_text_view);
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
                finish();
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

    /**
     * Get user input from editor and save pet into database.
     */
    private void saveInventory() {
        // Read from input fields
        // Use trim to eliminate leading or trailing white space
        String nameString = mNameEditText.getText().toString().trim();
        String priceString = mPriceEditText.getText().toString().trim();
        String supplierString = mSupplierEditText.getText().toString().trim();

        // Check if this is supposed to be a new pet
        // and check if all the fields in the editor are blank
        // This code returns the code without creating a list view if nothing is entered
        if (mCurrentProductUri == null &&
                TextUtils.isEmpty(nameString) && TextUtils.isEmpty(priceString) &&
                TextUtils.isEmpty(supplierString)) {
            // Since no fields were modified, we can return early without creating a new pet.
            // No need to create ContentValues and no need to do any ContentProvider operations.
            return;
        }

        // Create a ContentValues object where column names are the keys,
        // and pet attributes from the editor are the values.
        ContentValues values = new ContentValues();
        values.put(InventoryEntry.COLUMN_INVENTORY_NAME, nameString);
//        values.put(InventoryEntry.COLUMN_INVENTORY_PRICE, priceString);
        values.put(InventoryEntry.COLUMN_INVENTORY_SUPPLIER, supplierString);
        // If the weight is not provided by the user, don't try to parse the string into an
        // integer value. Use 0 by default.
        // This also helps the app not crash because it cannot convert a blank space into an integer
        int defaultPrice = 0;
//        float defaultPrice = 0;
        if (!TextUtils.isEmpty(priceString)) {
            defaultPrice = Integer.parseInt(priceString);
        }
        values.put(InventoryEntry.COLUMN_INVENTORY_PRICE, defaultPrice);

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
    }

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


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Since the editor shows all pet attributes, define a projection that contains
        // all columns from the pet table
        String[] projection = {
                InventoryEntry._ID,
                InventoryEntry.COLUMN_INVENTORY_NAME,
                InventoryEntry.COLUMN_INVENTORY_PRICE,
                InventoryEntry.COLUMN_INVENTORY_SUPPLIER,
                InventoryEntry.COLUMN_INVENTORY_QUANTITY};

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

                // Extract out the value from the Cursor for the given column index
                String name = cursor.getString(nameColumnIndex);
                String supplier = cursor.getString(supplierColumnIndex);
                int price = cursor.getInt(priceColumnIndex);
//                float price = cursor.getFloat(priceColumnIndex);
                int quantity = cursor.getInt(quantityColumnIndex);

                // Update the views on the screen with the values from the database
                mNameEditText.setText(name);
                mSupplierEditText.setText(supplier);
                mPriceEditText.setText("" + price);
//                mQuantityTextView.setText("" + quantity);

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
//        mQuantityTextView.setText("" + 1);
    }


}
