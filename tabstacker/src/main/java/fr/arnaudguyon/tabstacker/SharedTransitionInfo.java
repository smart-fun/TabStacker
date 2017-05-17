package fr.arnaudguyon.tabstacker;

import android.support.annotation.NonNull;
import android.view.View;

import java.util.ArrayList;

/**
 * Created by aguyon on 17.05.17.
 */

public class SharedTransitionInfo {

    private View mSrcView;
    private String mDstTransitionName;

    public SharedTransitionInfo(@NonNull View srcView, @NonNull String dstTransitionName) {
        mSrcView = srcView;
        mDstTransitionName = dstTransitionName;
    }

    View getSrcView() {
        return mSrcView;
    }

    String getDstTransitionName() {
        return mDstTransitionName;
    }

    public interface TransitionInterface {
        ArrayList<SharedTransitionInfo> getTransitionInfos();
    }

}
