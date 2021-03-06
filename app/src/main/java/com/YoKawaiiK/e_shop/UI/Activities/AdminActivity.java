package com.YoKawaiiK.e_shop.UI.Activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.YoKawaiiK.e_shop.R;
import com.YoKawaiiK.e_shop.UI.OffersFragment;
import com.YoKawaiiK.e_shop.UI.ProductsFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

// Экран базовый для админ-панели
public class AdminActivity extends AppCompatActivity {
    private Toolbar mToolBar;
    private BottomNavigationView bottomNavigationView;
    private TextView FragmentTitle;
    private FirebaseAuth mAuth;
    private RelativeLayout CustomCartContainer;
    private TextView PageTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);
        mAuth = FirebaseAuth.getInstance();

        //tool bar
        mToolBar = (Toolbar) findViewById(R.id.Admin_ToolBar);
        setSupportActionBar(mToolBar);
        getSupportActionBar().setTitle(R.string.admin_activity__admin_control);

        FragmentTitle = (TextView) findViewById(R.id.FragmentTitle);
        bottomNavigationView = (BottomNavigationView) findViewById(R.id.Bottom_view);
        bottomNavigationView.setOnNavigationItemSelectedListener(naveListener);

        // Фрагмент по умолчанию - это продукт (может войти в систему, чтобы перейти к фрагменту продуктов)
        getSupportFragmentManager().beginTransaction().replace(R.id.FrameLayout, new ProductsFragment()).commit();
        FragmentTitle.setText(R.string.admin_activity__all_products);


    }


    @Override
    protected void onStart() {
        super.onStart();

        NotshowCartIcon();
    }

    private BottomNavigationView.OnNavigationItemSelectedListener naveListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    Fragment SelectedFragment = null;
                    int id = item.getItemId();
                    if (id == R.id.ProductID) {
                        SelectedFragment = new ProductsFragment();
                        FragmentTitle.setText(R.string.admin_activity__all_products);
                    } else if (id == R.id.OffersID) {
                        SelectedFragment = new OffersFragment();
                        FragmentTitle.setText(R.string.admin_activity__all_offers);
                    }

                    getSupportFragmentManager().beginTransaction().replace(R.id.FrameLayout, SelectedFragment).commit();
                    return true;
                }
            };


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.admin_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.adminLogoutId) {
            CheckLogout();
        }
        return super.onOptionsItemSelected(item);
    }


    private void CheckLogout() {
        AlertDialog.Builder checkAlert = new AlertDialog.Builder(AdminActivity.this);
        checkAlert.setMessage(R.string.message_do_you_want_to_logout)
                .setCancelable(false).setPositiveButton(R.string.message_button_yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mAuth.signOut();
                Intent intent = new Intent(AdminActivity.this, LogInActivity.class);
                startActivity(intent);
                finish();
            }
        }).setNegativeButton(R.string.message_button_no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        AlertDialog alert = checkAlert.create();
        alert.setTitle(R.string.alert_dialog__logout);
        alert.show();

    }


    private void NotshowCartIcon() {
        //toolbar & cartIcon
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(false);
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