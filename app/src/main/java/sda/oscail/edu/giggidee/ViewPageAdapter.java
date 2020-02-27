package sda.oscail.edu.giggidee;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

public class ViewPageAdapter extends FragmentPagerAdapter{

    private Context context;

    ViewPageAdapter(FragmentManager fm,int behavior, Context nContext) {
        super(fm, behavior);
        context = nContext;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {

        Fragment fragment = new Fragment();

        //finds the tab position (note array starts at 0)
        position = position+1;

        //finds the fragment
        switch (position)
        {
            case 1:
                //code
                fragment = new Noticeboard();
                break;
            case 2:
                //code
                fragment = new Chat();
                break;
            case 3:
                //code
                fragment = new Roster();
                break;
        }

        return fragment;
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        position = position+1;

        CharSequence tabTitle = "";

        //finds the fragment
        switch (position)
        {
            case 1:
                //code
                tabTitle = "NOTICEBOARD";
                break;
            case 2:
                //code
                tabTitle = "CHAT";
                break;
            case 3:
                //code
                tabTitle = "ROSTER";
                break;
        }

        return tabTitle;
    }
}
