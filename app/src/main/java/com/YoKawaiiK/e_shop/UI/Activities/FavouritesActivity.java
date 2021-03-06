package com.YoKawaiiK.e_shop.UI.Activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.YoKawaiiK.e_shop.Adapters.AdminOfferAdapter;
import com.YoKawaiiK.e_shop.Adapters.CategoryProductInfoAdapter;
import com.YoKawaiiK.e_shop.Adapters.MyAdapterRecyclerView;
import com.YoKawaiiK.e_shop.Model.FavouritesClass;
import com.YoKawaiiK.e_shop.R;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class FavouritesActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ActionBarDrawerToggle mToggle;
    private Toolbar mToolBar;
    private TextView mperson_name;
    private CircleImageView image;
    private FirebaseAuth mAuth;
    private FirebaseUser CurrentUser;
    private String UserId;
    private RecyclerView.Adapter my_adapter;
    //Custom Xml Views (cart Icon)
    private RelativeLayout CustomCartContainer;
    private TextView PageTitle;
    private TextView CustomCartNumber;


    ArrayList<FavouritesClass> favouriteList;
//    private ArrayList<CategoryProductInfo> CategoryProducts;
    private MyAdapterRecyclerView.onItemClickListener listener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favourites_activity);

        mAuth = FirebaseAuth.getInstance();
        CurrentUser = mAuth.getCurrentUser();
        UserId = CurrentUser.getUid();

        mToolBar = findViewById(R.id.main_Toolbar);
        setSupportActionBar(mToolBar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        //define Navigation Viewer and got its data
        DefineNavigation();

        retrieveFav();
        //on clicking any product (go to ProductInfo Activity to show it's info)
        onClickAnyProduct();

    }

    @Override
    protected void onStart() {
        super.onStart();

        //Refresh CartIcon
        showCartIcon();

        //to check if the total price is zero or not
        HandleTotalPriceToZeroIfNotExist();
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (mToggle.onOptionsItemSelected(item)) return true;
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        int id = menuItem.getItemId();
        if (id == R.id.Home) {
            startActivity(new Intent(FavouritesActivity.this, MainActivity.class));
        } else if (id == R.id.Profile) {
            startActivity(new Intent(FavouritesActivity.this, UserProfileActivity.class));
        } else if (id == R.id.Cart) {
            startActivity(new Intent(FavouritesActivity.this, CartActivity.class));
        } else if (id == R.id.MyOrders) {
            startActivity(new Intent(FavouritesActivity.this, OrderActivity.class));
        } else if (id == R.id.fruits) {
            Intent intent = new Intent(FavouritesActivity.this, CategoryActivity.class);
            intent.putExtra(getString(R.string.intentStringExtraCategoryName), getString(R.string.intentStringExtraCategoryFruits));
            startActivity(intent);
        } else if (id == R.id.vegetables) {
            Intent intent = new Intent(FavouritesActivity.this, CategoryActivity.class);
            intent.putExtra(getString(R.string.intentStringExtraCategoryName), getString(R.string.intentStringExtraCategoryVegetables));
            startActivity(intent);
        } else if (id == R.id.meats) {
            Intent intent = new Intent(FavouritesActivity.this, CategoryActivity.class);
            intent.putExtra(getString(R.string.intentStringExtraCategoryName), getString(R.string.intentStringExtraCategoryMeats));
            startActivity(intent);
        } else if (id == R.id.electronics) {
            Intent intent = new Intent(FavouritesActivity.this, CategoryActivity.class);
            intent.putExtra(getString(R.string.intentStringExtraCategoryName), getString(R.string.intentStringExtraCategoryElectronics));
            startActivity(intent);
        } else if (id == R.id.Logout) {
            CheckLogout();
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }


    private void CheckLogout() {
        AlertDialog.Builder checkAlert = new AlertDialog.Builder(FavouritesActivity.this);
        checkAlert.setMessage(R.string.checkLogoutMessage)
                .setCancelable(false).setPositiveButton(R.string.checkLogoutAnswerYes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(FavouritesActivity.this, LogInActivity.class);
                startActivity(intent);
                finish();
            }
        }).setNegativeButton(R.string.checkLogoutAnswerNo, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        AlertDialog alert = checkAlert.create();
        alert.setTitle(getString(R.string.checkLogoutTitle));
        alert.show();

    }


    private void DefineNavigation() {
        View mnavigationview;
        navigationView = findViewById(R.id.navigation_view2);
        drawerLayout = findViewById(R.id.drawer2);

        navigationView.setNavigationItemSelectedListener(this);
        mnavigationview = navigationView.getHeaderView(0);
        mperson_name = mnavigationview.findViewById(R.id.persname);
        image = mnavigationview.findViewById(R.id.circimage);


        mToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.Open, R.string.Close);
        drawerLayout.addDrawerListener(mToggle);
        mToggle.syncState();

        getNavHeaderData();
    }

    public void retrieveFav() {
        LinearLayout mylayout = (LinearLayout) findViewById(R.id.recyclerViewlayout);
        LayoutInflater inflater = getLayoutInflater();
        inflater.inflate(R.layout.favourite_recycler_view, mylayout, false);
        final RecyclerView rc = mylayout.findViewById(R.id.recyclerView);
        // rc.setHasFixedSize(true);
        // rc.setLayoutManager(new LinearLayoutManager(this));
        GridLayoutManager mGridLayoutManager;
        mGridLayoutManager = new GridLayoutManager(this, 2, GridLayoutManager.VERTICAL, false);
        rc.setLayoutManager(mGridLayoutManager);
        favouriteList = new ArrayList<>();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("favourites")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());

        my_adapter = new MyAdapterRecyclerView(listener, favouriteList);
        rc.setAdapter(my_adapter);

        ValueEventListener eventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    Log.i("FavoritesActivity-retrieveFav", String.valueOf(ds.child("producttitle").getValue()));
                    Log.i("FavoritesActivity-retrieveFav", String.valueOf(ds.child("expiredDate").getValue()));
                    Log.i("FavoritesActivity-retrieveFav", String.valueOf(ds.child("checked").getValue()));
                    Log.i("FavoritesActivity-retrieveFav", String.valueOf(ds.child("productprice").getValue()));
                    Log.i("FavoritesActivity-retrieveFav", String.valueOf(ds.child("productimage").getValue()));

                    FavouritesClass fav = new FavouritesClass(
                            String.valueOf(ds.child("productimage").getValue()),
                            String.valueOf(ds.child("producttitle").getValue()),
                            String.valueOf(ds.child("productprice").getValue()),
                            String.valueOf(ds.child("expiredDate").getValue()),
                            Boolean.valueOf(String.valueOf(ds.child("checked").getValue()))
                    );

//                    String productImage,
//                    String productTitle,
//                    String productPrice,
//                    String productExpiryDate,
//                    boolean isFavorite

//                    FavouritesClass fav = new FavouritesClass();

//                    fav = ds.getValue(FavouritesClass.class);
                    favouriteList.add(fav);
                    my_adapter.notifyDataSetChanged();
                }
//                my_adapter = new MyAdapterRecyclerView(listener, favouriteList);
//                rc.setAdapter(my_adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        };
        ref.addListenerForSingleValueEvent(eventListener);
    }


//    adapter.setOnItemClickListener(new AdminOfferAdapter.onItemClickListener() {
//        @Override
//        public void onItemClick(int pos) {
//            Intent i = new Intent(getActivity(), EditProductActivity.class);
//            Bundle b = new Bundle();
//            b.putString("img", adminProducts.get(pos).getImage());
//            b.putString("name", adminProducts.get(pos).getName());
//            b.putString("category", adminProducts.get(pos).getCategory());
//            b.putString("expired", adminProducts.get(pos).getExpired());
//            b.putString("price", adminProducts.get(pos).getPrice());
//            b.putString("quantity", adminProducts.get(pos).getQuantity());
//            i.putExtras(b);
//            startActivity(i);
//        }
//    });


    private void onClickAnyProduct() {

        listener = new MyAdapterRecyclerView.onItemClickListener() {
            @Override
            public void onItemClick(int pos) {
                FavouritesClass fav = favouriteList.get(pos);

                Log.i("FavoritesActivity", String.valueOf(fav));

                Intent intent = new Intent(FavouritesActivity.this, ProductInfoActivity.class);
                intent.putExtra(getString(R.string.intentStringExtraProductName), fav.getProductTitle());
                intent.putExtra(getString(R.string.intentStringExtraProductPrice), fav.getProductPrice());
                intent.putExtra(getString(R.string.intentStringExtraProductImage), fav.getProductImage());
                intent.putExtra(getString(R.string.intentStringExtraProductExpiryDate), fav.getProductExpiryDate());
                intent.putExtra(getString(R.string.intentStringExtraProductIsFavorite), String.valueOf(fav.isFavorite()));
                intent.putExtra(getString(R.string.intentStringExtraIsOffered), "no");

                startActivity(intent);
            }

        };
    }


    private void getNavHeaderData() {
        DatabaseReference root = FirebaseDatabase.getInstance().getReference();
        DatabaseReference m = root.child("users").child(UserId);
        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String name = dataSnapshot.child("Name").getValue().toString();
                    String photo = dataSnapshot.child("Image").getValue().toString();
                    if (photo.equals("default")) {
                        Picasso.get().load(R.drawable.profile).into(image);
                    } else
                        Picasso.get().load(photo).placeholder(R.drawable.profile).into(image);
                    mperson_name.setText(name);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        };
        m.addListenerForSingleValueEvent(valueEventListener);
//        retrieveFav();
    }


    private void showCartIcon() {
        //toolbar & cartIcon
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.buyer_toolbar, null);
        actionBar.setCustomView(view);

        //************custom action items xml**********************
        CustomCartContainer = (RelativeLayout) findViewById(R.id.CustomCartIconContainer);
        PageTitle = (TextView) findViewById(R.id.PageTitle);
        CustomCartNumber = (TextView) findViewById(R.id.CustomCartNumber);

        PageTitle.setText(R.string.faPageTitle);
        setNumberOfItemsInCartIcon();

        CustomCartContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(FavouritesActivity.this, CartActivity.class));
            }
        });

    }


    private void setNumberOfItemsInCartIcon() {
        DatabaseReference root = FirebaseDatabase.getInstance().getReference();
        DatabaseReference m = root.child("cart").child(UserId);
        ValueEventListener eventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    if (dataSnapshot.getChildrenCount() == 1) {
                        CustomCartNumber.setVisibility(View.GONE);
                    } else {
                        CustomCartNumber.setVisibility(View.VISIBLE);
                        CustomCartNumber.setText(String.valueOf(dataSnapshot.getChildrenCount() - 1));
                    }
                } else {
                    CustomCartNumber.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        };
        m.addListenerForSingleValueEvent(eventListener);
    }

    private void HandleTotalPriceToZeroIfNotExist() {
        DatabaseReference root = FirebaseDatabase.getInstance().getReference();
        DatabaseReference m = root.child("cart").child(UserId);
        ValueEventListener eventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    FirebaseDatabase.getInstance().getReference().child("cart").child(UserId).child("totalPrice").setValue("0");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        };
        m.addListenerForSingleValueEvent(eventListener);

    }

}