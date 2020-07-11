package development.software.mobile.checkedin;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import development.software.mobile.checkedin.models.Group;
import development.software.mobile.checkedin.models.User;

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


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mAuth = FirebaseAuth.getInstance();
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        myRef = database.getReference();
        return inflater.inflate(R.layout.my_group_tab,container,false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        tabhost = (TabLayout) getActivity().findViewById(R.id.tabs);
        Intent intent = getActivity().getIntent();
        groupNames = view.findViewById(R.id.group_name_spinner);
        listView = view.findViewById(R.id.list_item);
        addCheckin = view.findViewById(R.id.add_check_in);
        user = (User) intent.getSerializableExtra("user");
        groups = user.getGroupMap().keySet().toArray(new String[user.getGroupMap().keySet().size()]);
        ArrayAdapter<String> adapter = new ArrayAdapter(getContext(), android.R.layout.simple_spinner_dropdown_item, groups);
        groupNames.setAdapter(adapter);
        if(groups.length > 0){
            currentGroupName = groups[0];
            updateSelection(groups[0]);
        }

        groupNames.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String groupName = groups[position];
                currentGroupName = groupName;
                updateSelection(groupName);
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
                checkInIntent.putExtra("groupName", currentGroupName);
            }
        });
    }

    private void updateSelection(String groupName){
        myRef.child("groups").child(user.getGroupMap().get(groupName)).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Group group = dataSnapshot.getValue(Group.class);
                group.getMembers().removeAll(Collections.singleton(null));
                MyCustomAdapter myCustomAdapter = new MyCustomAdapter(group.getMembers(), getContext(),user.getEmail().equals(group.getOwner()),group,tabhost);
                listView.setAdapter(myCustomAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
