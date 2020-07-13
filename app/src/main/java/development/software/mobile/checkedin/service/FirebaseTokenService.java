package development.software.mobile.checkedin.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import development.software.mobile.checkedin.CreateGroup;
import development.software.mobile.checkedin.R;
import development.software.mobile.checkedin.models.Token;
import development.software.mobile.checkedin.models.User;

public class FirebaseTokenService extends FirebaseMessagingService {

    private FirebaseAuth mAuth;
    private  DatabaseReference myRef;

    public FirebaseTokenService(){
        mAuth = FirebaseAuth.getInstance();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        myRef = database.getReference();
    }

    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
        FirebaseUser firebaseUser=
                FirebaseAuth.getInstance().getCurrentUser();
        String refreshToken= FirebaseInstanceId.getInstance().getToken();
        if(firebaseUser!=null){
            updateToken(refreshToken);
        }
    }
    private void updateToken(String refreshToken){
        FirebaseUser firebaseUser=
                FirebaseAuth.getInstance().getCurrentUser();
        Token token1= new Token(refreshToken);
        FirebaseDatabase.getInstance().getReference("Tokens").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(token1);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            myRef.child("users").child(currentUser.getUid()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    // This method is called once with the initial value and again
                    // whenever data at this location is updated.
                    User  user = dataSnapshot.getValue(User.class);
                    if(user != null){
                        Intent intent = new Intent(getApplicationContext(), CreateGroup.class);
                        intent.putExtra("user", user);
                        if(user.getGroupMap().size() > 0) {
                            intent.putExtra("tab", 0);
                        } else {
                            intent.putExtra("tab", 1);
                        };
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        PendingIntent pendingIntent = PendingIntent.getActivity(FirebaseTokenService.this, 0, intent, 0);
                        String title =remoteMessage.getData().get("title");
                        String message=remoteMessage.getData().get("message");
                        NotificationCompat.Builder builder =
                                new NotificationCompat.Builder(getApplicationContext(), "checkedIn")
                                        .setSmallIcon(R.drawable.logo)
                                        .setContentTitle(title)
                                        .setContentText(message)
                                        .setContentIntent(pendingIntent)
                                        .setAutoCancel(true);
                        NotificationManager manager =
                                (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
                        manager.notify(0, builder.build());
                    }
                }
                @Override
                public void onCancelled(DatabaseError error) {
                    // Failed to read value
                    Log.w("JAY", "Failed to read value.", error.toException());
                }
            });
        }

    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(name.toString(), name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}
