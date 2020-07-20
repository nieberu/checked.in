package development.software.mobile.checkedin;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;

import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import java.util.HashMap;
import java.util.Map;

import development.software.mobile.checkedin.models.CheckIn;
import development.software.mobile.checkedin.models.Group;
import development.software.mobile.checkedin.models.Member;
import development.software.mobile.checkedin.models.Position;
import development.software.mobile.checkedin.models.Token;
import development.software.mobile.checkedin.models.User;
import development.software.mobile.checkedin.notification.Data;
import development.software.mobile.checkedin.ui.main.SectionsPagerAdapter;
import development.software.mobile.checkedin.util.PushNotification;

public class CreateGroup extends AppCompatActivity implements LocationListener {

    protected LocationManager locationManager;
    protected LocationListener locationListener;
    private DatabaseReference mDatabase;
    Map<LatLng,CheckIn> checkInMap = new HashMap<>();
    private PushNotification pushNotification;;


    private User currentUser;
    private ImageView profile;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        mDatabase = database.getReference();
        pushNotification = PushNotification.builder();
        Intent intent = getIntent();


        profile = findViewById(R.id.profileImageView);

        currentUser = (User)intent.getSerializableExtra("user");
        int position = intent.getIntExtra("tab", 0);
        updateCheckInMap();

        StorageReference sr = FirebaseStorage.getInstance().getReference().child("/profilepictures/"+currentUser.getUid()+"/pp.jpg");

        GlideApp.with(this)
                .load(sr)
                .apply(new RequestOptions().override(200, 200))
                .circleCrop()
                .into(profile);

        profile.setOnClickListener(v -> {
            Intent intent1 = new Intent(getApplicationContext(), Profile.class);
            intent1.putExtra("user", currentUser);
            startActivity(intent1);
        });

        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());
        ViewPager viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(sectionsPagerAdapter);
        viewPager.setCurrentItem(position);
        TabLayout tabs = findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 200);

        }
        else{
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        }

    }

    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == 200){
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        updateLocationFirebase(location);
    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.i("Latitude","disable");
    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.i("Latitude","enable");
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.i("Latitude","status");
    }

    public void updateLocationFirebase(Location location){
        Position pos = new Position(location.getLatitude(), location.getLongitude(), location.getSpeed());
        mDatabase.child("locations").child(currentUser.getUid()).setValue(pos);
        checkLocation(location);
    }

    private void updateCheckInMap(){
        for(String uid: currentUser.getCheckInMap().values()){
            mDatabase.child("checkIn").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    CheckIn checkIn = dataSnapshot.getValue(CheckIn.class);
                    LatLng latLng = checkIn.getLatLng();
                    checkInMap.put(latLng, checkIn);
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

    }

    private void checkLocation(Location location){
        for (LatLng lat: checkInMap.keySet()) {
            float[] results = new float[10];
            Location.distanceBetween(location.getLatitude(), location.getLongitude(), lat.latitude, lat.longitude, results);
            if(results[0] < 500){
                notifyCheckIn(checkInMap.get(lat));

            }
        }
    }

    private void notifyCheckIn(CheckIn checkIn){
        mDatabase.child("groups").child(checkIn.getGroupId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue() != null){
                    Group group = dataSnapshot.getValue(Group.class);
                    for (Member member: group.getMembers()) {
                        if("member".equals(member.getType())){
                            sendNotification(checkIn, member, group.getName());
                        }
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void sendNotification(CheckIn checkIn, Member member, String groupName){
        mDatabase.child("Tokens").child(member.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Token token = dataSnapshot.getValue(Token.class);
                if(token != null){
                    Data data = new Data("CheckIn", currentUser.getEmail() + " has been Checked In "+checkIn.getName()+"!","CheckIn");
                    data.getAdditionalFields().put("name",groupName);
                    pushNotification.sendNotification(token.getToken(),data);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}