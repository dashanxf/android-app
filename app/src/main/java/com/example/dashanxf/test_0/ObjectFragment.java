package com.example.dashanxf.test_0;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.ToggleButton;

/**
 * Created by dashanxf on 2016/9/10.
 */
public class ObjectFragment extends Fragment {

    public static final String ARG_OBJECT = "object";
    public static final String ARG_BTHSERVICE = "bthservice";
    public static View rootView;
    public static TextView editText0,editText1,editText2;
    ToggleButton mOn,tOn;
    private BluetoothLeService mBluetoothLeService;
    private boolean isOn = false;
    private boolean tempOn = false;

    private BluetoothService mBluetoothService;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final Bundle args = getArguments();
        mBluetoothService = (BluetoothService) args.getSerializable(ARG_BTHSERVICE);
        rootView = inflater.inflate(R.layout.activity_ranging, container, false);
        editText0 = (TextView) rootView.findViewById(R.id.rangingText);
        editText1 = (TextView) rootView.findViewById(R.id.dist);
        editText2 = (TextView) rootView.findViewById(R.id.unit);
        mOn = (ToggleButton) rootView.findViewById(R.id.toggle_r);
        mOn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                isOn = !isOn;
                if(isOn){
                    mBluetoothService.connectBLE(1);
                }else{
                    mBluetoothService.connectBLE(2);
                }
            }
        });

        if(args.getInt(ARG_OBJECT)==0) {
            rootView = inflater.inflate(R.layout.activity_audio, container, false);
            ToggleButton music_play = (ToggleButton) rootView.findViewById(R.id.toggle_a);
            music_play.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v){
                    mBluetoothService.playMusic();
                }
            });
        }

        if(args.getInt(ARG_OBJECT)==1) {
            rootView = inflater.inflate(R.layout.activity_temp, container, false);
            tOn = (ToggleButton) rootView.findViewById(R.id.toggle_t);
            tOn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    tempOn = !tempOn;
                    if(tempOn){
                        mBluetoothService.connectBLE(3);
                    }else{
                        mBluetoothService.connectBLE(4);
                    }
                }
            });
        }
        return rootView;
    }

}
