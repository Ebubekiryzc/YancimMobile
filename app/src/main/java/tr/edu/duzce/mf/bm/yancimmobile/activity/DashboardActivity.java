package tr.edu.duzce.mf.bm.yancimmobile.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionButton;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import tr.edu.duzce.mf.bm.yancimmobile.R;
import tr.edu.duzce.mf.bm.yancimmobile.adapter.GameTypeAdapter;
import tr.edu.duzce.mf.bm.yancimmobile.adapter.GameTypeSpinnerAdapter;
import tr.edu.duzce.mf.bm.yancimmobile.helpers.abstracts.ItemClickListener;
import tr.edu.duzce.mf.bm.yancimmobile.model.GameType;
import tr.edu.duzce.mf.bm.yancimmobile.model.Room;
import tr.edu.duzce.mf.bm.yancimmobile.model.User;

// TODO: B??t??n itemlerde d??n isme g??re ??yle sorgula.
// TODO: Resim y??klemesini kontrol et, bitince kaydet ??al????s??n.
public class DashboardActivity extends AppCompatActivity {

    private static final String TAG = DashboardActivity.class.getSimpleName();

    private TextView username;
    private RecyclerView gameTypeRecyclerView, roomRecyclerView;
    private FloatingActionButton roomCreateButton, gameTypeCreateButton;
    private LinearLayout mainLayout;
    private ProgressBar gameTypeProgressBar;

    private User user;
    private FirebaseUser firebaseUser;

    // Buras?? a????lacak modallardaki de??i??kenleri tutmak i??in kullan??lan de??i??kenler
    private TextInputLayout gameTypeNameWrapper;
    private MaterialButton selectGameTypeImageButton;

    private static final String DARK_MODE_PREFERENCE = "dark_mode_preference";
    private static final String GAME_TYPE_NODE = "gameTypes";
    private boolean isDarkMode;

    // Resim se??me
    private static final int PICK_IMAGE_REQUEST = 4813;
    private Uri resourceURI;
    private String uploadedResourcePath;

    // Firebase
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference gameTypePath;
    private FirebaseStorage firebaseStorage;
    private StorageReference imagePath;

    // Eklenecek Oyun Tipi ve Oda i??in referans:
    private GameType gameTypeToAdd;

    // T??m oyun tipi ve odalar?? tutan referanslar:
    private List<GameType> gameTypes;
    private List<Room> rooms;

    // Adapters
    private GameTypeAdapter gameTypeAdapter;
    private GameTypeSpinnerAdapter gameTypeSpinnerAdapter;


    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        initComponents();
        setCurrentTheme(isDarkMode);
        loadData();
        registerEventHandlers();
    }

    private void loadData() {
        loadGameTypes();
    }

    private void loadGameTypes() {
        gameTypeRecyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(DashboardActivity.this);
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        gameTypeRecyclerView.setLayoutManager(linearLayoutManager);

        gameTypes = new ArrayList<>();
        ItemClickListener itemClickListener = new ItemClickListener() {
            @Override
            public void onItemClick(int position) {
                Toast.makeText(DashboardActivity.this, gameTypes.get(position).getName() + "TIKLANDI", Toast.LENGTH_SHORT).show();
            }
        };

        gameTypeAdapter = new GameTypeAdapter(DashboardActivity.this, gameTypes, itemClickListener);
        gameTypeRecyclerView.setAdapter(gameTypeAdapter);

        gameTypePath.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    GameType gameType = dataSnapshot.getValue(GameType.class);
                    gameTypes.add(gameType);
                }
                gameTypeAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
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
        signOut.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(DashboardActivity.this, SignActivity.class);
                startActivity(intent);
                finish();
                return true;
            }
        });

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

    private void setMainDashboardColor() {
        Drawable mainDashboardBackground = mainLayout.getBackground();

        if (isDarkMode) {
            mainDashboardBackground.setColorFilter(getResources().getColor(R.color.primaryLightColor), PorterDuff.Mode.SRC_ATOP);
        } else {
            mainDashboardBackground.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
        }
    }

    private void initComponents() {
        // Aktivite de??i??kenleri
        username = findViewById(R.id.username);
        gameTypeRecyclerView = findViewById(R.id.gameTypesRecyclerView);
        roomRecyclerView = findViewById(R.id.roomRecyclerView);
        mainLayout = findViewById(R.id.mainDashboard);
        roomCreateButton = findViewById(R.id.roomCreateButton);
        gameTypeCreateButton = findViewById(R.id.gameTypeCreateButton);

        // Firebase k??sm??
        firebaseDatabase = FirebaseDatabase.getInstance();
        gameTypePath = firebaseDatabase.getReference(GAME_TYPE_NODE);
        firebaseStorage = FirebaseStorage.getInstance();
        imagePath = firebaseStorage.getReference();

        // Preferences
        sharedPreferences = getSharedPreferences("common_preferences", MODE_PRIVATE);
        isDarkMode = sharedPreferences.getBoolean(DARK_MODE_PREFERENCE, false);

        setMainDashboardColor();

        Intent intent = getIntent();
        User user = (User) intent.getSerializableExtra("user");
        username.setText(user.getUsername());

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        if (user.getRole().equals("admin")) {
            gameTypeCreateButton.setVisibility(View.VISIBLE);
        }
    }


    private void registerEventHandlers() {
        gameTypeCreateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createNewGameType();
            }
        });
        roomCreateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createNewRoom();
            }
        });
    }

    private void createNewRoom() {
        AlertDialog.Builder builder = new AlertDialog.Builder(DashboardActivity.this)
                .setTitle(DashboardActivity.this.getResources().getString(R.string.create_room_text));

        LayoutInflater layoutInflater = DashboardActivity.this.getLayoutInflater();
        View addRoomView = layoutInflater.inflate(R.layout.add_new_room, null);

        GameTypeSpinnerAdapter gameTypeSpinnerAdapter = new GameTypeSpinnerAdapter(DashboardActivity.this, gameTypes);

        Spinner gameTypeSpinner = addRoomView.findViewById(R.id.gameTypeSpinner);
        gameTypeSpinner.setAdapter(gameTypeSpinnerAdapter);

        builder.setView(addRoomView)
                .setIcon(R.drawable.ic_room);

        builder.setPositiveButton(DashboardActivity.this.getResources().getString(R.string.add), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // Veri taban??na GameType aktarma
                if (gameTypeToAdd != null) {
                    gameTypePath.push().setValue(gameTypeToAdd);
                    Toast.makeText(DashboardActivity.this, R.string.game_type_added_successfully, Toast.LENGTH_SHORT).show();
                }
                dialogInterface.dismiss();
            }
        });

        builder.setNegativeButton(DashboardActivity.this.getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //Sonra kodlanacak
                dialogInterface.dismiss();
            }
        });

        builder.show();
    }

    private void createNewGameType() {
        AlertDialog.Builder builder = new AlertDialog.Builder(DashboardActivity.this)
                .setTitle(DashboardActivity.this.getResources().getString(R.string.create_game_type_text));

        LayoutInflater layoutInflater = DashboardActivity.this.getLayoutInflater();
        View addGameTypeView = layoutInflater.inflate(R.layout.add_new_game_type, null);

        gameTypeNameWrapper = addGameTypeView.findViewById(R.id.newGameTypeEditText);
        selectGameTypeImageButton = addGameTypeView.findViewById(R.id.selectGameTypeImageButton);
        gameTypeProgressBar = addGameTypeView.findViewById(R.id.gameTypeProgressBar);

        // Referans?? yeniliyoruz:
        gameTypeToAdd = new GameType();

        selectGameTypeImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (Build.VERSION.SDK_INT >= 22) {
                    checkAndRequestForSelectImagePermission();
                } else {
                    selectImage();
                }
            }
        });
        builder.setView(addGameTypeView)
                .setIcon(R.drawable.ic_game_type);

        builder.setPositiveButton(DashboardActivity.this.getResources().getString(R.string.add), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                gameTypeToAdd.setName(gameTypeNameWrapper.getEditText().getText().toString());
                // Veri taban??na GameType aktarma
                if (gameTypeToAdd.getName() != null) {
                    gameTypePath.push().setValue(gameTypeToAdd);
                    Toast.makeText(DashboardActivity.this, R.string.game_type_added_successfully, Toast.LENGTH_SHORT).show();
                }
                dialogInterface.dismiss();
            }
        });

        builder.setNegativeButton(DashboardActivity.this.getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        builder.show();
    }

    private void checkAndRequestForSelectImagePermission() {
        String permission = Manifest.permission.READ_EXTERNAL_STORAGE;
        if (ContextCompat.checkSelfPermission(DashboardActivity.this, permission) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(DashboardActivity.this, permission)) {
                Toast.makeText(DashboardActivity.this, DashboardActivity.this.getResources().getString(R.string.imageRationaleText), Toast.LENGTH_SHORT).show();
            } else {
                ActivityCompat.requestPermissions(DashboardActivity.this, new String[]{permission}, PICK_IMAGE_REQUEST);
            }
        } else {
            selectImage();
        }
    }

    private void selectImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, DashboardActivity.this.getResources().getString(R.string.select_image)), PICK_IMAGE_REQUEST);
    }

    public void uploadGameTypeImage() {
        if (resourceURI != null) {
            // Resim y??klemeye ba??lay??nca progress bar a??
            gameTypeProgressBar.setVisibility(View.VISIBLE);

            // Resmin ad??n?? rastgele bir de??er at??yoruz.
            String imageName = UUID.randomUUID().toString();

            // T??m klas??rlerden images klas??r??n?? elde etmek i??in atama yap??yoruz.
            StorageReference imageFile = imagePath.child(String.format("images/%s", imageName));

            imageFile.putFile(resourceURI)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            // Dosya upload i??lemi bitince indirme linki olu??tur:
                            imageFile.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    // Bitince progress bar kapat
                                    gameTypeProgressBar.setVisibility(View.INVISIBLE);

                                    // Oyun tipini veri taban??na ekle:
                                    gameTypeToAdd.setImage(uri.toString());
                                }
                            });
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // Ba??ar??s??z olursa da dialog kapat??l??r
                            gameTypeProgressBar.setVisibility(View.INVISIBLE);
                            Toast.makeText(DashboardActivity.this, R.string.upload_failed, Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    // TODO: Resim kald??r butonu eklenebilir.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST) {
            if (resultCode == RESULT_OK) {
                if (data != null) {
                    if (data.getData() != null) {
                        resourceURI = data.getData();
                        selectGameTypeImageButton.setText(R.string.selected_text);
                        selectGameTypeImageButton.setIcon(ContextCompat.getDrawable(DashboardActivity.this, R.drawable.ic_check));
                        selectGameTypeImageButton.setBackgroundColor(DashboardActivity.this.getResources().getColor(R.color.secondaryDarkColor, null));
                        uploadGameTypeImage();
                    }
                }
            }
        }
    }
}