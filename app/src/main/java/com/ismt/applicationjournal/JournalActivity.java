package com.ismt.applicationjournal;

import static android.text.TextUtils.isEmpty;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import  android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Objects;
import java.util.Random;


public class JournalActivity extends AppCompatActivity implements LocationListener {
    private static final int PICK_IMAGE_REQUEST = 101;
    private EditText thought_input, title_input;
    private Button submit_button, open_button, uploadPhoto;
    private ImageView imageView;
    private TextView browse, addJournal_label;
    private Location location;
    private LocationManager locationManager;
    private EditText txtLocation;
    private ImageButton add_date;
    private int REQUEST_CAMERA= 3;
    private int REQUEST_CODE_STORAGE_PERMISSION;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener authStateListener;

    private String selectedImagePath;

    EditText date_input;
    Uri filepath;
    Bitmap bitmap;
    public static final int GALLERY_CODE =1;

    DAOJournal dao;
    Journal edit_journal;
    ImageView image;

    final String[] thisuri = new String[1];
    final String[] thislocation = new String[2];

    Calendar calendar = Calendar.getInstance();
    SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_journal);
        Objects.requireNonNull(getSupportActionBar()).setElevation(0);
        mAuth = FirebaseAuth.getInstance();
         txtLocation = findViewById(R.id.location_txt);

        dao = new DAOJournal();
        edit_journal = (Journal) getIntent().getSerializableExtra("EDIT");

        title_input = (EditText) findViewById(R.id.title_input);
        thought_input = (EditText) findViewById(R.id.thought_input);

        submit_button = (Button) findViewById(R.id.submit_button);
        open_button = (Button) findViewById(R.id.open_button);
        uploadPhoto = (Button) findViewById(R.id.uploadPhoto);

        addJournal_label = findViewById(R.id.addJournal_label);
        image = findViewById(R.id.image);

        add_date = findViewById(R.id.add_date);

        open_button.setOnClickListener(view -> {
            Intent intent = new Intent(JournalActivity.this, MainActivity.class);
            startActivity(intent);
        });


        if (edit_journal != null) {
            addJournal_label.setText("Update Journal");
            submit_button.setText("UPDATE");
            title_input.setText(edit_journal.getTitle());
            thought_input.setText(edit_journal.getThought());

            txtLocation.setText("lat: " + edit_journal.getLat() + ",\nlng: " + edit_journal.getLng());
            add_date.setVisibility(View.VISIBLE);
            date_input = findViewById(R.id.date_input);
            date_input.setText(formatter.format(calendar.getTime()));
            uploadPhoto.setVisibility(View.GONE);


        } else {
            submit_button.setText("Submit");
        }

        submit_button.setOnClickListener(view -> {
                addJournal();

        });

        date_input = findViewById(R.id.date_input);
        date_input.setText(formatter.format(calendar.getTime()));

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int hr = calendar.get(Calendar.HOUR);
        int min = calendar.get(Calendar.MINUTE);
        int sec = calendar.get(Calendar.SECOND);

        DatePickerDialog dialog = new DatePickerDialog(JournalActivity.this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int yr, int monthOfYear, int day) {
                int mm = monthOfYear + 1;
                date_input.setText(day + "-" + mm + "-" + yr + " " + hr + ":" + min + ":" + sec);
            }
        }, year, month, day);


        add_date.setOnClickListener(v -> {
            dialog.show();
        });

        ImageButton locate_btn = findViewById(R.id.locate_btn);
        locate_btn.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 100);
            }
            accessLocationService();

            if (location != null) {
                txtLocation.setText("lat: " + location.getLatitude() + ",\nlng: " + location.getLongitude());
            }
        });
        uploadPhoto.setOnClickListener(view -> {
            if (ContextCompat.checkSelfPermission(
                    getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        JournalActivity.this,
                        new String[] {Manifest.permission.READ_EXTERNAL_STORAGE},
                        REQUEST_CODE_STORAGE_PERMISSION
                );
            } else {
                uploadImage();
            }
        });
        accessLocationService();

    }


    private void uploadImage() {
        /*ProgressDialog dialog = new ProgressDialog(this);
        dialog.setTitle("File uploader");
        dialog.show();

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference uploader = storage.getReference().child("image1" + new Random().nextInt(50));

        uploader.putFile(filepath).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                dialog.dismiss();
                Toast.makeText(JournalActivity.this, "File uploaded successfully", Toast.LENGTH_SHORT).show();

                uploader.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri getImageUri) {
                        thisuri[0] = getImageUri.toString();
                        addJournal();
                    }
                });
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                float percentage = (100 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
                dialog.setMessage("uploaded: " + (int) percentage + "%");
            }
        });*/
        final CharSequence[] items = {"Camera", "Gallery", "Cancel"};
        AlertDialog.Builder builder = new AlertDialog.Builder(JournalActivity.this);
        builder.setTitle("Add image");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (items[i].equals("Camera")){
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(intent,REQUEST_CAMERA);
                }
                else if (items[i].equals("Cancel")){
                    dialogInterface.dismiss();
                }
            }
        });
        builder.show();
    }
    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    private void addJournal() {

        dao = new DAOJournal();
        edit_journal = (Journal) getIntent().getSerializableExtra("EDIT");

        thislocation[0] = "";
        thislocation[1] = "";

        if(location!= null ){
            thislocation[0] = String.valueOf(location.getLatitude());
            thislocation[1] = String.valueOf(location.getLongitude());
        }

        if (isEmpty(title_input.getText().toString())) {
            title_input.setError("");
            return;
        }

        Journal obj = new Journal(
                title_input.getText().toString(),
                thought_input.getText().toString(),
                thisuri[0],
                thislocation[0],
                thislocation[1],
                date_input.getText().toString()

        );
        if (edit_journal == null) {

            dao.add(obj).addOnSuccessListener(suc -> {
                Toast.makeText(JournalActivity.this, "Inserted ", Toast.LENGTH_SHORT).show();
            }).addOnFailureListener(er -> {
                Toast.makeText(JournalActivity.this, "" + er.getMessage(), Toast.LENGTH_SHORT).show();
            });
        }


        if (edit_journal != null) {

            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("title", title_input.getText().toString());
            hashMap.put("thought", thought_input.getText().toString());
            hashMap.put("lat", thislocation[0]);
            hashMap.put("lng", thislocation[1]);
            hashMap.put("date",date_input.getText().toString());
            hashMap.put("image", uploadPhoto.getText().toString());

            if (filepath != null) {
                hashMap.put("pimage", thisuri[0]);
            }

            dao.update(edit_journal.getKey(), hashMap).addOnSuccessListener(suc -> {
                Toast.makeText(this, "updated ", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, MainActivity.class));
                this.finish();

            }).addOnFailureListener(er -> {
                Toast.makeText(this, "" + er.getMessage(), Toast.LENGTH_SHORT).show();
            });
        }

        finish();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_CAMERA && requestCode == Activity.RESULT_OK) {
            Bitmap photo = (Bitmap) data.getExtras().get("data");
           // uploadPhoto.setImageBitmap(photo);
            uploadPhoto.setVisibility(View.VISIBLE);
            // findViewById(R.id.imageRemove).setVisibility(View.VISIBLE);
            Uri tempUri = getImageUri(getApplicationContext(), photo);
            selectedImagePath = getPathFromUri(tempUri);
        }
    }

    private void accessLocationService() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
            Toast.makeText(this, "Location permission granted", Toast.LENGTH_SHORT).show();
            accessLocationService();
        }

    }

    private String getPathFromUri(Uri contentUri){
        String filePath;
        Cursor cursor = getContentResolver()
                .query(contentUri, null, null, null, null);
        if (cursor == null){
            filePath = contentUri.getPath();
        }
        else {
            cursor.moveToFirst();
            int index = cursor.getColumnIndex("_data");
            filePath = cursor.getString(index);
            cursor.close();
        }
        return filePath;
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        this.location = location;
        txtLocation.setText("lat: " + location.getLatitude() + ",\nlng: " + location.getLongitude());
    }
}

