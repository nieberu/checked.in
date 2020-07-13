package development.software.mobile.checkedin;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

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

import com.google.android.gms.maps.GoogleMap.OnCameraIdleListener;
import com.google.android.gms.maps.GoogleMap.OnCameraMoveCanceledListener;
import com.google.android.gms.maps.GoogleMap.OnCameraMoveListener;
import com.google.android.gms.maps.GoogleMap.OnCameraMoveStartedListener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import development.software.mobile.checkedin.models.CheckIn;
import development.software.mobile.checkedin.models.Group;
import development.software.mobile.checkedin.models.Member;
import development.software.mobile.checkedin.models.Position;
import development.software.mobile.checkedin.models.User;

public class TrackTab extends Fragment implements OnMapReadyCallback,
        OnCameraIdleListener,
        OnCameraMoveCanceledListener,
        OnCameraMoveListener,
        OnCameraMoveStartedListener
{

    private GoogleMap googleMap;
    private FirebaseAuth mAuth;
    private DatabaseReference myRef;
    private Spinner groupNames;
    private String[] groups;
    private User user;
    private boolean firstZoomLevel = true;
    private Context context;
    private MyBroadcastReceiver broadcastReceiver;
    private Map<String, Marker> markerMap = new HashMap<>();


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mAuth = FirebaseAuth.getInstance();
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        myRef = database.getReference();
        broadcastReceiver = new MyBroadcastReceiver();
        return inflater.inflate(R.layout.activity_map,container,false);

    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        context = getActivity().getApplicationContext();


        Intent intent = getActivity().getIntent();
        groupNames = view.findViewById(R.id.group_name_spinner);
        user = (User) intent.getSerializableExtra("user");
        groups = user.getGroupMap().keySet().toArray(new String[user.getGroupMap().keySet().size()]);
        ArrayAdapter<String> adapter = new ArrayAdapter(getContext(), android.R.layout.simple_spinner_dropdown_item, groups);
        groupNames.setAdapter(adapter);
        if(groups.length > 0){
            updateSelection(groups[0]);
        }

        groupNames.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String groupName = groups[position];
                updateSelection(groupName);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    /**
     * Called when the zoom in button (the one with the +) is clicked.
     */
    public void onZoomIn(View view) {
        googleMap.moveCamera(CameraUpdateFactory.zoomIn());
    }

    /**
     * Called when the zoom out button (the one with the -) is clicked.
     */
    public void onZoomOut(View view) {
        googleMap.moveCamera(CameraUpdateFactory.zoomOut());
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;

        googleMap.setOnCameraIdleListener(this);
        googleMap.setOnCameraMoveStartedListener(this);
        googleMap.setOnCameraMoveListener(this);
        googleMap.setOnCameraMoveCanceledListener(this);
        // [START_EXCLUDE silent]
        // We will provide our own zoom controls.
        googleMap.getUiSettings().setZoomControlsEnabled(false);
        googleMap.getUiSettings().setMyLocationButtonEnabled(true);
    }

    private void updateSelection(String groupName){
        myRef.child("groups").child(user.getGroupMap().get(groupName)).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Group group = dataSnapshot.getValue(Group.class);
                addMarkers(group.getMembers());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }



    private void addMarkers(List<Member> members){
        for(Member member : members){
            if(member != null && "member".equals(member.getType())){
                myRef.child("locations").child(member.getUid()).addValueEventListener(new ValueEventListener() {
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
                myRef.child("checkIn").child(member.getUid()).addValueEventListener(new ValueEventListener() {
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
        View marker = ((LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.custom_marker_view, null);
        ImageView imageView = marker.findViewById(R.id.pp_image);
        StorageReference sr = FirebaseStorage.getInstance().getReference().child("/profilepictures/"+uid+"/pp.jpg");
        GlideApp.with(marker.getContext())
                .load(sr)
                .apply(new RequestOptions().override(80, 80))
                .centerCrop()
                .into(imageView);
        MarkerOptions markerOptions = new MarkerOptions()
                .position(latlang)
                .icon(BitmapDescriptorFactory.fromBitmap(createDrawableFromView(getContext(), marker)))
                .zIndex(10f);
        return markerOptions;
    }

    // Convert a view to bitmap
    public static Bitmap createDrawableFromView(Context context, View view) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
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
    public void onCameraIdle() {

    }

    @Override
    public void onCameraMoveCanceled() {

    }

    @Override
    public void onCameraMove() {

    }

    @Override
    public void onCameraMoveStarted(int i) {

    }

    @Override
    public void onStart() {
        super.onStart();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("user_selected");
        intentFilter.addCategory(Intent.CATEGORY_DEFAULT);
        context.registerReceiver(broadcastReceiver, intentFilter);
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    class MyBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String memberid = intent.getStringExtra("memberId");
            Marker marker = markerMap.get(memberid);
            if(marker != null){
                CameraPosition cameraPosition = new CameraPosition.Builder().target(marker.getPosition()).zoom(15.0f).build();
                CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition);
                googleMap.moveCamera(cameraUpdate);
            }

        }
    }
}
