package development.software.mobile.checkedin;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import development.software.mobile.checkedin.models.Group;
import development.software.mobile.checkedin.models.Member;
import development.software.mobile.checkedin.models.Token;
import development.software.mobile.checkedin.models.User;
import development.software.mobile.checkedin.notification.Data;
import development.software.mobile.checkedin.util.Hashids;
import development.software.mobile.checkedin.util.MailSender;
import development.software.mobile.checkedin.util.PushNotification;

public class JoinGroupTab extends Fragment {

    private FirebaseAuth mAuth;
    private DatabaseReference myRef;
    private Button joinButton;
    private Button cancelButton;
    private EditText groupNameText;
    private EditText groupKeyText;
    private EditText ownerEmailText;
    private User currentUser;
    private PushNotification pushNotification;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mAuth = FirebaseAuth.getInstance();
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        myRef = database.getReference();
        pushNotification = PushNotification.builder();
        return inflater.inflate(R.layout.join_group_tab,container,false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        Intent intent = getActivity().getIntent();
        currentUser = (User) intent.getSerializableExtra("user");

        groupNameText = (EditText) view.findViewById(R.id.group_name) ;

        /* set group name for Join Request */
        String groupName = intent.getStringExtra("groupName");
        if(groupName != null){
            groupNameText.setText(groupName);
        }
        groupKeyText = (EditText) view.findViewById(R.id.group_key) ;

        /* set key for Join Request */
        String key = intent.getStringExtra("key");
        if(key != null){
            groupKeyText.setText(key);
        }
        ownerEmailText = (EditText) view.findViewById(R.id.owner_email) ;

        /* set Owner Email for Join Request */
        String email = intent.getStringExtra("email");
        if(email != null){
            ownerEmailText.setText(email);
        }

        joinButton = (Button) view.findViewById(R.id.btn_join);
        joinButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View v) {
               myRef.child("groups").orderByChild("key").equalTo(groupKeyText.getText().toString()).addListenerForSingleValueEvent(new ValueEventListener() {
                   @Override
                   public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                       try{
                           for(DataSnapshot data : dataSnapshot.getChildren()){
                               Group group = data.getValue(Group.class);
                               if(group != null){
                                   if(group.getOwner().equals(ownerEmailText.getText().toString()) &&
                                           group.getName().equals(groupNameText.getText().toString())){
                                       updateFireBase(group);
                                       TabLayout tabhost = (TabLayout) getActivity().findViewById(R.id.tabs);
                                       tabhost.getTabAt(0).select();
                                   }else{
                                       Toast.makeText(getContext(), "Group not found ",
                                               Toast.LENGTH_SHORT).show();
                                   }
                               }
                           }
                       }catch(Exception e){
                           Toast.makeText(getContext(), "Group not found ",
                                   Toast.LENGTH_SHORT).show();
                       }
                   }

                   @Override
                   public void onCancelled(@NonNull DatabaseError databaseError) {

                   }
               });
            }
        });

        cancelButton = (Button) view.findViewById(R.id.btn_cancel);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TabLayout tabhost = (TabLayout) getActivity().findViewById(R.id.tabs);
                tabhost.getTabAt(0).select();
            }
        });
    }

    private void updateFireBase(Group group){
        group.getMembers().add(new Member(currentUser.getUid(), currentUser.getEmail(), "member"));
        currentUser.getGroupMap().put(group.getName(), group.getUid());
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/groups/"+group.getUid(),group);
        childUpdates.put("/users/"+currentUser.getUid(),currentUser);
        myRef.updateChildren(childUpdates);
        for(int i=0; i<group.getMembers().size(); i++){
            int finalI = i;
            Member member = group.getMembers().get(i);
            new Thread(new Runnable() {

                @Override
                public void run() {
                    try {
                        if("member".equals(member.getType())){
                            myRef.child("Tokens").child(member.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    Token token = dataSnapshot.getValue(Token.class);
                                    if(token != null){
                                        Data data = new Data("Join Group", currentUser.getFirstName() + currentUser.getLastName() + " has been Joined in group "+group.getName()+"!","MemberAdded");
                                        data.getAdditionalFields().put("name",group.getName());
                                        pushNotification.sendNotification(token.getToken(),data);
                                    }
                                }
                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                        }
                    } catch (Exception e) {
                        Log.i("SendMail", e.getMessage(), e);
                    }
                }

            }).start();
        }
    }
}
