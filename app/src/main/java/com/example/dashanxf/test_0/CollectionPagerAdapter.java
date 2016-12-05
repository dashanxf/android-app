package com.example.dashanxf.test_0;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.Log;

/**
 * Created by dashanxf on 2016/9/10.
 */
public class CollectionPagerAdapter extends FragmentStatePagerAdapter {

    private BluetoothService mBluetoothService = null;

    public CollectionPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    public void setBluetoothService(BluetoothService mBthService){
        this.mBluetoothService = mBthService;
    }

    @Override
    public Fragment getItem(int i) {
        Fragment fragment = new ObjectFragment();
        Bundle args = new Bundle();

        args.putInt(ObjectFragment.ARG_OBJECT,i); // Our object is just an integer :-P

        args.putSerializable(ObjectFragment.ARG_BTHSERVICE,mBluetoothService);

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        String title = "";
        switch(position){
            case 0:
                title = "Music Player";
                break;
            case 1:
                title = "Temperature";
                break;
            case 2:
                title = "Tracker";
                break;
            default:
                title = "Music Player";
                break;
        }
        return title;
    }
}
