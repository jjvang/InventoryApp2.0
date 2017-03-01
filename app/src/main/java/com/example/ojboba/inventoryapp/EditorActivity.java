package com.example.ojboba.inventoryapp;

import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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

    private void dispatchTakePictureIntent() {
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
    private String createOrderSummary(EditText mNameEditText, int quantity, int calculatePrice){
        String intentMessage = "Name:" + mNameEditText;
        intentMessage += "\nQuantity: " + quantity;
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
