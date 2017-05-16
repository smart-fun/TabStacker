# Tab Stacker #

**Tab Stacker** is an Android library that handles **Multiple Fragment History**, like it is done on iOS Apps.

![alt text](app/src/main/res/mipmap-xxxhdpi/ic_launcher.png?raw=true "Tab Stacker")

Each Tab has its own stack of Fragments, that can be added, replaced or removed using **animations**. When the user presses back, the top Fragment from the current Tab stack is dismissed.

When a complete stack is removed and restored (during a Tab change, or a system cleanup like when rotating the device), a **save and restore** mechanism allows you to keep your Fragments up-to-date.

Tab Stacker uses **Support Fragments**. It is recommended to always use Support Fragments as they are compatible with older devices, and the last bugs are fixed for all devices. See how to migrate to Support Fragment in the **[wiki](https://github.com/smart-fun/TabStacker/wiki)**.

## How to use##

Like any Tabbed application, you need a placeholder where put the Fragments.

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical" >

    <!-- This is the Fragment Holder -->
    <FrameLayout
        android:id="@+id/fragmentHolder"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:layout_weight="100" />

    <!-- Here you could include your bottom bar with tabs -->
    <include
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        layout="@layout/myCustomTabs" />

</LinearLayout>

```

At Activity creation, you need to create the TabStacker, and restore its state by calling restoreInstance.

When the activity is saving its state, you also need to save the TabStacker state. Call the TabStacker.saveInstance() **before** super.saveInstance() so that the TabStacker takes the hand in the save / restore process of the Fragments.

```java
public class MainActivity extends FragmentActivity {

    private TabStacker mTabStacker;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main_activity);

        mTabStacker = new TabStacker(getSupportFragmentManager(), R.id.fragmentHolder);
        
        if (savedInstanceState != null) {
            mTabStacker.restoreInstance(savedInstanceState);
            String selectedTab = mTabStacker.getCurrentTabName();
            // do something like highlight the selected tab...
        }
    }
    
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // Keep this first
        if (mTabStacker != null) {
            mTabStacker.saveInstance(outState);
        }

        super.onSaveInstanceState(outState);
    }
    
    // will be called by the Fragments when they build their view, so that the View hierarchy will be retored.
    public void restoreView(Fragment fragment, View view) {
        mTabStacker.restoreView(fragment, view);
    }
    
}
```

To handle the Back button it is quite simple:

```java
    @Override
    public void onBackPressed() {
        if (!mTabStacker.onBackPressed()) {
            super.onBackPressed();
        }
    }
```

### Pushing Fragments ###

On Android there are 2 ways to push Fragments: Add or Replace (try the Sample App to better see the difference). When you Add a Fragment, the previous one stays in place and can be seen with transparency for example. When you Replace a Fragment, the previous one is removed from the screen and a new one is displayed instead. For both Add & Replace you can use animations.

```java
    // optional animation
    AnimationSet animation = new AnimationSet(R.anim.slide_from_right, R.anim.slide_to_left, R.anim.slide_from_left, R.anim.slide_to_right);

    mTabStacker.addFragment(fragment, animation);
    // OR
    mTabStacker.replaceFragment(fragment, animation);


```

### Tab Switch ###

When the user clicks on a tab, the TabStack will save the stack of Fragments of the current Tab, and then restore the stack of Fragment of the new Tab. If the new Tab is empty, you'll have to push the 1st Fragment.

```java
    private void onClickOnTab(String tabName) {
        if (!mTabStacker.switchToTab(tabName)) {    // Try to switch to the TAB STACK
            // no fragment yet on this stack -> push the 1st fragment of the stack
            MyFragment fragment = MyFragment.createInstance(some parameters if necessary);
            mTabStacker.replaceFragment(fragment, null);  // no animation
        }
    }

```

I recommend to use the Fragment.createInstance pattern instead of new Fragment, so that all the logic for creating / saving / restoring the fragment parameters stays in the Fragment code.

### Fragment Code ###

Your Fragment must inherit from **Support Fragments** and implement the **TabStackInterface**.

```java
import android.support.v4.app.Fragment;
```

If your Fragment uses arguments, they will be automatically saved and restored.

The callbacks **onSaveTabFragmentInstance** and **onRestoreTabFragmentInstance** are replacing the Fragment.onSaveInstance() mechanism.

```java

public class MyFragment extends Fragment implements TabStacker.TabStackInterface {

    @Override
    public void onTabFragmentPresented(TabStacker.PresentReason reason) {
    }

    @Override
    public void onTabFragmentDismissed(TabStacker.DismissReason reason) {
    }

    @Override
    public View onSaveTabFragmentInstance(Bundle outState) {
    }

    @Override
    public void onRestoreTabFragmentInstance(Bundle savedInstanceState) {
    }

}

```

Let's put a concrete example with a Fragment that has:

* a title that is defined by arguments
* a String that is changed dynamically but you want to save & restore when necessary
* a View hierarchy that you want to save and restore when necessary

Note that for saving the View hierarchy you'll have to keep a reference to that view, because getView() is always null when a cleanup occurs.

```java

public class MyFragment extends Fragment implements TabStacker.TabStackInterface {

    private static final String ARGUMENT_TITLE = "title";
    private static final String DYNAMIC_IMPORTANT = "important";
    
    private View mView;		// keep a reference to the inflated view

    // createInstance() pattern with arguments
    public static MyFragment createInstance(String title) {
        MyFragment fragment = new MyFragment();
        Bundle bundle = new Bundle();
        bundle.putString(ARGUMENT_TITLE, title);
        fragment.setArguments(bundle);
        return fragment;
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.testfragment, container, false);
        return mView;
    }
    
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // retrieve the arguments to set the Title
        Bundle arguments = getArguments();
        String title = arguments.getString(ARGUMENT_TITLE);
        TextView titleView = (TextView) view.findViewById(R.id.titleView);
        titleView.setText(title);
        
        // Restore the View hierarchy (the Activity holds the TabStacker)
        MainActivity activity = (MainActivity) getActivity();
        activity.restoreView(this, view);
        
    }
    
        @Override
    public View onSaveTabFragmentInstance(Bundle outState) {
        outState.putString(DYNAMIC_IMPORTANT, "my important string to save");
        return mView;	// so that the View hierarchy can be saved
    }

    @Override
    public void onRestoreTabFragmentInstance(Bundle savedInstanceState) {
        String importantString = savedInstanceState.getString(DYNAMIC_IMPORTANT);
    }

}

```

Okay that's it for a full implementation of Fragments with TabStacker.

**See other potential tips in the [wiki](https://github.com/smart-fun/TabStacker/wiki)**.


You can experiment and download the [Tab Stacker Sample App on Google Play](https://play.google.com/apps/testing/fr.arnaudguyon.tabstackerapp)

![alt text](extras/screenshot.png?raw=true "Tab Stacker App")

## Installation with gradle

Add the following maven{} line to your **PROJECT** build.gradle file

```
allprojects {
    repositories {
        jcenter()
        maven { url "https://jitpack.io" }    // add this line
    }
}
```

Add the libary dependency to your **APP** build.gradle file

```
dependencies {
    compile 'com.github.smart-fun:TabStacker:1.0.3'    // add this line
}
```

## Troubeshooting ##

### I can click on the Fragment behind the top Fragment ###
You have called addFragment() so the previous Fragment is still behind and is clickable. If you don't want the previous Fragment to stay behind, call replaceFragment() instead. If you just don't want it to be clickable, set clickable="true" in the top fragment layout, so that all clicks that are not intercepted by buttons will be stopped.

### I got an IllegalStateException (aka State Loss Exception) ###
When you do asynchronous calls and then you want to change the Fragment (for example after a webservice call), the Fragment may have been removed or the Activity finished. In these cases you should not try to change the Fragments. Add these tests before trying to change the Fragments:

```java
// code in Activity
if (isActive()) { // be sure that the Activity is still there
    mTabStacker.replaceFragment(...)
}

// or code in Fragment
Activity activity = getActivity();
if ((activity != null) && !isDetached()) { // be sure the fragment is still there
    activity.doSomethingWithTheFragment();
}

```

Note that there are known bugs in Android where this exception is thrown and should not.

[Issue 207269](https://code.google.com/p/android/issues/detail?id=207269)

[Issue 25517](https://code.google.com/p/android/issues/detail?id=25517)

A workaround to fix that is to catch the exception, but the Fragment won't be pushed anyway.

```java
    try {
        mTabStacker.replaceFragment(...);
    } catch (IllegalStateException exception) {
        // too bad but it did not crash at least
    }
```



## Library License

Copyright 2016 Arnaud Guyon

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

[http://www.apache.org/licenses/LICENSE-2.0](http://www.apache.org/licenses/LICENSE-2.0)

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
