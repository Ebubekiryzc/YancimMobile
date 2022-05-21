package tr.edu.duzce.mf.bm.yancimmobile.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

import tr.edu.duzce.mf.bm.yancimmobile.R;

public class SignActivity extends AppCompatActivity {

    private static final String TAG = SignActivity.class.getSimpleName();

    private TextView currentModeText, changeModeTextButton;
    private TextInputLayout usernameWrapper, emailWrapper, passwordWrapper;
    private MaterialButton submitButton;

    private FirebaseAuth authManager;

    private boolean isSignUpMode = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign);
        initComponents();
        changeMode();
        registerEventHandlers();
    }


    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = authManager.getCurrentUser();

        // Buradaki verbose parametresi false, çünkü kullanıcı uygulamayı açtığı anda otomatik giriş yapıyorsa neden ekrana mesaj basalım ki?
        updateUI(currentUser, false);
    }


    private void updateUI(FirebaseUser user, Boolean verbose) {
        if (user != null) {

            // verbose yani detay gösterilsin mi parametresi true olarak verilirse ekrana mesaj basılır.
            if (verbose)
                Toast.makeText(SignActivity.this, String.format("%s, %s",
                        SignActivity.this.getResources().getText(R.string.login_successful),
                        user.getDisplayName()), Toast.LENGTH_SHORT).show();
        }
//        if (user != null) {
//            Intent intent = new Intent(getApplicationContext(), DashboardActivity.class);
//            intent.putExtra("user", user);
//            startActivity(intent);
//        }
    }

    private void initComponents() {
        authManager = FirebaseAuth.getInstance();

        currentModeText = findViewById(R.id.currentModeText);
        changeModeTextButton = findViewById(R.id.changeModeText);
        usernameWrapper = findViewById(R.id.signupUsername);
        emailWrapper = findViewById(R.id.signEmail);
        passwordWrapper = findViewById(R.id.signPassword);
        submitButton = findViewById(R.id.signSubmitButton);

        cleanInputs();
    }

    private void changeMode() {
        if (!isSignUpMode) {
            currentModeText.setText(R.string.have_an_account_text);
            changeModeTextButton.setText(R.string.signin);
            submitButton.setText(R.string.signup);
            usernameWrapper.setVisibility(View.VISIBLE);
            passwordWrapper.setHint(R.string.ask_password);

            cleanInputs();

            isSignUpMode = true;
        } else {
            currentModeText.setText(R.string.do_not_have_an_account_text);
            changeModeTextButton.setText(R.string.signup);
            submitButton.setText(R.string.signin);
            usernameWrapper.setVisibility(View.GONE);
            passwordWrapper.setHint(R.string.password);

            cleanInputs();

            isSignUpMode = false;
        }
    }

    private void executeMainOperation() {
        if (isSignUpMode) {
            signUp();
        } else {
            signIn();
        }
    }

    private void registerEventHandlers() {
        changeModeTextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeMode();
            }
        });

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                executeMainOperation();
            }
        });
    }

    private boolean checkTextLength(String text, int length) {
        if (text.length() >= length) {
            return true;
        }
        return false;
    }

    private boolean checkSignUpInputs() {
        boolean usernameValid = false;
        if (isSignUpMode) {
            usernameValid = checkTextLength(usernameWrapper.getEditText().getText().toString(), 1);
        }
        boolean emailValid = checkTextLength(emailWrapper.getEditText().getText().toString(), 3);
        boolean passwordValid = checkTextLength(passwordWrapper.getEditText().getText().toString(), 6);

        setErrorMessages(usernameValid, emailValid, passwordValid);

        return usernameValid && emailValid && passwordValid;
    }

    private void setErrorMessages(boolean usernameValid, boolean emailValid, boolean passwordValid) {
        if (usernameValid && emailValid && passwordValid) {
            cleanInputs();
            return;
        }
        if (!usernameValid) {
            usernameWrapper.setErrorEnabled(true);
            usernameWrapper.setError(SignActivity.this.getResources().getString(R.string.username_required));
        }
        if (!emailValid) {
            emailWrapper.setErrorEnabled(true);
            emailWrapper.setError(SignActivity.this.getResources().getString(R.string.email_required));
        }
        if (!passwordValid) {
            passwordWrapper.setErrorEnabled(true);
            passwordWrapper.setError(SignActivity.this.getResources().getString(R.string.password_not_valid));
        }
    }


    // Burada errorEnabled kullanılmasının sebebi, sadece setError ile null yaptığımızda arayüzün boşlukları kalıyordu.
    private void cleanInputs() {
        usernameWrapper.setErrorEnabled(false);
        emailWrapper.setErrorEnabled(false);
        passwordWrapper.setErrorEnabled(false);

        usernameWrapper.setError(null);
        emailWrapper.setError(null);
        passwordWrapper.setError(null);
    }

    private void signUp() {
        if (!checkSignUpInputs()) {
            return;
        }

        String username = usernameWrapper.getEditText().getText().toString();
        String mailAddress = emailWrapper.getEditText().getText().toString();
        String password = passwordWrapper.getEditText().getText().toString();

        authManager.createUserWithEmailAndPassword(mailAddress, password)
                .addOnCompleteListener(SignActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = authManager.getCurrentUser();

                            // Task başarılı ise yani kullanıcı kaydedildi ise:
                            UserProfileChangeRequest userProfileChangeRequest = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(username)
                                    .build();

                            // Kullanıcının displayName özelliğini username ile eşitliyoruz:
                            user.updateProfile(userProfileChangeRequest)
                                    // Kullanıcı asenkron olarak güncellendiği için bir complete listener ekliyoruz
                                    // Bu sayede updateUI kısmına user'ı yolladığımızda, kullanıcının username alanı henüz doldurulmamış olmuyor.
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                updateUI(user, true);
                                            }
                                        }
                                    });
                        } else {
                            Log.w(TAG, SignActivity.this.getResources().getString(R.string.signup_failed), task.getException());
                            Toast.makeText(SignActivity.this, SignActivity.this.getResources().getString(R.string.signup_failed), Toast.LENGTH_SHORT).show();
                            updateUI(null, true);
                        }
                    }
                });
    }

    private void signIn() {
        String mailAddress = emailWrapper.getEditText().getText().toString();
        String password = passwordWrapper.getEditText().getText().toString();

        authManager.signInWithEmailAndPassword(mailAddress, password)
                .addOnCompleteListener(SignActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = authManager.getCurrentUser();
                            updateUI(user, true);
                        } else {
                            Log.w(TAG, SignActivity.this.getResources().getString(R.string.signin_failed), task.getException());
                            Toast.makeText(SignActivity.this, SignActivity.this.getResources().getString(R.string.signin_failed), Toast.LENGTH_SHORT).show();
                            updateUI(null, true);
                        }
                    }
                });
    }
}