package com.sqinject.compile.builder;

import com.sqinject.annotation.BindColor;
import com.sqinject.annotation.BindId;
import com.sqinject.annotation.BindInt;
import com.sqinject.annotation.BindString;
import com.sqinject.annotation.IdType;
import com.sqinject.compile.Utils;
import com.sqinject.compile.checker.ResChecker;
import com.sqinject.compile.constant.JptConstants;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

/**
 * 生成id绑定类
 * @author zhuxiaoxin
 */
public class BindIdsBuilder extends BaseBuilder {

    private Map<TypeElement, List<VariableElement>> mStringTargetMap;

    private Map<TypeElement, List<VariableElement>> mIntTargetMap;

    private Map<TypeElement, List<VariableElement>> mColorTargetMap;

    private Map<TypeElement, List<VariableElement>> mIdTargetMap;

    private Set<TypeElement> mTargetSet;

    public BindIdsBuilder(RoundEnvironment roundEnvironment, ResChecker checker, Elements elementUtils, Types typeUtils, Filer filer, Messager messager) {
        super(roundEnvironment, checker, elementUtils, typeUtils, filer, messager);
    }

    @Override
    public void build() {
        mTargetSet = new HashSet<>();
        mStringTargetMap = parseAnnotation(BindString.class);
        mIntTargetMap = parseAnnotation(BindInt.class);
        mColorTargetMap = parseAnnotation(BindColor.class);
        mIdTargetMap = parseAnnotation(BindId.class);
        generateCode();
    }

    private Map<TypeElement, List<VariableElement>> parseAnnotation(Class annotationClass){
        Set<Element> elements = (Set<Element>) mRoundEnvironment.getElementsAnnotatedWith(annotationClass);
        if (!Utils.isEmpty(elements)) {
            Map<TypeElement, List<VariableElement>> map = new HashMap<>();
            for (Element element : elements) {
                if (element instanceof VariableElement) {
                    TypeElement targetElement = (TypeElement) element.getEnclosingElement();
                    mTargetSet.add(targetElement);
                    if (map.get(targetElement) == null) {
                        List<VariableElement> targetStringLists = new ArrayList<>();
                        targetStringLists.add((VariableElement) element);
                        map.put(targetElement, targetStringLists);
                    } else {
                        map.get(targetElement).add((VariableElement) element);
                    }
                }
            }
            return map;
        }
        return null;
    }

    private void generateCode() {
        for (TypeElement targetElement : mTargetSet){
            mMessager.printMessage(Diagnostic.Kind.NOTE, "存在id类注解的类" + targetElement.getQualifiedName().toString());
            //针对每个Target生成代码
            ClassName targetClassName = ClassName.get(targetElement);
            //生成的类实现的接口,实现的接口在javapoet中的表示
            ParameterizedTypeName typeName = ParameterizedTypeName.get(JptConstants.IDSBINDER, targetClassName);
            //bind方法签名
            ParameterSpec targetParameterSpec = ParameterSpec.builder(targetClassName, "target", Modifier.FINAL).build();
            ParameterSpec contextParameterSpec = ParameterSpec.builder(JptConstants.CONTEXT, "context", Modifier.FINAL).build();
            MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder("bind")
                    .addParameter(targetParameterSpec)
                    .addParameter(contextParameterSpec)
                    .addModifiers(Modifier.PUBLIC);
            if (mStringTargetMap != null && mStringTargetMap.get(targetElement) != null) {
                List<VariableElement> stringList = mStringTargetMap.get(targetElement);
                for (VariableElement stringElement : stringList) {
                    //属性名 who
                    String fieldName = stringElement.getSimpleName().toString();
                    //注解值，即idName
                    String name = stringElement.getAnnotation(BindString.class).value();
                    if(!mResChecker.isIdValid(name, "string")){
                        mMessager.printMessage(Diagnostic.Kind.ERROR, "R文件中不存在string为" + name + "的值");
                    }
                    //target.mAppName = IdUtils.getStringByName("app_name", context, "mAppName");
                    methodBuilder.addStatement("target.$N = $T.getStringByName($S, context, $S)", fieldName, JptConstants.IDUTILS, name, fieldName);
                }
            }
            if (mIntTargetMap != null && mIntTargetMap.get(targetElement) != null) {
                List<VariableElement> intList = mIntTargetMap.get(targetElement);
                for (VariableElement intElement : intList) {
                    //属性名
                    String fieldName = intElement.getSimpleName().toString();
                    //注解值
                    String name = intElement.getAnnotation(BindInt.class).value();
                    if(!mResChecker.isIdValid(name, "integer")){
                        mMessager.printMessage(Diagnostic.Kind.ERROR, "R文件中不存在integer为" + name + "的值");
                    }
                    //target.mAppId = IdUtils.getIntByName("appId", context);
                    methodBuilder.addStatement("target.$N = $T.getIntByName($S, context)", fieldName, JptConstants.IDUTILS, name);
                }
            }
            if (mColorTargetMap != null && mColorTargetMap.get(targetElement) != null) {
                List<VariableElement> colorList = mColorTargetMap.get(targetElement);
                for (VariableElement colorElement : colorList) {
                    //属性名
                    String fieldName = colorElement.getSimpleName().toString();
                    //注解值
                    String name = colorElement.getAnnotation(BindColor.class).value();
                    //检测名称是否合法
                    if(!mResChecker.isIdValid(name, "color")){
                        mMessager.printMessage(Diagnostic.Kind.ERROR, "R文件中不存在color为" + name + "的值");
                    }
                    //target.whiteColor = IdUtils.getColorByName("white", context);
                    methodBuilder.addStatement("target.$N = $T.getColorByName($S, context)", fieldName, JptConstants.IDUTILS, name);
                }
            }
            if (mIdTargetMap != null && mIdTargetMap.get(targetElement) != null) {
                List<VariableElement> idList = mIdTargetMap.get(targetElement);
                for (VariableElement idElement : idList) {
                    //属性名
                    String fieldName = idElement.getSimpleName().toString();
                    //注解值
                    String name = idElement.getAnnotation(BindId.class).value();
                    IdType idType = idElement.getAnnotation(BindId.class).type();
                    //检测名称是否合法
                    if(!mResChecker.isIdValid(name, getTypeString(idType))){
                        mMessager.printMessage(Diagnostic.Kind.ERROR, "R文件中不存在" + getTypeString(idType) + "为" + name + "的值");
                    }
                    //target.iconId = IdUtils.getIdByName("", "", context)
                    methodBuilder.addStatement("target.$N = $T.getIdByName($S, $S, context)", fieldName, JptConstants.IDUTILS, name, getTypeString(idType));
                }
            }
            //生成代码
            try {
                JavaFile.builder(targetClassName.packageName(),
                        TypeSpec.classBuilder(targetClassName.simpleName() + "$IdsBinder")
                                .addSuperinterface(typeName)
                                .addModifiers(Modifier.PUBLIC)
                                .addMethod(methodBuilder.build())
                                .build())
                        .build()
                        .writeTo(mFiler);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private String getTypeString(IdType idType) {
        switch (idType) {
            case ID:
                return "id";
            case STYLE:
                return "style";
            case LAYOUT:
                return "layout";
            case MIPMAP:
                return "mipmap";
            case DRAWABLE:
                return "drawable";
        }
        return null;
    }
}
