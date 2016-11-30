# Tab Stacker #

**Tab Stacker** is an Android Studio library that allows to handle a **Fragment history for each Tab**, like it is done on iOS apps natively.

![alt text](app\src\main\res\mipmap-xxxhdpi\ic_launcher.png?raw=true "Tab Stacker")

Each Tab has its own stack of Fragments, that can be added, replaced or removed using **animations**. When the user presses back, the top Fragment from the current Tab stack is dismissed.

When a complete stack is removed and restored (during a Tab change, or a system cleanup like when rotating the device), a **save and restore** mechanism allows you to keep your Fragments up-to-date.

Tab Stacker uses **Support Fragments**. It is recommended to always use Support Fragments as they are compatible with older devices, and the last bugs are fixed for all devices. See how to migrate to Support Fragment in the corresponding section below.

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
        mTabStacker.saveInstance(outState);

        super.onSaveInstanceState(outState);
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

Your Fragment must inherit from Support Fragments.

```java
import android.support.v4.app.Fragment;
```

If your Fragment uses arguments, they will be automatically saved and restored.

If you have dynamic values that you want to save and restore, then you have to implement TabStacker.TabStackInterface. This interface has also callbacks that are called when a Fragment is presented or dismissed.

```java

public class MyFragment extends Fragment implements TabStacker.TabStackInterface {

    @Override
    public void onTabFragmentPresented(TabStacker.PresentReason reason) {
    }

    @Override
    public void onTabFragmentDismissed(TabStacker.DismissReason reason) {
    }

    @Override
    public void onSaveTabFragmentInstance(Bundle outState) {
    }

    @Override
    public void onRestoreTabFragmentInstance(Bundle savedInstanceState) {
    }

}

```

Let's put a simple example with arguments and dynamic content. Imagine you have a Fragment with a title that is given by argument, and a checkbox that you want to save the state.

Note that for saving elements from the view you'll have to keep a reference to the view.

```java

public class MyFragment extends Fragment implements TabStacker.TabStackInterface {

    private static final String ARGUMENT_TITLE = "title";
    private static final String DYNAMIC_DATA_CHECKBOX = "checkBox";
    
    private View mView;		// keep a reference to the inflated view
    private boolean mCheckBoxValue;

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
        
        // restore the checkbox value
        CheckBox optionCheckBox = (CheckBox) mView.findViewById(R.id.optionCheckBox);
        optionCheckBox.setChecked(mCheckBoxValue);
    }
    
        @Override
    public void onSaveTabFragmentInstance(Bundle outState) {
        if (mView == null) {    // the Fragment has not been presented since the last save, so put the last saved values
            outState.putBoolean(DYNAMIC_DATA_CHECKBOX, mCheckBoxValue);
        } else {
            CheckBox optionCheckBox = (CheckBox) mView.findViewById(R.id.optionCheckBox);
            outState.putBoolean(DYNAMIC_DATA_CHECKBOX, optionCheckBox.isChecked());
        }
    }

    @Override
    public void onRestoreTabFragmentInstance(Bundle savedInstanceState) {
        mCheckBoxValue = savedInstanceState.getBoolean(DYNAMIC_DATA_CHECKBOX);
    }

}

```

Okay that's it for a full implementation of Fragments with TabStacker.

At the moment the state of every component of the view is not saved / restored (this is why I gave the example of the CheckBox). This is planned to implement this in a future version of the library.

## Migrate to Support Fragments ##

The first thing to do is to add the dependency in your app build.gradle file. If you already include appcompat-v4 library you don't need this.

```xml
dependencies {
    compile 'com.android.support:support-fragment:25.0.1'
}
```

Then your Activity must inherit from FragmentActivity

```java
import android.support.v4.app.FragmentActivity;
```
Replace any getFragmentManager() in your activities with get**Support**FragmentManager()

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
    compile 'com.github.smart-fun:TabStacker:0.8.0'    // add this line
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
