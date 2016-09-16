package com.example.dashanxf.test_0;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

/**
 * Created by dashanxf on 2016/9/10.
 */
public class ObjectFragment extends Fragment {

    public static final String ARG_OBJECT = "object";
    public static final String ARG_BTHSERVICE = "bthservice";
    public static View rootView;
    public static TextView editText0,editText1,editText2;
    ImageButton mOn,mOff,tOn;
    private BluetoothLeService mBluetoothLeService;

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
        mOn = (ImageButton) rootView.findViewById(R.id.buttonOn);
        mOff = (ImageButton) rootView.findViewById(R.id.buttonOff);
        mOn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mBluetoothService.connectBLE(1);
            }
        });
        mOff.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mBluetoothService.connectBLE(2);
            }
        });

        if(args.getInt(ARG_OBJECT)==0) {
            rootView = inflater.inflate(R.layout.activity_audio, container, false);
            ImageButton music_play = (ImageButton) rootView.findViewById(R.id.on);
            music_play.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v)
                {
                    mBluetoothService.playMusic();
                }
            });
        }

        if(args.getInt(ARG_OBJECT)==1) {
            rootView = inflater.inflate(R.layout.activity_temp, container, false);
            tOn = (ImageButton) rootView.findViewById(R.id.button_temp_On);
            tOn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mBluetoothService.connectBLE(3);
                }
            });
        }
        return rootView;
    }

}
