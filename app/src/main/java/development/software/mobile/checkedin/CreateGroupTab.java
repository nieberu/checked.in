package development.software.mobile.checkedin;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
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

import java.util.ArrayList;
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
import development.software.mobile.checkedin.notification.Client;
import development.software.mobile.checkedin.notification.Data;
import development.software.mobile.checkedin.service.NotifictionService;
import development.software.mobile.checkedin.util.Hashids;
import development.software.mobile.checkedin.util.MailSender;
import development.software.mobile.checkedin.util.PushNotification;

public class CreateGroupTab extends Fragment {

    private FirebaseAuth mAuth;
    private DatabaseReference myRef;
    private List<String> emailAddressList;
    private Map<String, String> emailMap;
    private AutoCompleteTextView friends;
    private Button createGroupButton;
    private EditText groupNameText;
    private Hashids hashids = new Hashids("CheckedInrandomGroupKey",6);
    private Random random = new Random();
    private User currentUser;
    private PushNotification pushNotification;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mAuth = FirebaseAuth.getInstance();
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        myRef = database.getReference();
        emailAddressList = new ArrayList<>();
        emailMap = new HashMap<>();
        pushNotification = PushNotification.builder();
        myRef.child("users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
               for (DataSnapshot data : dataSnapshot.getChildren()){
                   User user = data.getValue(User.class);
                   emailAddressList.add(user.getEmail());
                   emailMap.put(user.getEmail(), user.getUid());
               }
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), R.layout.simple_list_item_1, emailAddressList);
                friends.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        return inflater.inflate(R.layout.create_group_tab,container,false);


    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        Intent intent = getActivity().getIntent();
        currentUser = (User) intent.getSerializableExtra("user");
        friends = (AutoCompleteTextView) view.findViewById(R.id.friends);
        createGroupButton = (Button) view.findViewById(R.id.btn_create);
        groupNameText = (EditText) view.findViewById(R.id.group_name) ;
        createGroupButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View v) {
                if(validateMembers()){
                    String members = currentUser.getEmail() + "\n" + friends.getText().toString();
                    List<Member> memberList = new ArrayList<>();
                    for (String email : Arrays.asList(members.split("\n"))){
                        memberList.add(new Member(emailMap.get(email), email, "member"));
                    }
                    List<Member> onlyOwner = new ArrayList<>();
                    onlyOwner.add(memberList.get(0));
                    Group group = new Group(UUID.randomUUID().toString(),groupNameText.getText().toString(),
                            onlyOwner ,currentUser.getEmail(),
                            hashids.encode((random.nextInt(10) + 1),(random.nextInt(10) + 1), (random.nextInt(10) + 1)));
                    updateFireBase(group);


                    for(int i=0; i<memberList.size(); i++){
                        int finalI = i;
                        Member member = memberList.get(i);
                        new Thread(new Runnable() {

                            @Override
                            public void run() {
                                try {
                                    if(member.getEmail() != currentUser.getEmail()){
                                        MailSender sender = new MailSender("checked.in2020@gmail.com",
                                                "Checked.In2020");
                                        sender.sendMail("checked.in2020@gmail.com", memberList.get(finalI).getEmail(), group, currentUser);
                                        myRef.child("Tokens").child(memberList.get(finalI).getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                Token token = dataSnapshot.getValue(Token.class);
                                                if(token != null){
                                                    Data data = new Data("Join Group", "You have been invited to a group "+group.getName()+"!","JoinGroup");
                                                    data.getAdditionalFields().put("name",group.getName());
                                                    data.getAdditionalFields().put("email",group.getOwner());
                                                    data.getAdditionalFields().put("key",group.getKey());
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


                    Toast.makeText(getActivity(), "Members invited to group!", Toast.LENGTH_SHORT).show();
                    TabLayout tabhost = (TabLayout) getActivity().findViewById(R.id.tabs);
                    tabhost.getTabAt(0).select();
                }
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private boolean validateMembers(){
        String[] members = friends.getText().toString().split("\n");
        String missingMembers = "";
        for (String member : members){
            if(!emailAddressList.contains(member)){
                missingMembers  = missingMembers + member + ",";
            }
        }
        if(missingMembers.length() > 0){
            Toast.makeText(getContext(), "Members not found " + missingMembers.substring(0, missingMembers.length() - 1),
                    Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void updateFireBase(final Group group){
        currentUser.getGroupMap().put(group.getName(), group.getUid());
        myRef.child("groups").child(group.getUid()).setValue(group);
        for(Member member : group.getMembers()){
            myRef.child("users").child(member.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    User user = dataSnapshot.getValue(User.class);
                    user.getGroupMap().put(group.getName(), group.getUid());
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
