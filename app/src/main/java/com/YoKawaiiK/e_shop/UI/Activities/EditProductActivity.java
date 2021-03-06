package com.YoKawaiiK.e_shop.UI.Activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.YoKawaiiK.e_shop.Model.Product;
import com.YoKawaiiK.e_shop.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

public class EditProductActivity extends AppCompatActivity {
    private TextInputEditText name, quantity, price, expDate;
    private Button edit, choose;
    private ImageView img;
    private Uri imgUri;
    private String category, oldName, oldImagePath , oldCategory;
    private StorageReference mStorageRef;
    private DatabaseReference mDatabaseRef;
    private Spinner spinner;
    private StorageTask mUploadTask;
    private byte[] oldImageBytes;
    private Toolbar mToolBar;
    private RelativeLayout CustomCartContainer;
    private TextView PageTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_product);

        //tool bar
        mToolBar = (Toolbar)findViewById(R.id.EditProduct_ToolBar);
        setSupportActionBar(mToolBar);
        getSupportActionBar().setTitle(R.string.epaTitle);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        name = findViewById(R.id.editTextProductNameEdit);
        quantity = findViewById(R.id.editTextProductNumberEdit);
        edit = findViewById(R.id.btnAddEdit);
        choose = findViewById(R.id.btnChooseImgEdit);
        img = findViewById(R.id.imgProductEdit);
        price = findViewById(R.id.editTextProductPriceEdit);
        expDate = findViewById(R.id.editTextProductExpireEdit);
        spinner = findViewById(R.id.spinner);

        Bundle b = getIntent().getExtras();
        name.setText(b.getString(getString(R.string.epaViewTextName)));
        oldName = b.getString(getString(R.string.epaViewTextOldName));
        quantity.setText(b.getString(getString(R.string.epaViewTextQuantity)));
        Picasso.get().load(b.getString("img")).fit().centerCrop().into(img);
        price.setText(b.getString(getString(R.string.epaViewTextPrice)));
        expDate.setText(b.getString(getString(R.string.epaViewTextExpired)));
        imgUri = Uri.parse(b.getString("img"));
        oldImagePath = b.getString(getString(R.string.epaViewTextoldImagePath));
        oldCategory = b.getString(getString(R.string.epaViewTextOldCategory));

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.productstypes, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
        spinner.setAdapter(adapter);

        if (b.getString("category").equals("Fruits"))
            spinner.setSelection(0);
        else if (b.getString("category").equals("Vegetables"))
            spinner.setSelection(1);
        else if (b.getString("category").equals("Meats"))
            spinner.setSelection(2);
        else if (b.getString("category").equals("Electronics"))
            spinner.setSelection(3);


        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                category = adapterView.getItemAtPosition(i).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        StorageReference httpsRef = FirebaseStorage.getInstance().getReferenceFromUrl(oldImagePath);
        final long ONE_MEGABYTE = 1024 * 1024 * 10;
        httpsRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                oldImageBytes = bytes;
            }
        });

        mStorageRef = FirebaseStorage.getInstance().getReference("products");
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("product");


        choose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openImage();
            }
        });

        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mUploadTask != null && mUploadTask.isInProgress())
                    Toast.makeText(EditProductActivity.this, R.string.epaToastUploadIsInProgress, Toast.LENGTH_SHORT).show();
                else {
                    AlertDialog.Builder dialog = new AlertDialog.Builder(
                            EditProductActivity.this).setTitle(R.string.epaDialogTitleConfirmation)
                            .setMessage(R.string.epaDialogMessage)
                            .setPositiveButton(R.string.epaDialogPositiveButton, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            deleteImage();
                            deleteData();
                            uploadData();
                            finish();
                        }
                    }).setNegativeButton(R.string.epaDialogNegativeButton, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    }).setIcon(android.R.drawable.ic_dialog_alert);
                    dialog.show();
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        NotshowCartIcon();
    }

    public void deleteData() {
        DatabaseReference reference = mDatabaseRef.child(oldCategory).child(oldName);
        reference.removeValue();
    }

    public void deleteImage() {
        if (imgUri.toString().equals(oldImagePath)) {
            StorageReference x = mStorageRef.child(oldName);
            x.delete();
        }else
        {
            StorageReference z = mStorageRef.child(oldName + ".jpg");
            z.delete();
        }
    }

    public void uploadData() {
        if (name.getText().toString().isEmpty() || quantity.getText().toString().isEmpty() || price.getText().toString().isEmpty() || expDate.getText().toString().isEmpty() || imgUri == null) {
            Toast.makeText(EditProductActivity.this, "Empty Cells", Toast.LENGTH_SHORT).show();
        } else {
            uploadImage();
        }
    }

    public void uploadImage() {
        if (imgUri.toString().equals(oldImagePath)) {
            StorageReference fileReference = mStorageRef.child(name.getText().toString());
            mUploadTask = fileReference.putBytes(oldImageBytes).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Task<Uri> urlTask = taskSnapshot.getStorage().getDownloadUrl();
                    while (!urlTask.isSuccessful()) ;
                    Uri downloadUrl = urlTask.getResult();
                    Product product = new Product(quantity.getText().toString().trim(), price.getText().toString().trim(), expDate.getText().toString().trim(), downloadUrl.toString());
                    DatabaseReference z = mDatabaseRef.child(category);
                    z.child(name.getText().toString().trim()).setValue(product);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(EditProductActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else if (imgUri != null) {
            StorageReference fileReference = mStorageRef.child(name.getText().toString() + "." + getFileExtension(imgUri));
            mUploadTask = fileReference.putFile(imgUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    Task<Uri> urlTask = taskSnapshot.getStorage().getDownloadUrl();
                    while (!urlTask.isSuccessful()) ;
                    Uri downloadUrl = urlTask.getResult();
                    Product product = new Product(quantity.getText().toString().trim(),
                            price.getText().toString().trim(),
                            expDate.getText().toString().trim(),
                            downloadUrl.toString());
                    DatabaseReference z = FirebaseDatabase.getInstance().getReference()
                            .child("product")
                            .child(category)
                            .child(name.getText().toString());
                    z.setValue(product);
                    Toast.makeText(EditProductActivity.this, R.string.epaUploadImageToastUploadedSuccessfully, Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(EditProductActivity.this, e.getMessage().toString(), Toast.LENGTH_SHORT).show();
                }
            });

        }
    }

    public void openImage() {
        Intent i = new Intent();
        i.setType("image/*");
        i.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(i, SignUpActivity.GALARY_PICK);
    }

    public String getFileExtension(Uri uri) {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SignUpActivity.GALARY_PICK && resultCode == Activity.RESULT_OK && data.getData() != null && data != null) {
            imgUri = data.getData();

            try {
                Picasso.get().load(imgUri).fit().centerCrop().into(img);
            } catch (Exception e) {
                Log.e(this.toString(), e.getMessage().toString());
            }

        }
    }

    private void NotshowCartIcon(){
        //toolbar & cartIcon
        ActionBar actionBar= getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        LayoutInflater inflater = (LayoutInflater)this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view= inflater.inflate(R.layout.buyer_toolbar,null);
        //actionBar.setCustomView(view);

        //************custom action items xml**********************
        CustomCartContainer = (RelativeLayout)findViewById(R.id.CustomCartIconContainer);
        PageTitle =(TextView)findViewById(R.id.PageTitle);
        PageTitle.setVisibility(View.GONE);
        CustomCartContainer.setVisibility(View.GONE);

    }
}