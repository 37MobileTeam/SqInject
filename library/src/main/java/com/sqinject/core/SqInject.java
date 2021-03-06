package com.sqinject.core;

import android.app.Activity;
import android.app.Dialog;
import android.app.Fragment;
import android.content.Context;
import android.util.Log;
import android.view.View;

public class SqInject {

    private static final String TAG = SqInject.class.getSimpleName();

    /**
     * 绑定activity的view
     * @param activity
     */
    public static void bindView(Activity activity) {
        View source = activity.getWindow().getDecorView();
        bindView(activity, source);
    }

    /**
     * 绑定dialog的view
     * @param dialog
     */
    public static void bindView(Dialog dialog) {
        View source = dialog.getWindow().getDecorView();
        bindView(dialog, source);
    }

    /**
     * 绑定fragment的view
     * @param fragment
     */
    public static void bindView(Fragment fragment) {
        View source = fragment.getView();
        bindView(fragment, source);
    }

    /**
     * 绑定Object和view
     * @param target
     * @param source
     */
    public static void bindView(Object target, View source){
        try {
            //查找自身或者父类是否存在$ViewBinder类
            Class viewBinderClass = findBinderClass(target.getClass(), ViewBinder.class.getSimpleName());
            if (viewBinderClass != null) {
                //创建ViewBinder实例，调用bindView方法
                ViewBinder viewBinder = (ViewBinder) viewBinderClass.newInstance();
                viewBinder.bindView(target, source);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //解决父子类同时存在绑定的问题
    public static void bindView(Object target, Class superClass, View source) {
        try {
            Class findClass = Class.forName(superClass.getName() + "$" + ViewBinder.class.getSimpleName());
            ViewBinder viewBinder = (ViewBinder) findClass.newInstance();
            viewBinder.bindView(target, source);
        } catch (ClassNotFoundException e) {
            Log.e(TAG, "不存在"+superClass.getSimpleName()+"$"+ViewBinder.class.getSimpleName()+"类");
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }
    }

    /**
     * 绑定id类
     * @param target
     */
    public static void bindIds(Object target, Context context) {
        try {
            Class idsBinderClass = findBinderClass(target.getClass(), IdsBinder.class.getSimpleName());
            IdsBinder idsBinder = (IdsBinder) idsBinderClass.newInstance();
            idsBinder.bind(target, context);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void bindIds(Activity activity) {
        bindIds(activity, activity);
    }

    public static void bindIds(Dialog dialog) {
        bindIds(dialog, dialog.getContext());
    }

    public static void bindIds(Fragment fragment) {
        bindIds(fragment, fragment.getActivity());
    }

    //解决父子类同时存在绑定的问题
    public static void bindIds(Object target, Class superClass, Context context) {
        try {
            Class findClass = Class.forName(superClass.getName() + "$" + IdsBinder.class.getSimpleName());
            IdsBinder idsBinder = (IdsBinder) findClass.newInstance();
            idsBinder.bind(target, context);
        } catch (ClassNotFoundException e) {
            Log.e(TAG, "不存在"+superClass.getSimpleName()+"$"+IdsBinder.class.getSimpleName()+"类");
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }
    }

    /**
     * 同时绑定id和view
     * @param target
     * @param source
     */
    public static void bind(Object target, View source) {
        bindIds(target, source.getContext());
        bindView(target, source);
    }

    public static void bind(Activity activity) {
        bind(activity, activity.getWindow().getDecorView());
    }

    public static void bind(Dialog dialog) {
        bind(dialog, dialog.getWindow().getDecorView());
    }

    public static void bind(Fragment fragment){
        bind(fragment, fragment.getView());
    }

    private static Class findBinderClass(Class target, String type) {
        String clsName = target.getName();
        if (clsName.startsWith("android.") || clsName.startsWith("java.") || clsName.startsWith("androidx.")) {
            Log.d(TAG, "找不到绑定类，搜索已到达framework层，终止查找类");
            return null;
        }
        Class findClass = null;
        String findClassName = target.getName()+"$"+type;
        try {
            findClass = Class.forName(findClassName);
        } catch (ClassNotFoundException e) {
            Log.d(TAG, findClassName + "不存在，开始搜索是否存在父类绑定");
            findClass = findBinderClass(target.getSuperclass(), type);
        }
        return findClass;
    }
}
