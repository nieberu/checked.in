package development.software.mobile.checkedin.ui.main;

import android.content.Context;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import development.software.mobile.checkedin.CreateGroupTab;
import development.software.mobile.checkedin.JoinGroupTab;
import development.software.mobile.checkedin.MyGroupTab;
import development.software.mobile.checkedin.R;
import development.software.mobile.checkedin.TrackTab;

/**
 * A [FragmentPagerAdapter] that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
public class SectionsPagerAdapter extends FragmentPagerAdapter {

    @StringRes
    private static final int[] TAB_TITLES = new int[]{R.string.track_tab,R.string.my_group,R.string.create_group, R.string.join_group};
    private final Context mContext;

    public SectionsPagerAdapter(Context context, FragmentManager fm) {
        super(fm);
        mContext = context;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0:
                TrackTab tt = new TrackTab();
                return tt;
            case 1:
                MyGroupTab mgt = new MyGroupTab();
                return mgt;
            case 2:
                CreateGroupTab cgt = new CreateGroupTab();
                return  cgt;
            case 3:
                JoinGroupTab jgt = new JoinGroupTab();
                return  jgt;
        }
        return null;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return mContext.getResources().getString(TAB_TITLES[position]);
    }

    @Override
    public int getCount() {
        // Show 2 total pages.
        return 4;
    }
}