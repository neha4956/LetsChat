package com.example.letschat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    private TextInputEditText mLoginEmail;
    private TextInputEditText mLoginPassword;
    private Button mLogin_btn;
    private ProgressDialog mLoginProgress;
    private FirebaseAuth mAuth;

    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth=FirebaseAuth.getInstance();

        mToolbar=(Toolbar)findViewById(R.id.login_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("LogIn");
        getSupportActionBar().setDefaultDisplayHomeAsUpEnabled(true);


        mLoginProgress=new ProgressDialog(this);

           mLoginEmail=findViewById(R.id.login_email);
           mLoginPassword=findViewById(R.id.login_password);
           mLogin_btn=findViewById(R.id.login_btn);
           mLogin_btn.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View view) {
                  String email=mLoginEmail.getText().toString();
                  String password=mLoginPassword.getText().toString();
                  if(!TextUtils.isEmpty(email) || !TextUtils.isEmpty(password)){

                      mLoginProgress.setTitle("Logging In");
                      mLoginProgress.setMessage("Please Wait");
                      mLoginProgress.setCanceledOnTouchOutside(false);
                      mLoginProgress.show();

                      loginUser(email,password);
                  }
               }
           });
    }

    private void loginUser(String email, String password) {
      mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
          @Override
          public void onComplete(@NonNull Task<AuthResult> task) {

              mLoginProgress.dismiss();

              if(task.isSuccessful()){
               Intent mainIntent=new Intent(LoginActivity.this,MainActivity.class);
               mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
               startActivity(mainIntent);
               finish();
              }

             else{
                  mLoginProgress.hide();

                  Toast.makeText(LoginActivity.this, "Please check your Email and Password once Again", Toast.LENGTH_SHORT).show();

              }
          }
      });


    }
}