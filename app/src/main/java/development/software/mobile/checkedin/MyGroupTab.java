package development.software.mobile.checkedin;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import development.software.mobile.checkedin.models.Group;
import development.software.mobile.checkedin.models.Member;
import development.software.mobile.checkedin.models.User;
import development.software.mobile.checkedin.util.ShakeDetector;

public class MyGroupTab extends Fragment{

    private FirebaseAuth mAuth;
    private DatabaseReference myRef;
    private ListView listView;
    private Spinner groupNames;
    private String[] groups;
    private User user;
    private TabLayout tabhost;
    private Button addCheckin;
    private String currentGroupName;
    private Group currentGroup;
    private ListView checkInListView;
    private TextView groupKey;
    private TextView groupKeyLabel;

    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private ShakeDetector mShakeDetector;



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mAuth = FirebaseAuth.getInstance();
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        myRef = database.getReference();

        //Handling shaking detection
        mSensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mShakeDetector = new ShakeDetector();
        mSensorManager.registerListener(mShakeDetector, mAccelerometer, SensorManager.SENSOR_DELAY_UI);
        mShakeDetector.setOnShakeListener(new ShakeDetector.OnShakeListener() {
            @Override
            public void onShake(int count) {
                if(user.getEmail().equals(currentGroup.getOwner())){
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setTitle("Delete group");
                    builder.setMessage("Are you sure you want to delete your group: "+currentGroup.getName()+"?")
                            .setCancelable(false)
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Toast.makeText(getContext(), "Clicked on delete", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                }
                            });

                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                }
                else{
                    Toast.makeText(getContext(), "You are not the owner of this group. You can't delete it", Toast.LENGTH_SHORT).show();
                }
            }
        });

        return inflater.inflate(R.layout.my_group_tab,container,false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        tabhost = (TabLayout) getActivity().findViewById(R.id.tabs);
        Intent intent = getActivity().getIntent();
        groupKey = view.findViewById(R.id.groupKeyTextView);
        groupKeyLabel = view.findViewById(R.id.groupKeyText);
        groupNames = view.findViewById(R.id.group_name_spinner);
        listView = view.findViewById(R.id.list_item);
        checkInListView = view.findViewById(R.id.checkin_list_item);
        addCheckin = view.findViewById(R.id.add_check_in);
        user = (User) intent.getSerializableExtra("user");
        groups = user.getGroupMap().keySet().toArray(new String[user.getGroupMap().keySet().size()]);
        ArrayAdapter<String> adapter = new ArrayAdapter(getContext(), android.R.layout.simple_spinner_dropdown_item, groups);
        groupNames.setAdapter(adapter);
        String groupName = intent.getStringExtra("groupName");
        if(groups.length > 0){
            currentGroupName = groupName == null ? groups[0]: groupName;
            updateSelection(currentGroupName);
        } else {
            groupKey.setVisibility(View.GONE);
            groupKeyLabel.setVisibility(View.GONE);
        }

        groupNames.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String groupName = groups[position];
                currentGroupName = groupName;
                updateSelection(currentGroupName);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        addCheckin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent checkInIntent = new Intent(getActivity().getApplicationContext(), CheckinActivity.class);
                checkInIntent.putExtra("user", user);
                checkInIntent.putExtra("group", currentGroup);
                startActivity(checkInIntent);
            }
        });


    }

    private void updateSelection(String groupName){
        myRef.child("groups").child(user.getGroupMap().get(groupName)).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Group group = dataSnapshot.getValue(Group.class);
                currentGroup = group;
                groupKey.setText(currentGroup.getKey());
                if(!user.getEmail().equals(group.getOwner())){
                    groupKey.setVisibility(View.GONE);
                    groupKeyLabel.setVisibility(View.GONE);
                } else {
                    groupKey.setVisibility(View.VISIBLE);
                    groupKeyLabel.setVisibility(View.VISIBLE);
                }

                group.getMembers().removeAll(Collections.singleton(null));
                List<Member> memberList = new ArrayList<>();
                List<Member> checkInList = new ArrayList<>();
                for (Member member : group.getMembers()){
                    if("member".equals(member.getType())){
                        memberList.add(member);
                    }else{
                        checkInList.add(member);
                    }
                }
                MyCustomAdapter myCustomAdapter = new MyCustomAdapter(memberList, getContext(),user.getEmail().equals(group.getOwner()),group,tabhost);
                listView.setAdapter(myCustomAdapter);
                MyCheckinAdapter myCheckInAdapter = new MyCheckinAdapter(checkInList, getContext(),user.getEmail().equals(group.getOwner()),group,tabhost);
                checkInListView.setAdapter(myCheckInAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void deleteGroup(){
        myRef.child("groups").child(currentGroup.getUid()).removeValue();
    }

    @Override
    public void onPause(){
        mSensorManager.unregisterListener(mShakeDetector);
        super.onPause();
    }

    @Override
    public void onResume(){
        mSensorManager.registerListener(mShakeDetector, mAccelerometer, SensorManager.SENSOR_DELAY_UI);
        super.onResume();
    }
}
