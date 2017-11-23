package com.lizarda.lizarda.ui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.PopupMenu;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.lizarda.lizarda.R;
import com.lizarda.lizarda.model.Product;
import com.lizarda.lizarda.model.User;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.lizarda.lizarda.Const.FIREBASE.CHILD_PRODUCT;
import static com.lizarda.lizarda.Const.FIREBASE.CHILD_USER;
import static com.lizarda.lizarda.Const.NOT_SET;
import static com.lizarda.lizarda.Const.TAG.SPINNER_KATEGORI;
import static com.lizarda.lizarda.Const.TAG.URI;

public class AddProductActivity extends AppCompatActivity implements View.OnClickListener {

    // MARK: - Properties
    @BindView(R.id.iv_product_add_product)
    ImageView mIvProduct;

    @BindView(R.id.btn_browse_add_product)
    Button mBtnBrowse;

    @BindView(R.id.et_nama_add_product)
    EditText mEtNamaProduct;

    @BindView(R.id.spinner_jenis_add_product)
    Spinner mSpinnerJenisProduct;

    @BindView(R.id.et_description_add_product)
    EditText mEtDescriptionProduct;

    @BindView(R.id.et_harga_add_product)
    EditText mEtHargaProduct;

    @BindView(R.id.btn_input_add_product)
    Button mBtnInputProduct;

    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private FirebaseDatabase mDatabase;
    private DatabaseReference mDatabaseRef;
    private StorageReference mStorageRef;

    public static final int REQUEST_PICK_IMAGE = 1;
    public static final int REQUEST_IMAGE_CAPTURE = 2;
    private String mCurrentPhotoPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_product);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        ButterKnife.bind(this);

        mBtnBrowse.setOnClickListener(this);
        mBtnInputProduct.setOnClickListener(this);

        setupFirebase();
        // userIdFromDatabase();

        mSpinnerJenisProduct.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.d(SPINNER_KATEGORI, "onItemSelected: kategori : " + mSpinnerJenisProduct.getSelectedItem().toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    // MARK: - Views
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_browse_add_product:
                showPopupMenuPhoto();
                break;
            case R.id.btn_input_add_product:
                if (isInputEmpty()) {
                    Toast.makeText(this, "Mohon cek input Anda.", Toast.LENGTH_SHORT).show();
                } else {
                    writeToNodeProduct();
                }
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showPopupMenuPhoto() {
        PopupMenu popup = new PopupMenu(AddProductActivity.this, mBtnBrowse);
        //Inflating the Popup using xml file
        popup.getMenuInflater().inflate(R.menu.image_popup_menu, popup.getMenu());
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.popup_open_gallery) {
                    presentImagePicker();
                }
                if (id == R.id.popup_open_camera) {
                    dispatchTakePictureIntent();
                }
                return true;
            }
        });
        popup.show();
    }

    private void presentImagePicker() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), REQUEST_PICK_IMAGE);
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_PICK_IMAGE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri uri = data.getData();
            Log.d(URI, "onActivityResult: uri.toString() : " + uri.toString());
            Log.d(URI, "onActivityResult: uri.getPath() : " + uri.getPath());

            // update UI
            Picasso.with(this).load(uri).into(mIvProduct);

            // TODO: 11/23/17  nanti file path di pake di firebase storage
            // ...
        }

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            // update UI
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            mIvProduct.setImageBitmap(imageBitmap);
            try {
                File productImageFile = createImageFile();
                Uri productImageUri = Uri.fromFile(productImageFile);
                Picasso.with(this).load(productImageUri).resize(300, 300).into(mIvProduct);

                Log.d(URI, "onActivityResult: productImageUri.toString() : " + productImageUri.toString());
                Log.d(URI, "onActivityResult: productImageUri.getPath() : " + productImageUri.getPath());
                Log.d(URI, "onActivityResult: mCurrentPhotoPath : " + mCurrentPhotoPath);
                // sudah berhasil dapetin absolutPath
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    // MARK: - Model & Logic
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

    private void writeToNodeProduct() {
        String hargaStr = mEtHargaProduct.getText().toString();
        double harga = Double.parseDouble(hargaStr);

        String productId = mDatabaseRef.push().getKey();
        Product product = new Product(
                productId,
                mEtNamaProduct.getText().toString(),
                mEtDescriptionProduct.getText().toString(),
                NOT_SET,
                false,
                harga,
                mSpinnerJenisProduct.getSelectedItem().toString(),
                ""
        );
        mDatabaseRef.child(CHILD_PRODUCT).child(productId).setValue(product)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(
                                AddProductActivity.this,
                                "Sukses Tambah Produk Anda.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(
                                AddProductActivity.this,
                                "Terjadi kesalahan. Gagal tambah product Anda.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private boolean isInputEmpty() {
        String name = mEtNamaProduct.getText().toString();
        String jenis = mSpinnerJenisProduct.getSelectedItem().toString();
        String description = mEtDescriptionProduct.getText().toString();
        String hargaStr = mEtHargaProduct.getText().toString();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(jenis) || TextUtils.isEmpty(description)
                || TextUtils.isEmpty(hargaStr)) {
            return true;
        } else {
            return false;
        }
    }

    private void setupFirebase() {
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance();
        mDatabaseRef = mDatabase.getReference();
        mStorageRef = FirebaseStorage.getInstance().getReference();
    }


    // MARK: - untuk generate database only! =======================================================
    private ArrayList<String> mUsersId = new ArrayList<>();

    private void userIdFromDatabase() {

        mDatabaseRef.child(CHILD_USER).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Log.e("Count", "" + dataSnapshot.getChildrenCount());
                    for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                        User user = userSnapshot.getValue(User.class);
                        mUsersId.add(user.getId());
                        Log.d("Count", "onDataChange: " + user.getId());
                    }
                    writeManyProduct();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void writeManyProduct() {

        String[] names = {
                "Ular padang pasir",
                "Iguana Hijau",
                "Bunglon hutan"
        };

        String[] desctiprions = {
                "Jual ular langka, umur 1 bulan, sehat.",
                "Jual Iguana Reptil langka, umur 1 tahun lincah.",
                "Bunglon peliharaan dari Makassar.",
        };

        String[] categories = {
                "Ular",
                "Iguana",
                "Bunglon"
        };

        for (int i = 0; i < 50; i++) {
            Random rn = new Random();
            double harga = rn.nextInt(5000000 - 200000 + 1) + 200000;

            int index = rn.nextInt(2 - 0) + 0;
            int indexUid = rn.nextInt(3 - 0) + 0;

            String productId = mDatabaseRef.push().getKey();
            Product product = new Product(
                    productId,
                    names[index],
                    desctiprions[index],
                    NOT_SET,
                    false,
                    harga,
                    categories[index],
                    mUsersId.get(indexUid)
            );

            mDatabaseRef.child(CHILD_PRODUCT).child(productId).setValue(product)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            // Toast.makeText(AddProductActivity.this, "Sukses Tambah Produk Anda.", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // Toast.makeText(AddProductActivity.this, "Terjadi kesalahan. Gagal tambah product Anda.", Toast.LENGTH_SHORT).show();
                        }
                    });
        }


    }

}
