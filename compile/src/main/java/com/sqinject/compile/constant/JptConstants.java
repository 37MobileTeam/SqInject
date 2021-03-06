package com.sqinject.compile.constant;

import com.squareup.javapoet.ClassName;

public class JptConstants {

    public static final ClassName UTILS = ClassName.get("com.sqinject.core.util", "ViewUtils");

    public static final ClassName IDUTILS = ClassName.get("com.sqinject.core.util", "IdUtils");

    public static final ClassName VIEW = ClassName.get("android.view", "View");

    public static final ClassName IVIEWBINDER = ClassName.get("com.sqinject.core", "ViewBinder");

    public static final ClassName IDSBINDER = ClassName.get("com.sqinject.core", "IdsBinder");

    public static final ClassName CONTEXT = ClassName.get("android.content", "Context");

    public static final ClassName BOUNDCLICKLISTENER = ClassName.get("com.sqinject.core", "DebouncingOnClickListener");

}
