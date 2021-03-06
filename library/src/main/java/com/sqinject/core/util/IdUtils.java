package com.sqinject.core.util;

import android.content.Context;
import android.view.View;

public class IdUtils {

    public static String getStringByName(String idName, Context context, String who){
        int id = context.getResources().getIdentifier(idName, "string", context.getPackageName());
        String result = context.getString(id);
        if (result != null) {
            return result;
        }
        throw new IllegalStateException("Required string '"
                + idName
                + " for "
                + who
                + " was not found.");
    }

    public static int getIntByName(String idName, Context context) {
        int id = context.getResources().getIdentifier(idName, "integer", context.getPackageName());
        int result = context.getResources().getInteger(id);
        return result;
    }

    public static int getColorByName(String idName, Context context) {
        int id = context.getResources().getIdentifier(idName, "color", context.getPackageName());
        int result = context.getResources().getColor(id);
        return result;
    }

    public static int getIdByName(String idName, String idType, Context context) {
        int id = context.getResources().getIdentifier(idName, idType, context.getPackageName());
        return id;
    }

    public static View findViewByName(String idName, View source) {
        int id = source.getResources().getIdentifier(idName, "id", source.getContext().getPackageName());
        return source.findViewById(id);
    }

}
