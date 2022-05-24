package tr.edu.duzce.mf.bm.yancimmobile.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.splashscreen.SplashScreen;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import tr.edu.duzce.mf.bm.yancimmobile.R;
import tr.edu.duzce.mf.bm.yancimmobile.model.User;

// TODO: Signup mode ilk olarak açılıyor. Bu değişebilir, ilk login gelebilir.
// TODO: İzin istenmesi gerekiyor.
public class SignActivity extends AppCompatActivity {

    private static final String TAG = SignActivity.class.getSimpleName();

    private TextView currentModeText, changeModeTextButton;
    private TextInputLayout usernameWrapper, emailWrapper, passwordWrapper;
    private MaterialButton submitButton;

    private FirebaseAuth authManager;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference usersReference;

    private SharedPreferences sharedPreferences;

    private static final String DARK_MODE_PREFERENCE = "dark_mode_preference";
    private static final String USER_PATH = "users";

    private boolean isSignUpMode = false;
    private boolean isDarkMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign);
        initComponents();
        changeMode();
        registerEventHandlers();
        setCurrentTheme(isDarkMode);
    }


    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = authManager.getCurrentUser();
        if (currentUser != null) {
            usersReference.child(currentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    User snapshotValue = snapshot.getValue(User.class);
                    // Buradaki verbose parametresi false, çünkü kullanıcı uygulamayı açtığı anda otomatik giriş yapıyorsa neden ekrana mesaj basalım ki?
                    updateUI(snapshotValue, false);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        int iconId = getCurrentIcon();

        MenuItem darkModeButton = menu.add("darkModeButton");
        darkModeButton.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        darkModeButton.setIcon(iconId);

        darkModeButton.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                isDarkMode = !isDarkMode;
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean(DARK_MODE_PREFERENCE, isDarkMode);
                editor.apply();

                setCurrentTheme(isDarkMode);

                int iconId = getCurrentIcon();

                menuItem.setIcon(iconId);
                return true;
            }
        });

//        MenuItem signOut = menu.add("logout");
//        signOut.setTitle(R.string.logout);
//        signOut.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);

        return super.onCreateOptionsMenu(menu);
    }

    private int getCurrentIcon() {
        int iconId;
        if (isDarkMode) {
            iconId = R.drawable.ic_day;
        } else {
            iconId = R.drawable.ic_night;
        }
        return iconId;
    }

    private void setCurrentTheme(boolean isDarkMode) {
        if (isDarkMode) {
            getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    private void updateUI(User user, Boolean verbose) {
        if (user != null) {

            // verbose yani detay gösterilsin mi parametresi true olarak verilirse ekrana mesaj basılır.
            if (verbose)
                Toast.makeText(SignActivity.this, String.format("%s, %s",
                        SignActivity.this.getResources().getText(R.string.login_successful),
                        user.getUsername()), Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(SignActivity.this, DashboardActivity.class);
            intent.putExtra("user", user);
            startActivity(intent);
        }
    }

    private void initComponents() {
        authManager = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        usersReference = firebaseDatabase.getReference().getRef().child(USER_PATH);

        currentModeText = findViewById(R.id.currentModeText);
        changeModeTextButton = findViewById(R.id.changeModeText);
        usernameWrapper = findViewById(R.id.signupUsername);
        emailWrapper = findViewById(R.id.signEmail);
        passwordWrapper = findViewById(R.id.signPassword);
        submitButton = findViewById(R.id.signSubmitButton);

        sharedPreferences = getSharedPreferences("common_preferences", MODE_PRIVATE);
        isDarkMode = sharedPreferences.getBoolean(DARK_MODE_PREFERENCE, false);

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
                            User userToDatabase = new User(user.getEmail(), username, "user");

                            usersReference.child(user.getUid()).setValue(userToDatabase).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {

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
                                                        updateUI(userToDatabase, true);
                                                    }
                                                }
                                            });
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
                            usersReference.child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    User snapshotValue = snapshot.getValue(User.class);
                                    updateUI(snapshotValue, true);
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                        } else {
                            Log.w(TAG, SignActivity.this.getResources().getString(R.string.signin_failed), task.getException());
                            Toast.makeText(SignActivity.this, SignActivity.this.getResources().getString(R.string.signin_failed), Toast.LENGTH_SHORT).show();
                            updateUI(null, true);
                        }
                    }
                });
    }
}