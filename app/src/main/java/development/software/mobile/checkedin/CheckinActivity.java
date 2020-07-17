package development.software.mobile.checkedin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;

import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import development.software.mobile.checkedin.models.CheckIn;
import development.software.mobile.checkedin.models.Group;
import development.software.mobile.checkedin.models.Member;
import development.software.mobile.checkedin.models.Position;
import development.software.mobile.checkedin.models.User;

public class CheckinActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleMap.OnMapClickListener,
        GoogleMap.OnMarkerDragListener {

    private GoogleMap googleMap;
    private FirebaseAuth mAuth;
    private DatabaseReference myRef;
    private User user;
    private Group group;
    private ImageView profile;
    private EditText checkInName;
    private EditText address;
    private Button checkInButton;
    private boolean firstZoomLevel = true;
    private Map<String, Marker> markerMap = new HashMap<>();
    private Geocoder geoCoder;
    private ImageButton search;
    private LatLng currentLatLang;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkin);

        mAuth = FirebaseAuth.getInstance();
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        myRef = database.getReference();
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        profile = findViewById(R.id.profileImageView);
        geoCoder = new Geocoder(getApplicationContext());

        Intent intent = getIntent();
        user = (User) intent.getSerializableExtra("user");
        group = (Group) intent.getSerializableExtra("group");

        checkInName = findViewById(R.id.checkin_name);
        address = findViewById(R.id.address);
        checkInButton = findViewById(R.id.check_in_add);
        search = findViewById(R.id.address_search_btn);

        StorageReference sr = FirebaseStorage.getInstance().getReference().child("/profilepictures/"+user.getUid()+"/pp.jpg");

        GlideApp.with(this)
                .load(sr)
                .apply(new RequestOptions().override(200, 200))
                .circleCrop()
                .into(profile);

        profile.setOnClickListener(v -> {
            Intent intent1 = new Intent(getApplicationContext(), Profile.class);
            intent1.putExtra("user", user);
            startActivity(intent1);
        });
        addMarkers(group.getMembers());

        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String location = address.getText().toString().length() > 0 ? address.getText().toString() :null;
                if(location != null){
                    LatLng latLng = getLatLangFromAddress(location);
                    onMapClick(latLng);
                }

            }
        });

        checkInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = checkInName.getText().toString();
                String location = address.getText().toString();

                if(name.length() > 0 && location.length() > 0 && currentLatLang != null){
                    CheckIn checkIn = new CheckIn();
                    checkIn.setName(name);
                    checkIn.setAddress(location);
                    Map<String, Double> latLng = new HashMap<>();
                    latLng.put("latitude", currentLatLang.latitude);
                    latLng.put("longitude", currentLatLang.longitude);
                    checkIn.setLatLng(latLng);
                    checkIn.setGroupId(group.getUid());
                    checkIn.setUid(UUID.randomUUID().toString());
                    myRef.child("checkIn").child(checkIn.getUid()).setValue(checkIn);
                    updateFirebaseForGroup(group.getUid(), checkIn);
                }else{
                    //TODO : add Toast
                }

            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        googleMap.setOnMapClickListener(this);
        googleMap.setOnMarkerDragListener(this);
    }


    private void addMarkers(List<Member> members){
        for(Member member : members){
            if(member != null && "member".equals(member.getType())){
                myRef.child("locations").child(member.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Position position = dataSnapshot.getValue(Position.class);
                        LatLng latlang = new LatLng(position.getLatitude(), position.getLongitude());
                        Marker marker = googleMap.addMarker(generateMarker(member.getUid(), latlang));
                        Marker precMarker = markerMap.get(member.getUid());
                        if(precMarker != null){
                            precMarker.remove();
                            precMarker.setVisible(false);
                        }
                        markerMap.put(member.getUid(), marker);
                        CameraPosition cameraPosition = new CameraPosition.Builder().target(latlang).zoom(11.0f).build();
                        CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition);
                        if(firstZoomLevel){
                            googleMap.moveCamera(cameraUpdate);
                            firstZoomLevel = false;
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }

            if(member != null && "checkIn".equals(member.getType())){
                myRef.child("checkIn").child(member.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        CheckIn checkIn = dataSnapshot.getValue(CheckIn.class);
                        LatLng latlang = checkIn.getLatLng();
                        Marker marker = googleMap.addMarker(new MarkerOptions().position(latlang).title(checkIn.getName()));
                        markerMap.put(member.getUid(), marker);
                        CameraPosition cameraPosition = new CameraPosition.Builder().target(latlang).zoom(11.0f).build();
                        CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition);
                        if(firstZoomLevel){
                            googleMap.moveCamera(cameraUpdate);
                            firstZoomLevel = false;
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        }
    }

    private MarkerOptions generateMarker(String uid, LatLng latlang){
        View marker = ((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.custom_marker_view, null);
        ImageView imageView = marker.findViewById(R.id.pp_image);
        StorageReference sr = FirebaseStorage.getInstance().getReference().child("/profilepictures/"+uid+"/pp.jpg");
        GlideApp.with(marker.getContext())
                .load(sr)
                .apply(new RequestOptions().override(80, 80))
                .circleCrop()
                .into(imageView);
        MarkerOptions markerOptions = new MarkerOptions()
                .position(latlang)
                .icon(BitmapDescriptorFactory.fromBitmap(createDrawableFromView(getWindowManager(), marker)))
                .zIndex(10f);
        return markerOptions;
    }

    // Convert a view to bitmap
    public static Bitmap createDrawableFromView(WindowManager windowManager, View view) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(displayMetrics);
        view.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        view.measure(displayMetrics.widthPixels, displayMetrics.heightPixels);
        view.layout(0, 0, displayMetrics.widthPixels, displayMetrics.heightPixels);
        view.buildDrawingCache();
        Bitmap bitmap = Bitmap.createBitmap(view.getMeasuredWidth(), view.getMeasuredHeight(), Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);

        return bitmap;
    }

    @Override
    public void onMapClick(LatLng latLng) {
        currentLatLang = latLng;
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.draggable(true);

        // Setting the position for the marker
        markerOptions.position(latLng);

        // Setting the title for the marker.
        // This will be displayed on taping the marker
        markerOptions.title(latLng.latitude + " : " + latLng.longitude);

        // Clears the previously touched position
        googleMap.clear();

        // Animating to the touched position
        CameraPosition cameraPosition = new CameraPosition.Builder().target(latLng).zoom(17.0f).build();
        CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition);
        googleMap.animateCamera(cameraUpdate);;

        // Placing a marker on the touched position
        googleMap.addMarker(markerOptions);
        address.setText(getAddress(latLng));
    }

    @Override
    public void onMarkerDragStart(Marker marker) {

    }

    @Override
    public void onMarkerDrag(Marker marker) {
        googleMap.animateCamera(CameraUpdateFactory.newLatLng(marker.getPosition()));
        address.setText(getAddress(marker.getPosition()));
        currentLatLang = marker.getPosition();
    }

    @Override
    public void onMarkerDragEnd(Marker marker) {

    }

    private String getAddress(LatLng latLng){
        List<Address> matches = null;
        try {
            matches = geoCoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Address bestMatch = (matches.isEmpty() ? null : matches.get(0));
        return bestMatch.getAddressLine(0);
    }

    private LatLng getLatLangFromAddress(String address){
        List<Address> matches = null;
        try {
            matches = geoCoder.getFromLocationName(address,1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Address bestMatch = (matches.isEmpty() ? null : matches.get(0));
        return bestMatch != null ? new LatLng(bestMatch.getLatitude(), bestMatch.getLongitude()) : null;
    }

    private void updateFirebaseForGroup(String groupId, CheckIn checkIn){
        myRef.child("groups").child(groupId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Group group = dataSnapshot.getValue(Group.class);
                group.getMembers().removeAll(Collections.singleton(null));
                Member member = new Member(checkIn.getUid(),checkIn.getName() + "||" + checkIn.getAddress(),"checkIn");
                group.getMembers().add(member);
                Map<String, Object> childUpdates = new HashMap<>();
                childUpdates.put("/groups/"+group.getUid(),group);
                myRef.updateChildren(childUpdates);
                updateFirebaseForUser(group.getMembers(), checkIn);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void updateFirebaseForUser(List<Member> members,CheckIn checkIn){
        for (Member member: members ){
            if("member".equals(member.getType())) {
                myRef.child("users").child(member.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        User user = dataSnapshot.getValue(User.class);
                        user.getCheckInMap().put(checkIn.getName(),checkIn.getUid());
                        Map<String, Object> childUpdates = new HashMap<>();
                        childUpdates.put("/users/"+user.getUid(),user);
                        myRef.updateChildren(childUpdates);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }

        }

    }
}