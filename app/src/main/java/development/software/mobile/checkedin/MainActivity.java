package development.software.mobile.checkedin;

//import android.support.v7.app.AppCompatActivity;AppCompatActivity
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

import development.software.mobile.checkedin.models.User;

public class MainActivity extends AppCompatActivity {

    private TextView text;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        User user = (User)getIntent().getSerializableExtra("User");
        //text = findViewById(R.id.mainText);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference myRef = database.getReference();
        final User[] userObject = {null};
        mAuth = FirebaseAuth.getInstance();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        myRef.child("users").child(currentUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                userObject[0] = dataSnapshot.getValue(User.class);
                Log.d("JAY", "Value is: " + userObject[0]);
                Intent intent = new Intent(getApplicationContext(), CreateGroup.class);
                intent.putExtra("user", userObject[0]);
                if(userObject[0].getGroupNames().size() > 0) {
                    intent.putExtra("tab", 0);
                    intent.putExtra("groupNames", groupName(userObject[0].getGroupNames()));
                } else {
                    intent.putExtra("tab", 1);
                }
                startActivity(intent);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w("JAY", "Failed to read value.", error.toException());
            }
        });


    }

    private String groupName(List<String> groupNames)  {
        String s = "";
        for(String groupName : groupNames){
            s = s + groupName + "\n";
        }
        return s.substring(0, s.length() - 1);
    }


}
