package fr.arnaudguyon.tabstacker;

import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.util.SparseArray;
import android.view.View;

/**
 * Created by aguyon on 01.12.16.
 */

public class ViewData {

    static Bundle saveViewHierarchy(@NonNull View view) {

        Bundle bundle = new Bundle();
        SparseArray<Parcelable> savedViewHierarchy = new SparseArray<>();

        view.saveHierarchyState(savedViewHierarchy);

        int count = savedViewHierarchy.size();
        for(int i=0; i<count; ++i) {
            int key = savedViewHierarchy.keyAt(i);
            Parcelable parcelable = savedViewHierarchy.get(key);
            String bundleKey = "" + key;
            bundle.putParcelable(bundleKey, parcelable);
        }

        return bundle;
    }

    static void restoreView(Bundle bundle, @NonNull View view) {

        if (bundle == null) {
            return;
        }

        SparseArray<Parcelable> savedViewHierarchy = new SparseArray<>();

        for(String bundleKey : bundle.keySet()) {
            Parcelable parcelable = bundle.getParcelable(bundleKey);
            int key = Integer.parseInt(bundleKey);
            savedViewHierarchy.put(key, parcelable);
        }

        view.restoreHierarchyState(savedViewHierarchy);
    }

}
