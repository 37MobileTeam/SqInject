package com.sqinject.compile.builder;

import com.sqinject.compile.checker.ResChecker;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

abstract public class BaseBuilder {

    protected Messager mMessager;

    protected Elements mElementUtils;

    protected Types mTypeUtils;

    protected Filer mFiler;

    protected ResChecker mResChecker;

    protected RoundEnvironment mRoundEnvironment;

    public BaseBuilder(RoundEnvironment roundEnvironment, ResChecker checker, Elements elementUtils, Types typeUtils, Filer filer, Messager messager) {
        mRoundEnvironment = roundEnvironment;
        mElementUtils = elementUtils;
        mTypeUtils = typeUtils;
        mFiler = filer;
        mMessager = messager;
        mResChecker = checker;
    }

    abstract public void build();

}
