package tr.edu.duzce.mf.bm.yancimmobile.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseUser;

import tr.edu.duzce.mf.bm.yancimmobile.R;

public class DashboardActivity extends AppCompatActivity {

    private static final String TAG = DashboardActivity.class.getSimpleName();

    private TextView username;
    private TextInputLayout selectCityTextInputLayout;
    private RecyclerView gameTypeRecyclerView, roomRecyclerView;
    private LinearLayout mainLayout;

    private static final String DARK_MODE_PREFERENCE = "dark_mode_preference";
    private boolean isDarkMode;

    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        initComponents();
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

                setMainDashboardColor();
                return true;
            }
        });

        MenuItem signOut = menu.add("logout");
        signOut.setTitle(R.string.logout);
        signOut.setIcon(R.drawable.ic_signout);
        signOut.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);

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

    private void initComponents() {
        username = findViewById(R.id.username);
        selectCityTextInputLayout = findViewById(R.id.selectCityTextInput);
        gameTypeRecyclerView = findViewById(R.id.gameTypesRecyclerView);
        roomRecyclerView = findViewById(R.id.roomRecyclerView);
        mainLayout = findViewById(R.id.mainDashboard);

        sharedPreferences = getSharedPreferences("common_preferences", MODE_PRIVATE);
        isDarkMode = sharedPreferences.getBoolean(DARK_MODE_PREFERENCE, false);

        setMainDashboardColor();

        Intent intent = getIntent();
        FirebaseUser user = intent.getParcelableExtra("user");
        username.setText(user.getDisplayName());
    }

    private void setMainDashboardColor() {
        Drawable mainDashboardBackground = mainLayout.getBackground();

        if (isDarkMode) {
            mainDashboardBackground.setColorFilter(getResources().getColor(R.color.primaryLightColor), PorterDuff.Mode.SRC_ATOP);
        } else {
            mainDashboardBackground.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
        }
    }


}