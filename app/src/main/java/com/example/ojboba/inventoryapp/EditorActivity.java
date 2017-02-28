package com.example.ojboba.inventoryapp;

import android.Manifest;
import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by OjBoba on 2/22/2017.
 */

public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    /** Identifier for the inventories data loader */
    private static final int EXISTING_PET_LOADER = 0;

    int quantity = 1;
    int inputQuantity = 0;
    int calculatePrice = 0;
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

    private TextView mSalesButton;

    private TextView mOrderShipmentButton;

    private TextView mChooseImageButton;

    private TextView mQuantityTextView;

    private ImageView productImage;

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

        mChooseImageButton = (TextView) findViewById(R.id.chooseImage);
        mAddQuantityButton = (TextView) findViewById(R.id.addQuantity);
        mSubQuantityButton = (TextView) findViewById(R.id.subQuantity);
        mSalesButton = (TextView) findViewById(R.id.salesButton);
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
                if (mQuantityEditText != null){
                    inputQuantity = Integer.parseInt(mQuantityEditText.getText().toString());
                    quantity = quantity + inputQuantity;
                    displayQuantity(quantity);
                }
//                else{
//                    inputQuantity = Integer.parseInt(mQuantityEditText.getText().toString());
//                    quantity = quantity + inputQuantity;
//                    displayQuantity(quantity);
//                }

            }
        });


        mSubQuantityButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if (Integer.parseInt(mQuantityEditText.getText().toString()) == 0){
                    Toast.makeText(EditorActivity.this, "Please Input Quantity", Toast.LENGTH_SHORT).show();
//                    return;
                }
                else{
                    inputQuantity = Integer.parseInt(mQuantityEditText.getText().toString());
                    quantity = quantity - inputQuantity;
                    if (quantity < 0){
                        quantity = quantity + inputQuantity;
                        Toast.makeText(EditorActivity.this, "Quantity cannot be less than 0", Toast.LENGTH_SHORT).show();
                    }else{
                        displayQuantity(quantity);
                    }
                }

            }
        });


        mOrderShipmentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPriceEditText != null && mNameEditText != null && mPriceEditText != null) {
                    calculatePrice = quantity * Integer.parseInt(mPriceEditText.getText().toString());
                    Intent intent = new Intent(Intent.ACTION_SENDTO);
                    intent.setData(Uri.parse("mailto:")); // only email apps should handle this
                    intent.putExtra(Intent.EXTRA_SUBJECT, "Purchase Shipment for :" + mNameEditText);
                    intent.putExtra(Intent.EXTRA_TEXT, createOrderSummary(mNameEditText, quantity, calculatePrice));
                    if (intent.resolveActivity(getPackageManager()) != null) {
                        startActivity(intent);
                    }
                } else {
                    return;
                }
            }
        });

        mChooseImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                dispatchTakePictureIntent();
                ActivityCompat.requestPermissions(
                        EditorActivity.this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        REQUEST_CODE_GALLERY
                );
            }
        });



    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if(requestCode == REQUEST_CODE_GALLERY){
            if(grantResults.length >0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, REQUEST_CODE_GALLERY);
            }
            else {
                Toast.makeText(getApplicationContext(), "You don't have permission to access file location!", Toast.LENGTH_SHORT).show();
            }
            return;
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            productImage.setImageBitmap(imageBitmap);
        }

//        if(requestCode == REQUEST_CODE_GALLERY && resultCode == RESULT_OK && data != null){
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



    private String createOrderSummary(EditText mNameEditText, int quantity, int calculatePrice){
        String intentMessage = "Name:" + mNameEditText;
        intentMessage += "\nQuantity: " + quantity;
        intentMessage = intentMessage + "\nTotal: $" + calculatePrice;
        intentMessage = intentMessage + "\nThank You!";
        return intentMessage;
    }

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

    private void displayQuantity(int number) {
        mQuantityTextView = (TextView) findViewById(R.id.quantity_text_view);
        mQuantityTextView.setText("" + number);
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }


}
