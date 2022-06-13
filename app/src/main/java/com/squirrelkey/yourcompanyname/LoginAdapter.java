package com.squirrelkey.yourcompanyname;

import android.content.Context;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

public class LoginAdapter extends FragmentPagerAdapter {
    private Context context;
    int totalTabs;
    public LoginAdapter(FragmentManager fm, Context context, int totalTabs){
        super(fm);
        this.context = context;
        this.totalTabs = totalTabs;
    }

    @Override
    public int getCount() {
        return totalTabs;
    }

    public Fragment getItem(int pos){
        switch (pos){
            case 0 : com.squirrelkey.yourcompanyname.LoginFrag loginFrag  = new com.squirrelkey.yourcompanyname.LoginFrag();
                return loginFrag;
            case 1 : com.squirrelkey.yourcompanyname.SignupFrag signupFrag = new com.squirrelkey.yourcompanyname.SignupFrag();
                return signupFrag;
            default:return null;
        }
    }
}
