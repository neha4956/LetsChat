package com.example.letschat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {
    private TextInputEditText mDisplayName;
    private TextInputEditText mEmail;
    private TextInputEditText mPassword;
    private Button mCreateBtn;
    private FirebaseAuth mAuth;
    private Toolbar mToolbar;
    private ProgressDialog mRegProgress;
    private DatabaseReference mDatabase;
    private  DatabaseReference mDatabase1;

    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mRegProgress=new ProgressDialog(this);


        mToolbar=(Toolbar)findViewById(R.id.register_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Create Account");
        getSupportActionBar().setDefaultDisplayHomeAsUpEnabled(true);


        mAuth=FirebaseAuth.getInstance();


        mDisplayName=(TextInputEditText)findViewById(R.id.reg_display_name);
        mEmail=(TextInputEditText)findViewById(R.id.reg_email);
        mPassword=(TextInputEditText)findViewById(R.id.reg_password);
        mCreateBtn=(Button) findViewById(R.id.reg_create_btn);


        mCreateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String display_name=mDisplayName.getText().toString();
                String email=mEmail.getText().toString();
                String password=mPassword.getText().toString();
                 if(!TextUtils.isEmpty(display_name)|| !TextUtils.isEmpty(email)||TextUtils.isEmpty(password)){
                     mRegProgress.setTitle("Regsitering User");
                     mRegProgress.setMessage("please wait");
                     mRegProgress.setCanceledOnTouchOutside(false);
                     mRegProgress.show();

                     register_user(display_name,email,password);
                 }

            }
        });
    }

    private void register_user(final String display_name, String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener( new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            FirebaseAuth current_user=FirebaseAuth.getInstance();
                                 String uid= current_user.getUid();
                                 mDatabase= FirebaseDatabase.getInstance().getReference().child("Users").child(uid);

                            HashMap<String , String> userMap=new HashMap<>();
                            userMap.put("name", display_name);
                            userMap.put("status","Hi there. I'm Using Let's Chat App.");
                            userMap.put("image","default");
                            userMap.put("thumb_image","default");
                            userMap.put("userId",uid);
                            userMap.put("user_id2",uid);
                            mDatabase.setValue(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()) {


                                        mRegProgress.dismiss();

                                        Intent mainIntent = new Intent(RegisterActivity.this, MainActivity.class);
                                        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(mainIntent);
                                        finish();


                                    }
                                }
                            });


                        } else {
                                 mRegProgress.hide();

                            Toast.makeText(RegisterActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();

                        }
                    }
                });

    }
}