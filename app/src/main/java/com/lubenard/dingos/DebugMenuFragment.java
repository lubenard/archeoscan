package com.lubenard.dingos;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

public class DebugMenuFragment extends Fragment implements View.OnClickListener {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        return inflater.inflate(R.layout.debug_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        WaitScan.setDebugMode(1);

        Button intro = view.findViewById(R.id.debug_launch_intro);
        Button video1 = view.findViewById(R.id.debug_launch_video1);
        Button video2 = view.findViewById(R.id.debug_launch_video2);
        Button video3 = view.findViewById(R.id.debug_launch_video3);
        Button video4 = view.findViewById(R.id.debug_launch_video4);
        Button video5 = view.findViewById(R.id.debug_launch_video5);
        Button video6 = view.findViewById(R.id.debug_launch_video6);
        Button video7 = view.findViewById(R.id.debug_launch_video7);
        Button video8 = view.findViewById(R.id.debug_launch_video8);

        intro.setOnClickListener(this);
        video1.setOnClickListener(this);
        video2.setOnClickListener(this);
        video3.setOnClickListener(this);
        video4.setOnClickListener(this);
        video5.setOnClickListener(this);
        video6.setOnClickListener(this);
        video7.setOnClickListener(this);
        video8.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.debug_launch_intro:
                WaitScan.setShouldQuizzLaunch(1);
                WaitScan.setItemChoice(0, WaitScan.getResArray()[0]);
                break;
            case R.id.debug_launch_video1:
                WaitScan.setShouldQuizzLaunch(1);
                WaitScan.setItemChoice(1, WaitScan.getResArray()[1]);
                break;
        }
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        VideoPlayerFragment fragment = new VideoPlayerFragment();
        fragmentTransaction.replace(android.R.id.content, fragment);
        fragmentTransaction.commit();
    }
}