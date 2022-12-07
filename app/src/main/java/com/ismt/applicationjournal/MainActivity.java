package com.ismt.applicationjournal;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.core.view.View;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    Button add_button;
    String userId;
    GoogleSignInOptions gso;
    GoogleSignInClient mGoogleSignInClient;

    FirebaseAuth mFirebaseAuth;
    SwipeRefreshLayout swipeRefreshLayout;
    RecyclerView recyclerView;
    RVadapter adapter;
    DAOJournal dao;
    boolean isLoading = false;
    String key = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        add_button = findViewById(R.id.add_button);
        add_button.setOnClickListener( v -> {
            startActivity(new Intent(this, JournalActivity.class));
        });
        recyclerView = findViewById(R.id.rv);
        recyclerView.setHasFixedSize(true);

        LinearLayoutManager manager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(manager);

        adapter = new RVadapter(this);
        recyclerView.setAdapter(adapter);

        dao = new DAOJournal();
        loadData();

        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                //.requestIdToken(("238196444996-urssi2cn93ffk2pfbe7s9l2qqimgne2s.apps.googleusercontent.com"))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.logout_menu, menu);
        return  true;


    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
          switch (item.getItemId()){
              case R.id.logoutbar:
               Intent intent = new Intent(this,login.class);
               startActivity(intent);
                  Toast.makeText(this, "Logged Out", Toast.LENGTH_SHORT).show();
          }
         return super.onOptionsItemSelected(item);
    }

    private void loadData() {
        dao.get(key).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                ArrayList<Journal> journals = new ArrayList<>();

                for (DataSnapshot data : snapshot.getChildren()) {
                    Journal journal = data.getValue(Journal.class);
                    journal.setKey(data.getKey());
                    journals.add(journal);
                    key = data.getKey();
                }
                adapter.setItems(journals);
                adapter.notifyDataSetChanged();

                isLoading = false;

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    public void logout(View v){
       mFirebaseAuth.signOut();
    }
}