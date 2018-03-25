package com.corelabsplus.getmetheredriver.activities;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.corelabsplus.getmetheredriver.R;
import com.corelabsplus.getmetheredriver.utils.Driver;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class LoginActivity extends AppCompatActivity {

    private FloatingActionButton setNumber;
    private EditText countryCode, phoneNumber;
    //private FrameLayout confrimCodeFragmentContainer;
    private EditText confirmCode;
    private FloatingActionButton confirm, reload;
    private ProgressBar loadingBar;
    private TextView loadingLabel, mainLabel, disclaimer;
    private Toolbar toolbar;
    int backCounter = 0;


    private static final String TAG = "LoginActivity";

    private static final String KEY_VERIFY_IN_PROGRESS = "key_verify_in_progress";

    private static final int STATE_INITIALIZED = 1;
    private static final int STATE_CODE_SENT = 2;
    private static final int STATE_VERIFY_FAILED = 3;
    private static final int STATE_VERIFY_SUCCESS = 4;
    private static final int STATE_SIGNIN_FAILED = 5;
    private static final int STATE_SIGNIN_SUCCESS = 6;

    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;

    // [START declare_auth]
    private FirebaseAuth mAuth;
    // [END declare_auth]

    private boolean mVerificationInProgress = false;

    private FirebaseUser user;
    private String phone;

    private AlertDialog enterPlateDialog;
    private View enterPlateView;
    private AlertDialog.Builder enterPlate;
    private EditText enteredPlate;
    private String enteredPlateStr;
    private FloatingActionButton okBtn, cancelBtn;

    private DatabaseReference databaseBuses;
    private DatabaseReference databaseDrivers;
    private String driver_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_login);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
//        toolbar.setTitle("Login");

        databaseBuses = FirebaseDatabase.getInstance().getReference("buses");
        databaseDrivers = FirebaseDatabase.getInstance().getReference("drivers");


        // [START initialize_auth]
        mAuth = FirebaseAuth.getInstance();
        // [END initialize_auth]

        countryCode = (EditText) findViewById(R.id.country_code);
        phoneNumber = (EditText) findViewById(R.id.phone);
        confirm = (FloatingActionButton) findViewById(R.id.confirm);
        confirmCode = (EditText) findViewById(R.id.code);
        reload = (FloatingActionButton) findViewById(R.id.reloader);
        loadingBar = (ProgressBar) findViewById(R.id.fLoadingBar);
        mainLabel = (TextView) findViewById(R.id.main_label);
        disclaimer = (TextView) findViewById(R.id.disclaimer);
        loadingLabel = (TextView) findViewById(R.id.fLoadinglabel);
        setNumber = (FloatingActionButton) findViewById(R.id.send_code_btn);
        toolbar = (Toolbar) findViewById(R.id.toolbar);

        enterPlate = new AlertDialog.Builder(this);
        enterPlateView = getLayoutInflater().inflate(R.layout.enter_platenumber_dialog, null);
        enteredPlate = (EditText) enterPlateView.findViewById(R.id.enteredOrderId);
        okBtn = (FloatingActionButton) enterPlateView.findViewById(R.id.enterIdBtn);

        toolbar.setTitle("Set up phone number");

        TextWatcher countryCodeWatcher = new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                String theCode = countryCode.getText().toString().trim();

                if (theCode.startsWith("+") && theCode.length() == 4){
                    phoneNumber.requestFocus();
                }

                else if (!theCode.startsWith("+")){
                    countryCode.setError("Must start with \"+\"");
                }
            }
        };

        countryCode.addTextChangedListener(countryCodeWatcher);


        final TextWatcher phoneNumberWatcher = new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                String thePhone = phoneNumber.getText().toString().trim();

                if (!thePhone.contains("+") && thePhone.length() == 12){
                    phoneNumber.clearFocus();
                }

                else if (thePhone.contains("+")){
                    phoneNumber.setError("Invalid character detected, please remove it.");
                }
            }
        };

        phoneNumber.addTextChangedListener(phoneNumberWatcher);

        setNumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String country;
                country = countryCode.getText().toString();
                phone = phoneNumber.getText().toString();

                if (country.isEmpty()){
                    countryCode.setError("Country code is required.");
                }

                else if (phone.isEmpty()){
                    phoneNumber.setError("Phone number is required.");
                }

                else{
                    setNumber.setVisibility(View.INVISIBLE);
                    loadingBar.setVisibility(View.VISIBLE);
                    loadingLabel.setVisibility(View.VISIBLE);
                    loadingLabel.setText("Contacting server...");
                    startPhoneNumberVerification(phone);
                }
            }
        });

        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String code = confirmCode.getText().toString().trim();
                if (code.isEmpty()){
                    confirmCode.setError("This field is required!");
                }

                else if (code.length() != 6){
                    confirmCode.setError("Invalid code!");
                }

                else{
                    confirm.setVisibility(View.INVISIBLE);
                    loadingBar.setVisibility(View.VISIBLE);
                    loadingLabel.setText("Verifying...");
                    loadingLabel.setVisibility(View.VISIBLE);
                    verifyPhoneNumberWithCode(mVerificationId, code);
                }

            }
        });

        // Initialize phone auth callbacks
        // [START phone_auth_callbacks]
        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            @Override
            public void onVerificationCompleted(PhoneAuthCredential credential) {
                // This callback will be invoked in two situations:
                // 1 - Instant verification. In some cases the phone number can be instantly
                //     verified without needing to send or enter a verification code.
                // 2 - Auto-retrieval. On some devices Google Play services can automatically
                //     detect the incoming verification SMS and perform verification without
                //     user action.
//                user = mAuth.getCurrentUser();
//                getActiveOrder();
                Log.d(TAG, "onVerificationCompleted:" + credential);
                // [START_EXCLUDE silent]
                mVerificationInProgress = false;
                // [END_EXCLUDE]

                // [START_EXCLUDE silent]
                // Update the UI and attempt sign in with the phone credential
                // [END_EXCLUDE]

                signInWithPhoneAuthCredential(credential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                // This callback is invoked in an invalid request for verification is made,
                // for instance if the the phone number format is not valid.
                Log.w(TAG, "onVerificationFailed", e);
                // [START_EXCLUDE silent]
                mVerificationInProgress = false;
                // [END_EXCLUDE]

                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    // Invalid request
                    // [START_EXCLUDE]
                    //onBackPressed();

                    toolbar.setTitle("Set up phone number");
                    phoneNumber.setError("Invalid phone number.");
                    phoneNumber.setEnabled(true);
                    phoneNumber.setVisibility(View.VISIBLE);
                    countryCode.setVisibility(View.VISIBLE);
                    countryCode.requestFocus();
                    setNumber.setVisibility(View.VISIBLE);
                    confirmCode.setVisibility(View.INVISIBLE);
                    confirm.setVisibility(View.INVISIBLE);

                    loadingBar.setVisibility(View.INVISIBLE);
                    loadingLabel.setVisibility(View.INVISIBLE);

                    mainLabel.setText("Please Enter your phone number below and start making orders with us!");
                    disclaimer.setText("Please make sure that your carrier supports international Short Messaging Services (SMS), and note that your carrier charges for SMS may apply.");
                    // [END_EXCLUDE]
                } else if (e instanceof FirebaseTooManyRequestsException) {
                    // The SMS quota for the project has been exceeded
                    // [START_EXCLUDE]

                    toolbar.setTitle("Set up phone number");

                    phoneNumber.setError("Invalid phone number.");
                    phoneNumber.setEnabled(true);
                    phoneNumber.setVisibility(View.VISIBLE);
                    countryCode.setVisibility(View.VISIBLE);
                    countryCode.requestFocus();
                    setNumber.setVisibility(View.VISIBLE);
                    confirmCode.setVisibility(View.INVISIBLE);
                    confirm.setVisibility(View.INVISIBLE);

                    loadingBar.setVisibility(View.INVISIBLE);
                    loadingLabel.setVisibility(View.INVISIBLE);



                    mainLabel.setText("Please Enter your phone number below and start making orders with us!");
                    disclaimer.setText("Please make sure that your carrier supports international Short Messaging Services (SMS), and note that your carrier charges for SMS may apply.");
                    Snackbar.make(findViewById(android.R.id.content), "Quota exceeded.",
                            Snackbar.LENGTH_SHORT).show();
                    // [END_EXCLUDE]
                }

                // Show a message and update the UI
                // [START_EXCLUDE]
                //updateUI(STATE_VERIFY_FAILED);
                // [END_EXCLUDE]
            }

            @Override
            public void onCodeSent(String verificationId,
                                   PhoneAuthProvider.ForceResendingToken token) {
                // The SMS verification code has been sent to the provided phone number, we
                // now need to ask the user to enter the code and then construct a credential
                // by combining the code with a verification ID.
                Log.d(TAG, "onCodeSent:" + verificationId);

                // Save verification ID and resending token so we can use them later
                mVerificationId = verificationId;
                mResendToken = token;

                toolbar.setTitle("Confirm phone number");

                phoneNumber.setVisibility(View.INVISIBLE);
                countryCode.setVisibility(View.INVISIBLE);
                setNumber.setVisibility(View.INVISIBLE);


                confirmCode.setEnabled(true);
                confirmCode.setVisibility(View.VISIBLE);

                loadingBar.setVisibility(View.INVISIBLE);
                loadingLabel.setVisibility(View.INVISIBLE);
                reload.setVisibility(View.INVISIBLE);
                confirm.setVisibility(View.VISIBLE);
                confirmCode.requestFocus();

                mainLabel.setText("Almost there! You are on last step!");
                disclaimer.setText("Please enter the activation code you've received in the SMS to confirm your phone number.");

                //sendCode();

                // [START_EXCLUDE]
                // Update UI
                //updateUI(STATE_CODE_SENT);
                // [END_EXCLUDE]
            }
        };
        // [END phone_auth_callbacks]
    }

    // [START on_start_check_user]
    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        //getActiveOrder(currentUser);
        //updateUI(currentUser);

        // [START_EXCLUDE]
        if (mVerificationInProgress && validatePhoneNumber()) {
            startPhoneNumberVerification(phoneNumber.getText().toString());
        }
        // [END_EXCLUDE]
    }
    // [END on_start_check_user]

    private boolean validatePhoneNumber() {
        String thePhoneNumber = phoneNumber.getText().toString();
        if (TextUtils.isEmpty(thePhoneNumber)) {
            phoneNumber.setError("Invalid phone number.");
            return false;
        }
        else{
            return true;
        }
    }

    private void startPhoneNumberVerification(String phoneNumber) {
        // [START start_phone_auth]
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,               // Activity (for callback binding)
                mCallbacks);        // OnVerificationStateChangedCallbacks
        // [END start_phone_auth]

        mVerificationInProgress = true;
    }

    // [START sign_in_with_phone]
    public void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {

        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            user = task.getResult().getUser();

                            confirmCode.setEnabled(false);
                            confirm.setVisibility(View.INVISIBLE);

                            Intent intent = new Intent(LoginActivity.this, MapsActivity.class);
                            startActivity(intent);
                            finish();

//                            loadingBar.setVisibility(View.VISIBLE);
//                            loadingLabel.setVisibility(View.VISIBLE);

//                            enterPlateDialog = enterPlate.create();
//                            enterPlateDialog.show();
//                            enterPlateDialog.setCanceledOnTouchOutside(false);
//                            enterPlateDialog.setCancelable(false);
//
//                            okBtn.setOnClickListener(new View.OnClickListener() {
//                                @Override
//                                public void onClick(View v) {
//                                    loadingBar.setVisibility(View.VISIBLE);
//                                    loadingLabel.setText("Please wait...");
//                                    loadingLabel.setVisibility(View.VISIBLE);
//
//                                    enteredPlateStr = enteredPlate.getText().toString();
//
//                                    if(TextUtils.isEmpty(enteredPlateStr)){
//                                        enteredPlate.setError("Please enter bus plate number.");
//                                    }
//                                    else{
////                                        databaseBuses.addValueEventListener(new ValueEventListener() {
////                                            @Override
////                                            public void onDataChange(DataSnapshot dataSnapshot) {
////                                                if(dataSnapshot.exists()){
////
////                                                }
////                                            }
////
////                                            @Override
////                                            public void onCancelled(DatabaseError databaseError) {
////
////                                            }
////                                        });
//                                        Intent intent = new Intent(LoginActivity.this, MapsActivity.class);
//                                        intent.putExtra("plateNumber", enteredPlateStr);
//                                        startActivity(intent);
//                                        finish();
//                                    }
//                                }
//                            });
//                            checkProfile(user);

                            //checkProfile(user);
                            // [START_EXCLUDE]
                            //updateUI(STATE_SIGNIN_SUCCESS, user);
                            // [END_EXCLUDE]
                        } else {
                            // Sign in failed, display a message and update the UI
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                // The verification code entered was invalid
                                // [START_EXCLUDE silent]
                                confirmCode.setEnabled(true);
                                confirmCode.setError("Invalid code.");
                                loadingBar.setVisibility(View.INVISIBLE);
                                loadingLabel.setVisibility(View.INVISIBLE);
                                reload.setVisibility(View.INVISIBLE);
                                confirm.setVisibility(View.VISIBLE);
                                confirmCode.requestFocus();
                                // [END_EXCLUDE]
                            }
                            // [START_EXCLUDE silent]
                            // Update UI
                            //updateUI(STATE_SIGNIN_FAILED);
                            // [END_EXCLUDE]
                        }
                    }
                });
    }
    // [END sign_in_with_phone]

    private void verifyPhoneNumberWithCode(String verificationId, String code) {
        // [START verify_with_code]
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
        // [END verify_with_code]
        signInWithPhoneAuthCredential(credential);
    }

//    public boolean checkPhone(String driver_phone){
//        final List<String> phonesList = new ArrayList<>();
//
//        databaseDrivers.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
////                Toast.makeText(LoginActivity.this, dataSnapshot.child("phone").getValue().toString(), Toast.LENGTH_SHORT).show();
//                for(DataSnapshot driverSnapshot : dataSnapshot.getChildren()){
//                    String phone = driverSnapshot.child("phone").getValue().toString();
//                    phonesList.add(phone);
//                }
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        });
//
//        String check = String.valueOf(phonesList.indexOf(driver_phone));
//        if(check == null){
//            return false;
//        }
//        else{
//            return true;
//        }
//    }
}
