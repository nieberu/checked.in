package development.software.mobile.checkedin;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.IntentCompat;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import development.software.mobile.checkedin.models.User;

public class Profile extends AppCompatActivity {

    private ImageView backIcon, profileImage;
    private User currentUser;
    private TextView name, groupCount, email, phone, username, group;
    private Button logout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        backIcon = findViewById(R.id.backIcon);
        backIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        Intent intent = getIntent();
        currentUser = (User)intent.getSerializableExtra("user");

        name = findViewById(R.id.tv_name);
        name.setText(currentUser.getFirstName() + " " + currentUser.getLastName());

        groupCount = findViewById(R.id.groupCount);
        groupCount.setText(currentUser.getGroupMap().size()+"");

        email = findViewById(R.id.userEmail);
        email.setText(currentUser.getEmail());

        phone = findViewById(R.id.userPhone);
        phone.setText(currentUser.getPhoneNumber()+"");

        username = findViewById(R.id.username);
        username.setText(currentUser.getUserName());

        group = findViewById(R.id.groupText);
        if(currentUser.getGroupMap().size() == 1)
            group.setText("Group");

        profileImage = findViewById(R.id.pp);
        StorageReference sr = FirebaseStorage.getInstance().getReference().child("/profilepictures/"+currentUser.getUid()+"/pp.jpg");
        GlideApp.with(this)
                .load(sr)
                .circleCrop()
                .into(profileImage);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        AlertDialog alertDialog = builder.create();

        logout = findViewById(R.id.logoutButton);
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                builder.setTitle("Logout?")
                        .setMessage("Do you really want to log out?")
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton(android.R.string.yes,
                                (dialog, which) -> {
                                    FirebaseAuth.getInstance().signOut();
                                    Intent intent = new Intent(getApplicationContext(), Login.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                                            | Intent.FLAG_ACTIVITY_CLEAR_TOP
                                            | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent);
                                    finish();
                                })
                        .setNegativeButton(android.R.string.cancel, null)
                        .show();

            }
        });
    }
}