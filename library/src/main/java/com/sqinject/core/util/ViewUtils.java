package com.sqinject.core.util;

import android.view.View;

public class ViewUtils {

    public static View findRequiredView(View source, String idName, String who) {
        int id = source.getContext().getResources().getIdentifier(idName, "id", source.getContext().getPackageName());
        View view = source.findViewById(id);
        if (view != null) {
            return view;
        }
        throw new IllegalStateException("Required view '"
                + idName
                + " for "
                + who
                + " was not found.");
    }

    public static <T> T findRequiredViewAsType(View source, String idName, String who, Class<T> cls) {
        View view = findRequiredView(source, idName, who);
        return castView(view, who, cls);
    }

    public static <T> T castView(View view, String who, Class<T> cls) {
        try {
            return cls.cast(view);
        } catch (ClassCastException e) {
            throw new IllegalStateException("View for "
                    + who
                    + " was of the wrong type. See cause for more info.", e);
        }
    }

    public static <T> T castParam(Object value, String from, int fromPos, String to, int toPos,
                                  Class<T> cls) {
        try {
            return cls.cast(value);
        } catch (ClassCastException e) {
            throw new IllegalStateException("Parameter #"
                    + (fromPos + 1)
                    + " of method '"
                    + from
                    + "' was of the wrong type for parameter #"
                    + (toPos + 1)
                    + " of method '"
                    + to
                    + "'. See cause for more info.", e);
        }
    }

    private ViewUtils() {
        throw new AssertionError("No instances.");
    }

}
