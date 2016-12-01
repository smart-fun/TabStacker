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
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.util.SparseArray;
import android.view.View;

/**
 * Helper to save and restore a View Hierarchy
 */

class ViewData {

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
