package com.example.letschat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.internal.Storage;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.core.persistence.PersistenceStorageEngine;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class SettingsActivity extends AppCompatActivity {
    private CircleImageView mDisplayImage;
    private TextView mName;
    private TextView mStatus;

    private Button mStatusbtn;
    private Button mImagebtn;

    private static final int GALLERY_PICK=1;

    private DatabaseReference mUserDatabse;
    private FirebaseUser mCurrentUser;

    private StorageReference mImageStorage;
    private ProgressDialog mProgressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mDisplayImage=findViewById(R.id.settings_Image);
        mName=findViewById(R.id.settings_display_name);
        mStatus=findViewById(R.id.settings_status);
        mStatusbtn=findViewById(R.id.settings_status_btn);
        mImagebtn=findViewById(R.id.settings_Image_btn);

        mImageStorage = FirebaseStorage.getInstance().getReference();


        mCurrentUser= FirebaseAuth.getInstance().getCurrentUser();
        String current_uid=mCurrentUser.getUid();

        mUserDatabse= FirebaseDatabase.getInstance().getReference().child("Users").child(current_uid);
        mUserDatabse.keepSynced(true);
        mUserDatabse.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                String name= dataSnapshot.child("name").getValue(String.class);
                final String image= dataSnapshot.child("image").getValue(String.class);
                String status= dataSnapshot.child("status").getValue(String.class);
                String thumb_image= dataSnapshot.child("thumb_image").getValue(String.class);

                    mName.setText(name);
                    mStatus.setText(status);
                    if(!image.equals("default")){
                       // Picasso.get().load(image).placeholder(R.drawable.ic_person).into(mDisplayImage);
                        Picasso.get().load(image).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.ic_person).into(mDisplayImage, new Callback() {
                            @Override
                            public void onSuccess() {

                            }

                            @Override
                            public void onError(Exception e) {
                                Picasso.get().load(image).placeholder(R.drawable.ic_person).into(mDisplayImage);
                            }
                        });

                    }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        mStatusbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String status_value=mStatus.getText().toString();
                Intent status_intent=new Intent(SettingsActivity.this,StatusActivity.class);
                status_intent.putExtra("status_value",status_value);
                startActivity(status_intent);
            }
        });

        mImagebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
                getIntent.setType("image/*");

                Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                pickIntent.setType("image/*");

                Intent chooserIntent = Intent.createChooser(getIntent, "Select Image");
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] {pickIntent});

                startActivityForResult(chooserIntent, GALLERY_PICK);
/*
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .start(SettingsActivity.this);
*/
            }
        });

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==GALLERY_PICK && resultCode==RESULT_OK){



            Uri imageUri=data.getData();
            CropImage.activity(imageUri)
                    .setAspectRatio(1,1)
            .start(this);
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                mProgressDialog=new ProgressDialog(SettingsActivity.this);
                mProgressDialog.setTitle("Uploading Image");
                mProgressDialog.setMessage("Please Wait");
                mProgressDialog.setCanceledOnTouchOutside(false);
                mProgressDialog.show();

                Uri resultUri = result.getUri();

                final File thumb_FilePath=new File(resultUri.getPath());

                String current_user_id=mCurrentUser.getUid();


                Bitmap thumb_bitmap= null;
                try {
                    thumb_bitmap = new Compressor(this)
                            .setMaxWidth(200)
                            .setMaxHeight(200)
                            .setQuality(75)
                            .compressToBitmap(thumb_FilePath);
                } catch (IOException e) {
                    e.printStackTrace();
                }


                ByteArrayOutputStream baos=new ByteArrayOutputStream();
                thumb_bitmap.compress(Bitmap.CompressFormat.JPEG,100,baos);
                final byte[] thumb_byte =  baos.toByteArray();

                StorageReference filepath=mImageStorage.child("profile_images").child(current_user_id +".jpg");
                final StorageReference thumb_filepath=mImageStorage.child("profile_images").child("thumbs").child(current_user_id +".jpg");

                filepath.putFile(resultUri)
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                final Task<Uri> firebaseUri = taskSnapshot.getStorage().getDownloadUrl();
                                firebaseUri.addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        final String download_url = uri.toString();
                                        UploadTask uploadTask = thumb_filepath.putBytes(thumb_byte);

                                        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                            @Override
                                            public void onSuccess(UploadTask.TaskSnapshot thumb_taskSnapshot) {
                                                final Task<Uri> firebaseuri=thumb_taskSnapshot.getStorage().getDownloadUrl();
                                                firebaseuri.addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                    @Override
                                                    public void onSuccess(Uri uri) {
                                                        final String thumb_downloadUrl=uri.toString();


                                                            Map update_hashMap = new HashMap<>();
                                                            update_hashMap.put("image", download_url);
                                                            update_hashMap.put("thumb_image", thumb_downloadUrl);

                                                            mUserDatabse.updateChildren(update_hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    if (task.isSuccessful()) {

                                                                        mProgressDialog.dismiss();
                                                                        Toast.makeText(SettingsActivity.this, " Successfully uploaded", Toast.LENGTH_LONG).show();
                                                                    }
                                                                }
                                                            });


                                                    }
                                                });
                                            }
                                        });


                                        /*
                                        uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> thumb_task) {




                                                       String thumb_downloadUrl = thumb_task.getResult().getMetadata().getReference().getDownloadUrl().toString();

                                                       if (thumb_task.isSuccessful()) {
                                                           Map update_hashMap = new HashMap<>();
                                                           update_hashMap.put("image", download_url);
                                                           update_hashMap.put("thumb_image", thumb_downloadUrl);

                                                           mUserDatabse.updateChildren(update_hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                               @Override
                                                               public void onComplete(@NonNull Task<Void> task) {
                                                                   if (task.isSuccessful()) {

                                                                       mProgressDialog.dismiss();
                                                                       Toast.makeText(SettingsActivity.this, " Successfully uploaded", Toast.LENGTH_LONG).show();
                                                                   }
                                                               }
                                                           });
                                                       } else {
                                                           Toast.makeText(SettingsActivity.this, "error in uploading thumbnail", Toast.LENGTH_LONG).show();
                                                           mProgressDialog.dismiss();
                                                       }


                                            }
                                        });
*/

                                    }
                                });

                            }
                        });
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }



}