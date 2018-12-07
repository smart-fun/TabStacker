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

import android.os.Bundle;
import androidx.annotation.ColorRes;
import androidx.annotation.IdRes;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import fr.arnaudguyon.tabstacker.AnimationSet;
import fr.arnaudguyon.tabstacker.TabStacker;

public class MainActivity extends FragmentActivity {

    private static final String TAG = "MainActivity";

    private TabStacker mTabStacker;
    private AnimationSet mAddAnimation = new Anims.SlideFromBottom();
    private AnimationSet mReplaceAnimation = new Anims.SlideFromRight();

    // Tabs description: layout resource id, color resource id
    private enum Tab {
        TAB_A(R.id.tabA, R.color.tabA),
        TAB_B(R.id.tabB, R.color.tabB),
        TAB_C(R.id.tabC, R.color.tabC);

        private int mButtonResId;
        private int mColor;

        Tab(@IdRes int buttonResId, @ColorRes int color) {
            mButtonResId = buttonResId;
            mColor = color;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.mainactivity);

        // Creates the TabStacker
        mTabStacker = new TabStacker(getSupportFragmentManager(), R.id.fragmentHolder);

        if (savedInstanceState == null) {
            // new Activity: creates the first Tab
            selectTab(Tab.TAB_A);
        } else {
            // restoring Activity: restore the TabStacker, and select the saved selected tab
            mTabStacker.restoreInstance(savedInstanceState);
            Tab selectedTab = Tab.valueOf(mTabStacker.getCurrentTabName());
            selectTab(selectedTab);
        }

        for (final Tab tab : Tab.values()) {
            final Button button = (Button) findViewById(tab.mButtonResId);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onClickOnTab(tab);
                }
            });
        }

        // BACK BUTTON
        View backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        // REPLACE BUTTON
        View replaceButton = findViewById(R.id.replaceButton);
        replaceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TabFragment fragment = createFragment();
                mTabStacker.replaceFragment(fragment, mReplaceAnimation);
            }
        });

        // ADD BUTTON
        View addButton = findViewById(R.id.addButton);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TabFragment fragment = createFragment();
                mTabStacker.addFragment(fragment, mAddAnimation);
            }
        });

    }

    private void onClickOnTab(Tab clickedTab) {
        Log.i(TAG, "Clicked on Tab " + clickedTab.name());

        String tabName = clickedTab.name();
        if (mTabStacker.getCurrentTabName().equals(tabName)) {  // The user clicked again on the current stack
            mTabStacker.popToTop(true);  // Pop all but 1st fragment instantly
        } else {
            selectTab(clickedTab);
        }
    }

    private void selectTab(Tab clickedTab) {

        Log.i(TAG, "Select Tab " + clickedTab.name());

        updateButtonStates(clickedTab);

        // switch to Tab Stack
        String tabName = clickedTab.name();
        if (!mTabStacker.switchToTab(tabName)) {    // tries to switch to the TAB STACK
            // no fragment yet on this stack -> push the 1st fragment of the stack
            TabFragment fragment = createFragment();
            mTabStacker.replaceFragment(fragment, null);  // no animation
        }

    }

    // Update Button state (white / black)
    private void updateButtonStates(Tab clickedTab) {
        for(final Tab tab : Tab.values()) {
            View button = findViewById(tab.mButtonResId);
            button.setSelected((tab == clickedTab));
        }
    }

    private TabFragment createFragment() {
        String tabName = mTabStacker.getCurrentTabName();
        int number = mTabStacker.getCurrentTabSize() + 1;
        String title = "Fragment " + tabName + " " + number;
        int color = getResources().getColor(Tab.valueOf(tabName).mColor);
        color &= 0x00FFFFFF;
        color |= 0x7F000000;
        TabFragment fragment = TabFragment.createInstance(title, color);
        return fragment;
    }

    @Override
    public void onBackPressed() {
        if (!mTabStacker.onBackPressed()) {
            super.onBackPressed();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // (Keep this first) Saves the TabStacker instance
        mTabStacker.saveInstance(outState);

        super.onSaveInstanceState(outState);
    }

    // called by the Fragment when it has just created his view, so that we can restore the hierarchy
    void restoreView(Fragment fragment, View view) {
        mTabStacker.restoreView(fragment, view);
    }
}
