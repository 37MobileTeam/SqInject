package com.sqinject.compile;

import java.util.Set;

public class Utils {

    public static boolean isEmpty(Set set) {
        if (set != null && set.size() > 0) {
            return false;
        }
        return true;
    }

}
