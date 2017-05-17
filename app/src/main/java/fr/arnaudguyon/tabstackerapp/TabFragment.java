/*
    Copyright 2016 Arnaud Guyon

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
 */
package fr.arnaudguyon.tabstackerapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import fr.arnaudguyon.tabstacker.SharedTransitionInfo;
import fr.arnaudguyon.tabstacker.TabStacker;

/**
 * Example of Fragment using TabStacker.
 * It is constructed using the createInstance() pattern with arguments
 * The View hierarchy is saved and restored.
 * The title is displayed at a random place
 */

public class TabFragment extends Fragment implements TabStacker.TabStackInterface, SharedTransitionInfo.TransitionInterface {

    private static final String TAG = "TabFragment";

    private static final String ARGUMENT_TITLE = "title";
    private static final String ARGUMENT_COLOR = "color";
    private static final String ARGUMENT_RANDOM_TOP = "randomTop";

    // a reference to the view must be kept so that the view can be saved
    private View mView;

    private ArrayList<SharedTransitionInfo> mSharedTransitionInfos = new ArrayList<>();

    // constructor
    public static TabFragment createInstance(String title, int color) {
        TabFragment fragment = new TabFragment();
        Bundle bundle = new Bundle();
        bundle.putString(ARGUMENT_TITLE, title);
        bundle.putInt(ARGUMENT_COLOR, color);
        bundle.putFloat(ARGUMENT_RANDOM_TOP, (float) (Math.random() * 0.6 + 0.2));
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.testfragment, container, false);

        addSharedTransition(mView.findViewById(R.id.sharedElement), "iconTransition");

        return mView;
    }

    private void addSharedTransition(View sourceView, String destTransitionName) {
        ViewCompat.setTransitionName(sourceView, destTransitionName);
        mSharedTransitionInfos.add(new SharedTransitionInfo(sourceView, destTransitionName));
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle arguments = getArguments();

        String title = arguments.getString(ARGUMENT_TITLE);
        int color = arguments.getInt(ARGUMENT_COLOR);
        float randomTop = arguments.getFloat(ARGUMENT_RANDOM_TOP);

        // SET BACKGROUND RANDOM COLOR
        view.setBackgroundColor(color);

        // SET FRAGMENT TITLE
        TextView titleView = (TextView) view.findViewById(R.id.titleView);
        titleView.setText(title);
        centerTitle(randomTop);
        titleView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isDetached()) {
                    MainActivity activity = (MainActivity) getActivity();
                    activity.onTitlePressed();
                }
            }
        });

        // Asks the MainActivity to restore the View hierarchy (the activity holds the TabStacker)
        MainActivity activity = (MainActivity) getActivity();
        activity.restoreView(this, view);

        // Start Activity button
        View activityButton = view.findViewById(R.id.activityButton);
        activityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String url = "https://play.google.com/store/apps/details?id=fr.smartfun.pingoo";
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));
                startActivity(intent);
            }
        });
    }

    private void centerTitle(float topValue) {

        {
            View topView = getView().findViewById(R.id.topView);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            params.weight = topValue;
            topView.setLayoutParams(params);
        }

        {
            float bottomValue = 1 - topValue;
            View bottomView = getView().findViewById(R.id.bottomView);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            params.weight = bottomValue;
            bottomView.setLayoutParams(params);
        }

    }

    // Called when a Fragment is presented on Screen
    @Override
    public void onTabFragmentPresented(TabStacker.PresentReason reason) {
        // Logs the Reason and Fragment name
        Bundle arguments = getArguments();
        String title = arguments.getString(ARGUMENT_TITLE);
        Log.i(TAG, "PRESENT " + title + " (" + reason.name() + ")");
    }

    // Called when a Fragment is dismissed from Screen
    @Override
    public void onTabFragmentDismissed(TabStacker.DismissReason reason) {
        // Logs the Reason and Fragment name
        Bundle arguments = getArguments();
        String title = arguments.getString(ARGUMENT_TITLE);
        Log.i(TAG, "DISMISS " + title + " (" + reason.name() + ")");
    }

    // called when it's time to save some precious data
    @Override
    public View onSaveTabFragmentInstance(Bundle outState) {
        // You can add here some precious data to be saved in the bundle
        // getView() is always null, so needs to keep a reference so that the view values can be restored correctly later
        return mView;
    }

    // called to restore your precious data
    @Override
    public void onRestoreTabFragmentInstance(Bundle savedInstanceState) {
        // You could retrieve here some precious data that has been saved with onSaveTabFragmentInstance
    }

    @Override
    public ArrayList<SharedTransitionInfo> getTransitionInfos() {
        return mSharedTransitionInfos;
    }

}
