package development.software.mobile.checkedin;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;


import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import development.software.mobile.checkedin.models.Group;
import development.software.mobile.checkedin.models.Member;
import development.software.mobile.checkedin.models.User;

public class MyCustomAdapter extends BaseAdapter implements ListAdapter {

    private FirebaseAuth mAuth;
    private DatabaseReference myRef;
    private List<Member> list = new ArrayList<>();
    private boolean isOwner;
    private Group group;
    private Context context;



    public MyCustomAdapter(List<Member> list, Context context, boolean isOwner, Group group) {
        mAuth = FirebaseAuth.getInstance();
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        myRef = database.getReference();
        this.list = list;
        this.context = context;
        this.isOwner = isOwner;
        this.group = group;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int pos) {
        return list.get(pos);
    }

    @Override
    public long getItemId(int pos) {
        return pos;
        //just return 0 if your list items do not have an Id variable.
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.custom_list_view, null);
        }

        //Handle TextView and display string from your list
        TextView listItemText = (TextView)view.findViewById(R.id.member_email);
        listItemText.setText(list.get(position).getEmail());

        ImageView imageView = view.findViewById(R.id.pp_image);
        StorageReference sr = FirebaseStorage.getInstance().getReference().child("/profilepictures/"+list.get(position).getUid()+"/pp.jpg");


       GlideApp.with(view.getContext())
               .load(sr)
               .apply(new RequestOptions().override(200, 200))
               .circleCrop()
               .into(imageView);

        //Handle buttons and add onClickListeners
        Button removebtn = (Button)view.findViewById(R.id.btn_remove);
        removebtn.setEnabled(isOwner);
        removebtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Map<String, Object> childUpdates = new HashMap<>();
                Member member = list.get(position);
                group.getMembers().remove(position);
                childUpdates.put("/groups/"+group.getUid(),group);
                myRef.updateChildren(childUpdates);
                myRef.child("users").child(member.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        User user = dataSnapshot.getValue(User.class);
                        user.getGroupMap().remove(group.getName());
                        Map<String, Object> childUpdates = new HashMap<>();
                        childUpdates.put("/users/"+user.getUid(),user);
                        myRef.updateChildren(childUpdates);
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        });

        return view;
    }
}
