package development.software.mobile.checkedin;

import android.content.Context;
import android.content.Intent;
import android.text.Spannable;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.tabs.TabLayout;
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

public class MyCheckinAdapter extends BaseAdapter implements ListAdapter {

    private FirebaseAuth mAuth;
    private DatabaseReference myRef;
    private List<Member> list = new ArrayList<>();
    private boolean isOwner;
    private Group group;
    private Context context;
    private TabLayout tabHost;



    public MyCheckinAdapter(List<Member> list, Context context, boolean isOwner, Group group, TabLayout tabHost) {
        mAuth = FirebaseAuth.getInstance();
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        myRef = database.getReference();
        this.list = list;
        this.context = context;
        this.isOwner = isOwner;
        this.group = group;
        this.tabHost = tabHost;
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
            view = inflater.inflate(R.layout.checkin_list_view, null);
        }

        //boolean accepted = checkRequest(list.get(position).getUid(), group);
        //Handle TextView and display string from your list
        TextView listItemText = (TextView)view.findViewById(R.id.check_in_name);

        TextView addressTest = (TextView)view.findViewById(R.id.check_in_address);

        /*if(list.get(position).getEmail().equals(group.getOwner())) {
            listItemText.setText(list.get(position).getEmail());
        }
        else {
            listItemText.setText(list.get(position).getEmail() + "  Pending");
        }*/

        listItemText.setText(list.get(position).getEmail().split("\\|\\|")[0]);
        listItemText.setMovementMethod(LinkMovementMethod.getInstance());

        addressTest.setText(list.get(position).getEmail().split("\\|\\|")[1]);

        Spannable spans = (Spannable) listItemText.getText();
        ClickableSpan clickSpan = new ClickableSpan() {

            @Override
            public void onClick(View widget)
            {
                Intent intent = new Intent();
                intent.putExtra("memberId",list.get(position).getUid());
                intent.addCategory(Intent.CATEGORY_DEFAULT);
                intent.setAction("user_selected");
                context.sendBroadcast(intent);
                tabHost.getTabAt(0).select();
            }
        };
        spans.setSpan(clickSpan, 0, spans.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        return view;
    }
}