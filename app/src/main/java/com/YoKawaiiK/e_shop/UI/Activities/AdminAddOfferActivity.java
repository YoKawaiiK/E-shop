package com.YoKawaiiK.e_shop.UI.Activities;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.YoKawaiiK.e_shop.Model.Offer;
import com.YoKawaiiK.e_shop.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;


// Экран добавления предложения
public class AdminAddOfferActivity extends AppCompatActivity {
    private TextInputEditText name, description;
    private Button add, choose;
    private ImageView img;
    private Uri imgUri;
    private StorageReference mStorageRef;
    private StorageTask mUploadTask;
    private TextInputLayout nameTextInputLayout, descTextInputLayout;
    private Toolbar mToolBar;
    private RelativeLayout CustomCartContainer;
    private TextView PageTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_offer);

        //tool bar
        mToolBar = (Toolbar) findViewById(R.id.AddOffer_ToolBar);
        setSupportActionBar(mToolBar);
        getSupportActionBar().setTitle(R.string.admin_add_offer_activity__add_offer);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        name = findViewById(R.id.editTextOfferName);
        description = findViewById(R.id.editTextOfferDescription);
        add = findViewById(R.id.btnAddOffer);
        choose = findViewById(R.id.btnChooseOfferImg);
        img = findViewById(R.id.offerImage);
        nameTextInputLayout = findViewById(R.id.editTextOfferLayout);
        descTextInputLayout = findViewById(R.id.editTextOfferDescriptionLayout);

        mStorageRef = FirebaseStorage.getInstance().getReference("offers");

        nameTextInputLayout.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (name.getText().toString().trim().isEmpty()) {
                    nameTextInputLayout.setErrorEnabled(true);
                    nameTextInputLayout.setError(getString(R.string.error__please_enter_offer_name));
                } else {
                    nameTextInputLayout.setErrorEnabled(false);
                }
            }
        });

        descTextInputLayout.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (description.getText().toString().trim().isEmpty()) {
                    descTextInputLayout.setErrorEnabled(true);

                    descTextInputLayout.setError(getString(R.string.error__please_enter_offer_name));
                } else {
                    descTextInputLayout.setErrorEnabled(false);
                }
            }
        });

        //  Для валидации
        name.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (name.getText().toString().trim().isEmpty()) {
                    nameTextInputLayout.setErrorEnabled(true);
                    nameTextInputLayout.setError(getString(R.string.error__please_enter_offer_name));
                } else {
                    nameTextInputLayout.setErrorEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        //  Для валидации
        description.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (description.getText().toString().trim().isEmpty()) {
                    descTextInputLayout.setErrorEnabled(true);
                    descTextInputLayout.setError(getString(R.string.error__please_enter_offer_name));
                } else {
                    descTextInputLayout.setErrorEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        // Добавить
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mUploadTask != null && mUploadTask.isInProgress())
                    Toast.makeText(AdminAddOfferActivity.this, getString(R.string.admin_add_offer_activity__upload_is_in_progress), Toast.LENGTH_SHORT).show();
                else if (name.getText().toString().isEmpty() || description.getText().toString().isEmpty() || imgUri == null) {
                    Toast.makeText(AdminAddOfferActivity.this, getString(R.string.admin_add_offer_activity__empty_cells), Toast.LENGTH_SHORT).show();
                } else {
                    try {
                        uploadData();
                        Toast.makeText(AdminAddOfferActivity.this, getString(R.string.admin_add_offer_activity__added_successfully), Toast.LENGTH_SHORT).show();
                        finish();
                    } catch (Exception e) {
                        Toast.makeText(AdminAddOfferActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }

            }
        });

        //  Выбор изображения
        choose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openImage();
            }
        });

    }


    @Override
    protected void onStart() {
        super.onStart();

        NotshowCartIcon();
    }

    // Отображение тоста
    public void uploadData() {
        if (name.getText().toString().isEmpty() || description.getText().toString().isEmpty() || imgUri == null) {
            Toast.makeText(AdminAddOfferActivity.this, getString(R.string.admin_add_offer_activity__empty_cells), Toast.LENGTH_SHORT).show();
        } else {
            uploadImage();
        }
    }

    //  Зарузка изображения
    public void uploadImage() {
        if (imgUri != null) {
            StorageReference fileReference = mStorageRef.child(name.getText().toString() + "." + getFileExtension(imgUri));
            mUploadTask = fileReference.putFile(imgUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Task<Uri> urlTask = taskSnapshot.getStorage().getDownloadUrl();
                    while (!urlTask.isSuccessful()) ;
                    Uri downloadUrl = urlTask.getResult();
                    Offer offer = new Offer(description.getText().toString().trim(),
                            downloadUrl.toString());
                    DatabaseReference z = FirebaseDatabase.getInstance().getReference("offers");
                    z.child(name.getText().toString().trim()).setValue(offer);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                }
            });
        }
    }

    // Открыть изображение
    public void openImage() {
        Intent i = new Intent();
        i.setType("image/*");
        i.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(i, SignUpActivity.GALARY_PICK);
    }

    // Расширение файла
    public String getFileExtension(Uri uri) {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    // Получение изображения при выборе в галерее
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SignUpActivity.GALARY_PICK && resultCode == Activity.RESULT_OK && data != null) {
            imgUri = data.getData();
            Log.e("uri", imgUri.toString());
            try {
                Picasso.get().load(imgUri).fit().centerCrop().into(img);
            } catch (Exception e) {
                Log.e(this.toString(), e.getMessage().toString());
            }
        }
    }

    // Спрятать иконку
    private void NotshowCartIcon() {
        //toolbar & cartIcon
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.buyer_toolbar, null);
        //actionBar.setCustomView(view);

        //************custom action items xml**********************
        CustomCartContainer = (RelativeLayout) findViewById(R.id.CustomCartIconContainer);
        PageTitle = (TextView) findViewById(R.id.PageTitle);
        PageTitle.setVisibility(View.GONE);
        CustomCartContainer.setVisibility(View.GONE);

    }
}