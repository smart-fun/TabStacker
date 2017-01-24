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
package fr.arnaudguyon.tabstacker;

import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

/**
 * Manager which handles several Back Stacks for Fragments.
 * Each stack is linked to a "tab" name so that it is easy to switch from one stack to another one using tab names.
 */
public class TabStacker {

    private static final String BUNDLE_TAB_STACKER = "TabStacker";

    enum Type {
        Replace,
        Add
    }

    private String mCurrentTab = "";
    private FragmentManager mFragmentManager;
    private int mFragmentHolder;
    private HashMap<String, ArrayList<FragmentInfo>> mStacks = new HashMap<>();     // Tab name, FragmentInfo List

    /**
     * Constructor for a TabStacker instance
     * @param fragmentManager the FragmentManager from support library
     * @param fragmentHolder the place holder for all the Fragments
     */
    public TabStacker(FragmentManager fragmentManager, @IdRes int fragmentHolder) {
        mFragmentManager = fragmentManager;
        mFragmentHolder = fragmentHolder;
    }

    /**
     * Switch from the current tab to another tab.
     * @param tabName Name of the New tab
     * @return true if the new tab is already the current tab, or if the new tab has been restored (not empty).
     * false if the new tab is empty (so that a new Fragment must be put). In all cases the current tab becomes the new tab.
     */
    public boolean switchToTab(String tabName) {
        if (tabName.equals(mCurrentTab)) {
            return true;
        }
        notifyAllRemoved(mCurrentTab, DismissReason.LEAVING_STACK);
        popAll(mCurrentTab);
        mCurrentTab = tabName;
        notifyAllRestored(tabName, PresentReason.RESTORING_STACK);
        pushAll(tabName);
        return !isEmpty(tabName);
    }

    /**
     * Replace a fragment by another one, in the current tab stack
     * @param fragment The new fragment to display
     * @param animationSet Optional animations
     */
    public void replaceFragment(Fragment fragment, AnimationSet animationSet) {
        checkFragmentInterface(fragment);
        FragmentInfo topInfo = getTopFragmentInfo(mCurrentTab);
        if (topInfo != null) {
            onFragmentDismissed(topInfo.mFragment, DismissReason.REPLACED);
        }
        pushFragment(fragment, animationSet, Type.Replace);
        onFragmentPresented(fragment, PresentReason.NEW_FRAGMENT);
    }

    /**
     * Add a new fragment to the current tab stack
     * @param fragment The new fragment to display
     * @param animationSet Optional animations
     */
    public void addFragment(Fragment fragment, AnimationSet animationSet) {
        checkFragmentInterface(fragment);
        FragmentInfo topInfo = getTopFragmentInfo(mCurrentTab);
        if (topInfo != null) {
            onFragmentDismissed(topInfo.mFragment, DismissReason.OVERLAPPED);
        }
        pushFragment(fragment, animationSet, Type.Add);
        onFragmentPresented(fragment, PresentReason.NEW_FRAGMENT);
    }

    /**
     * @param selectedTab Name of the tab
     * @return the top fragment of the selectedTab tab stack
     */
    public Fragment getTopFragment(@NonNull String selectedTab) {
        FragmentInfo fragmentInfo = getTopFragmentInfo(selectedTab);
        if (fragmentInfo != null) {
            return fragmentInfo.mFragment;
        } else {
            return null;
        }
    }

    /**
     * @return the fragment at the top of the current tab stack
     */
    public Fragment getCurrentTopFragment() {
        return getTopFragment(mCurrentTab);
    }

    /**
     * @return the name of the current tab stack
     */
    public String getCurrentTabName() {
        return mCurrentTab;
    }

    /**
     * @return the number of fragments in the current tab stack
     */
    public int getCurrentTabSize() {
        return getTabSize(mCurrentTab);
    }

    /**
     *
     * @param tabName Name of the tab stack
     * @return the number of fragments in the tabName tab stack
     */
    public int getTabSize(String tabName) {
        ArrayList<FragmentInfo> list = mStacks.get(tabName);
        return (list != null) ? list.size() : 0;
    }

    private void pushFragment(Fragment fragment, AnimationSet animationSet, Type type) {
        checkFragmentInterface(fragment);
        if (isEmpty(mCurrentTab)) {
            type = Type.Replace;
            animationSet = null;
        }
        FragmentInfo fragmentInfo = new FragmentInfo(fragment, animationSet, type);
        if (type == Type.Replace) {
            replaceFragment(fragmentInfo, false);
        } else {
            addFragment(fragmentInfo, false);
        }

        ArrayList<FragmentInfo> list = mStacks.get(mCurrentTab);
        if (list == null) {
            list = new ArrayList<>();
            mStacks.put(mCurrentTab, list);
        }
        list.add(fragmentInfo);
    }

    /**
     * To be called from the Activity. Pop top fragment from the current tab stack.
     * The 1st fragment of the current tab stack is never popped.
     * @return false if this is the last fragment of the current tab stack, true if there are several fragments.
     */
    public boolean onBackPressed() {
        return pop(DismissReason.BACK, PresentReason.BACK, false);
    }

    /**
     * pop 1 fragment from the current stack & from the screen. The 1st fragment cannot be popped.
     * @param dismissReason Reason why the fragment is dismissed
     * @param presentReason Reason why the fragment behind is presented
     * @param instant removes instantly when true, else use animations if defined
     * @return true if the fragment has been successfully popped
     */
    private boolean pop(DismissReason dismissReason, PresentReason presentReason, boolean instant) {
        if (getTabSize(mCurrentTab) <= 1) { // Don't pop last remaining fragment
            return false;
        }
        ArrayList<FragmentInfo> infos = mStacks.get(mCurrentTab);
        FragmentInfo topFragmentInfo = infos.get(infos.size() - 1);
        if (topFragmentInfo.mType == Type.Add) {
            removeFragment(topFragmentInfo, instant);
            onFragmentDismissed(topFragmentInfo.mFragment, dismissReason);
        } else {

            int currentIndex = infos.size() - 1;
            int lastPreviousReplaceIndex;
            // finds the last replace index, so that we restore it and then all adds in between
            for (lastPreviousReplaceIndex = currentIndex - 1; lastPreviousReplaceIndex >= 1; --lastPreviousReplaceIndex) {
                FragmentInfo previousFragmentInfo = infos.get(lastPreviousReplaceIndex);
                if (previousFragmentInfo.mType == Type.Replace) {
                    break;
                }
            }

            FragmentInfo previousReplace = infos.get(lastPreviousReplaceIndex);
            AnimationSet topAnimationSet = instant ? null : topFragmentInfo.mAnimationSet;
            int inAnim = (topAnimationSet != null) ? topAnimationSet.getPopInAnim() : 0;
            int outAnim = (topAnimationSet != null) ? topAnimationSet.getPopOutAnim() : 0;

            {
                FragmentTransaction transaction = mFragmentManager.beginTransaction();
                if ((inAnim != 0) && (outAnim != 0)) {
                    transaction.setCustomAnimations(inAnim, outAnim);
                }
                transaction.replace(mFragmentHolder, previousReplace.mFragment);
                transaction.commit();
                onFragmentDismissed(topFragmentInfo.mFragment, dismissReason);
            }

            {
                for (int iAdd = lastPreviousReplaceIndex + 1; iAdd < currentIndex; ++iAdd) {
                    FragmentTransaction transaction = mFragmentManager.beginTransaction();
                    if ((inAnim != 0) && (outAnim != 0)) {
                        transaction.setCustomAnimations(inAnim, outAnim);
                    }
                    transaction.add(mFragmentHolder, infos.get(iAdd).mFragment);
                    transaction.commit();
                }
            }
        }
        infos.remove(topFragmentInfo);
        topFragmentInfo = getTopFragmentInfo(mCurrentTab);
        if (topFragmentInfo != null) {
            onFragmentPresented(topFragmentInfo.mFragment, presentReason);
        }
        return true;
    }

    /**
     * clears all the stack of the current tab
     */
    public void clearTabStack() {
        notifyAllRemoved(mCurrentTab, DismissReason.CLEARING_STACK);
        popAll(mCurrentTab);
        ArrayList<FragmentInfo> infos = mStacks.get(mCurrentTab);
        if (infos != null) {
            infos.clear();
        }
    }

    /**
     * pop all the fragments from the current stack except the 1st fragment, and remove them from the screen
     * @param instant removes the fragments instantly if true, else use animations if some are defined
     * @return true if the fragment has been popped
     */
    public int popToTop(boolean instant) {
        int tabSize = getCurrentTabSize();
        if (tabSize > 1) {
            return pop(tabSize - 1, instant);
        }
        return 0;
    }

    /**
     * pop several fragments from the current stack & from the screen. The 1st fragment cannot be popped.
     * @param count the number of fragments to pop
     * @param instant removes the fragment instantly if true, else use animations if some are defined
     * @return the number of popped fragments
     */
    public int pop(int count, boolean instant) {
        int nbPopped = 0;
        ArrayList<FragmentInfo> infos = mStacks.get(mCurrentTab);
        if ((infos == null) || infos.isEmpty()) {
            return nbPopped;
        }
        while((nbPopped < count) && (infos.size() > 0)) {
            if (pop(DismissReason.POP, PresentReason.POP, instant)) {
                ++nbPopped;
            } else {
                return nbPopped;
            }
        }
        return nbPopped;
    }

    private boolean isEmpty(String tabName) {
        return (getTabSize(tabName) == 0);
    }

    private FragmentInfo getTopFragmentInfo(String tabName) {
        ArrayList<FragmentInfo> infos = mStacks.get(tabName);
        if ((infos == null) || infos.isEmpty()) {
            return null;
        } else {
            return infos.get(infos.size() - 1);
        }
    }

    private void popAll(String tabName) {
        ArrayList<FragmentInfo> infos = mStacks.get(tabName);
        if (infos != null) {
            for (int i = infos.size() - 1; i > 0; --i) {
                FragmentInfo topInfo = infos.get(i);
                removeFragment(topInfo, true);
            }
        }
    }

    private void pushAll(String tabName) {
        ArrayList<FragmentInfo> infos = mStacks.get(tabName);
        if ((infos != null) && (infos.size() > 0)) {

            // Search the last "replace" fragment so that we start restoring from him
            int lastReplace = 0;
            for (int i = infos.size() - 1; i > 0; --i) {
                FragmentInfo info = infos.get(i);
                if (info.mType == Type.Replace) {
                    lastReplace = i;
                    break;
                }
            }
            // Restore the last "replace" fragment
            FragmentInfo replaceInfo = infos.get(lastReplace);
            replaceFragment(replaceInfo, true);

            // Eventually adds all the other "add" fragments
            for (int i = lastReplace + 1; i < infos.size(); ++i) {
                FragmentInfo info = infos.get(i);
                addFragment(info, true);
            }
        }
    }

    private void removeFragment(FragmentInfo fragmentInfo, boolean instant) {
        FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
        if (!instant && fragmentInfo.mAnimationSet != null) {
            fragmentInfo.mAnimationSet.addToTransaction(fragmentTransaction);
//            fragmentTransaction.setCustomAnimations(fragmentInfo.mAnimationSet.getPopInAnim(), fragmentInfo.mAnimationSet.getPopOutAnim());
        }
        fragmentTransaction.remove(fragmentInfo.mFragment);
        fragmentTransaction.commit();
    }

    private void replaceFragment(FragmentInfo fragmentInfo, boolean instant) {
        FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
        if (!instant && fragmentInfo.mAnimationSet != null) {
            fragmentInfo.mAnimationSet.addToTransaction(fragmentTransaction);
//            fragmentTransaction.setCustomAnimations(fragmentInfo.mAnimationSet.getPushInAnim(), fragmentInfo.mAnimationSet.getPushOutAnim());
        }
        fragmentTransaction.replace(mFragmentHolder, fragmentInfo.mFragment);
        fragmentTransaction.commit();
    }

    private void addFragment(FragmentInfo fragmentInfo, boolean instant) {
        FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
        if (!instant && fragmentInfo.mAnimationSet != null) {
            fragmentInfo.mAnimationSet.addToTransaction(fragmentTransaction);
//            fragmentTransaction.setCustomAnimations(fragmentInfo.mAnimationSet.getPushInAnim(), fragmentInfo.mAnimationSet.getPushOutAnim());
        }
        fragmentTransaction.add(mFragmentHolder, fragmentInfo.mFragment);
        fragmentTransaction.commit();
    }

    private void notifyAllRemoved(String tabName, DismissReason reason) {
        ArrayList<FragmentInfo> infos = mStacks.get(tabName);
        if (infos != null) {
            for (int i = infos.size() - 1; i >= 0; --i) {
                FragmentInfo info = infos.get(i);
                onFragmentDismissed(info.mFragment, reason);
            }
        }
    }

    private void notifyAllRestored(String tabName, PresentReason reason) {
        ArrayList<FragmentInfo> infos = mStacks.get(tabName);
        if (infos != null) {
            for (FragmentInfo info : infos) {
                Fragment fragment = info.mFragment;
                onFragmentPresented(fragment, reason);
            }
        }
    }

    public enum PresentReason {
        NEW_FRAGMENT,       // This is a newly created fragment
        RESTORING_STACK,    // The Stack is restoring
        BACK,               // The user pressed Back
        POP                 // The programmer popped some fragments
    }

    public enum DismissReason {
        REPLACED,           // is replaced by another Fragment (Replace)
        OVERLAPPED,         // is overlapped with another Fragment (Add)
        LEAVING_STACK,      // the stack is changing
        BACK,               // The user pressed Back
        CLEARING_STACK,     // Stack is being cleared
        POP                 // The programmer popped some fragments
    }

    private void onFragmentPresented(Fragment fragment, PresentReason reason) {
        if (fragment instanceof TabStackInterface) {
            ((TabStackInterface) fragment).onTabFragmentPresented(reason);
        }
    }

    private void onFragmentDismissed(Fragment fragment, DismissReason reason) {
        if (fragment instanceof TabStackInterface) {
            ((TabStackInterface) fragment).onTabFragmentDismissed(reason);
        }
    }

    /**
     * Interface which can be implemented by the Fragment that are pushed (not mandatory)
     */
    public interface TabStackInterface {
        /**
         * called when a fragment is presented on screen (see PresentReason)
         * @param reason Reason why the fragment is presented
         */
        void onTabFragmentPresented(PresentReason reason);

        /**
         * called when a fragment is dismissed from the screen (see DismissReason)
         * @param reason Reason why the fragment is dismissed
         */
        void onTabFragmentDismissed(DismissReason reason);

        View onSaveTabFragmentInstance(Bundle outState);
        void onRestoreTabFragmentInstance(Bundle savedInstanceState);
    }

    private void checkFragmentInterface(Fragment fragment) {
        if (!(fragment instanceof TabStackInterface)) {
            String className = fragment.getClass().getName();
            throw new RuntimeException(className + " must implement TabStackInterface");
        }
    }

    private static final String BUNDLE_CURRENT_TAB = "CurrentTab";
    private static final String BUNDLE_TAB_NAMES = "TabNames";
    private static final String BUNDLE_FRAGMENT_PREFIX = "FragmentInfo_";
    private static final String BUNDLE_STACKSIZE_POSTFIX = "_stackSize";

    /**
     * Saves the TabStacker into a Bundle so that it can be retrieved later
     * @param outState Bundle where to save the TabStacker
     */
    public void saveInstance(Bundle outState) {
        Bundle bundle = new Bundle();
        bundle.putString(BUNDLE_CURRENT_TAB, mCurrentTab);  // Current Tab

        Set<String> keys = mStacks.keySet();
        ArrayList<String> tabNames = new ArrayList<>(keys);
        bundle.putStringArrayList(BUNDLE_TAB_NAMES, tabNames);  // Tab Names

        for (String tabName : keys) {
            Bundle stackBundle = new Bundle();
            ArrayList<FragmentInfo> stack = mStacks.get(tabName);
            String stackSizeKey = tabName + BUNDLE_STACKSIZE_POSTFIX;
            stackBundle.putInt(stackSizeKey, stack.size());
            int index = 0;
            for(FragmentInfo fragmentInfo : stack) {
                String fragmentInfoKey = BUNDLE_FRAGMENT_PREFIX + index;
                ++index;
                Bundle infoBundle = fragmentInfo.saveInstance();
                stackBundle.putBundle(fragmentInfoKey, infoBundle);
            }
            bundle.putBundle(tabName, stackBundle);
        }

        // FIX
        // Do not remove the fragments from the stack as the system
        // saves the state in case it needs to rebuild it later, but
        // sometime it just don't need to rebuild them so they need to be still there.
        // Happens when starting a new Activity.

        // Force to remove all fragments from screen
//        int stackSize = getCurrentTabSize();
//        for(int i=0; i<stackSize; ++i) {
//            FragmentTransaction transaction = mFragmentManager.beginTransaction();
//            transaction.remove(getCurrentTopFragment());
//            transaction.commitAllowingStateLoss();
//        }

        outState.putBundle(BUNDLE_TAB_STACKER, bundle);
    }

    /**
     * Restore the TabStacker and push all the fragment for the current tab
     * Note that the view is not restored yet (it is not created yet at this moment)
     * @param savedInstanceState Bundle with the saved TabStacker to restore
     */
    public void restoreInstance(Bundle savedInstanceState) {
        if (savedInstanceState.containsKey(BUNDLE_TAB_STACKER)) {
            Bundle bundle = savedInstanceState.getBundle(BUNDLE_TAB_STACKER);
            if (bundle != null) {
                mCurrentTab = bundle.getString(BUNDLE_CURRENT_TAB);
                ArrayList<String> tabNames = bundle.getStringArrayList(BUNDLE_TAB_NAMES);
                if (tabNames != null) {
                    for (String tabName : tabNames) {
                        ArrayList<FragmentInfo> stackInfos = mStacks.get(tabName);
                        if (stackInfos == null) {
                            stackInfos = new ArrayList<>();
                            mStacks.put(tabName, stackInfos);
                        } else {
                            stackInfos.clear();
                        }
                        Bundle stackBundle = bundle.getBundle(tabName);
                        String stackSizeKey = tabName + BUNDLE_STACKSIZE_POSTFIX;
                        int stackSize = stackBundle.getInt(stackSizeKey);
                        for (int i = 0; i < stackSize; ++i) {
                            String fragmentInfoKey = BUNDLE_FRAGMENT_PREFIX + i;
                            Bundle fragmentInfoBundle = stackBundle.getBundle(fragmentInfoKey);
                            FragmentInfo fragmentInfo = FragmentInfo.restoreInstance(fragmentInfoBundle);
                            stackInfos.add(fragmentInfo);
                        }
                    }
                }
            }
        }

        notifyAllRestored(mCurrentTab, PresentReason.RESTORING_STACK);
        pushAll(mCurrentTab);
    }

    /**
     * Restores the View hierarchy
     * @param fragment Fragment which holds the View
     * @param fragmentView View to restore
     */
    public void restoreView(Fragment fragment, View fragmentView) {
        ArrayList<FragmentInfo> fragmentInfos = mStacks.get(mCurrentTab);
        for(FragmentInfo fragmentInfo : fragmentInfos) {
            if (fragmentInfo.mFragment == fragment) {
                fragmentInfo.restoreView(fragmentView);
                break;
            }
        }
    }

}
