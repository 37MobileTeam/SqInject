package com.sqinject.compile.builder;

import com.sqinject.annotation.BindView;
import com.sqinject.annotation.OnClick;
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
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

/**
 * @author zhuxiaoxin
 * 解析控件相关注解，生成xxx$ViewBinder类
 * 当前是BindView
 * 1、解析注解存放到map-注解处理器
 * 2、根据注解生成对应代码-javapoet
 */
public class BindViewBuilder extends BaseBuilder {

    private Map<TypeElement, List<VariableElement>> mBindViewIdTargetMap;

    private Map<TypeElement, List<ExecutableElement>> mOnClickTargetMap;

    private Set<TypeElement> mTargetSet;

    public BindViewBuilder(RoundEnvironment roundEnvironment, ResChecker checker,  Elements elementUtils, Types typeUtils, Filer filer, Messager messager) {
        super(roundEnvironment, checker, elementUtils, typeUtils, filer, messager);
    }

    @Override
    public void build() {
        //存放使用了注解的类
        mTargetSet = new HashSet<>();
        //解析BindView注解存储到Map中, key为该属性所在类
        mBindViewIdTargetMap = parseBindView();
        //解析OnClick注解存储到Map中, key为该方法所在类
        mOnClickTargetMap = parseOnClick();
        //遍历TargetSet, 从Map中获取使用了注解的成员和方法，生成代码
        generateCode();
    }

    private void generateCode() {

        for (TypeElement targetElement : mTargetSet) {
            //用于获得activity类名在javapoet中的表示
            ClassName targetClassName = ClassName.get(targetElement);

            //生成的类实现的接口,实现的接口在javapoet中的表示
            ParameterizedTypeName typeName = ParameterizedTypeName.get(JptConstants.IVIEWBINDER, targetClassName);

            /*
             * 构建参数
             * */
            //bind方法参数,即MainActivity target
            ParameterSpec targetParameterSpec = ParameterSpec.builder(targetClassName, "target", Modifier.FINAL).build();
            //bind方法参数二，即View source
            ParameterSpec sourceParameterSpec = ParameterSpec.builder(JptConstants.VIEW, "source", Modifier.FINAL).build();

            /*
             * 构建方法
             * **/
            //方法声明：public void bind(final MainActivity target)
            MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder("bindView")
                    .addAnnotation(Override.class)
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(targetParameterSpec)
                    .addParameter(sourceParameterSpec);

            if (mBindViewIdTargetMap != null && mBindViewIdTargetMap.get(targetElement) != null) {
                List<VariableElement> viewElements = mBindViewIdTargetMap.get(targetElement);
                //方法体
                for (VariableElement viewElement : viewElements) {
                    //获取属性名
                    String fieldName = viewElement.getSimpleName().toString();
                    //获取类型
                    TypeMirror typeMirror = viewElement.asType();
                    TypeElement fieldClassElement = (TypeElement) mTypeUtils.asElement(typeMirror);
                    mMessager.printMessage(Diagnostic.Kind.NOTE, "注解的字段类型为: " + fieldClassElement.getQualifiedName().toString());
                    TypeElement fieldType = mElementUtils.getTypeElement(fieldClassElement.getQualifiedName());
                    ClassName fieldClassName = ClassName.get(fieldType);
                    //获取@BindView注解的值,即名称
                    String name = viewElement.getAnnotation(BindView.class).value();
                    //检测名称是否合法
                    if(!mResChecker.isIdValid(name, "id")){
                        mMessager.printMessage(Diagnostic.Kind.ERROR, "R文件中不存在id为" + name + "的值");
                    }
                    methodBuilder.addStatement("target.$N = $T.findRequiredViewAsType(source, $S, $S, $T.class)", fieldName, JptConstants.UTILS, name, "field " + fieldName,fieldClassName);
                }
            }

            if (mOnClickTargetMap != null && mOnClickTargetMap.get(targetElement) != null) {
                List<ExecutableElement> methodElements = mOnClickTargetMap.get(targetElement);
                for (ExecutableElement methodElement : methodElements) {
                    //获取方法名
                    String methodName = methodElement.getSimpleName().toString();
                    //获取@OnClick注解的值
                    String name = methodElement.getAnnotation(OnClick.class).value();
                    //检测名称是否合法
                    if(!mResChecker.isIdValid(name, "id")){
                        mMessager.printMessage(Diagnostic.Kind.ERROR, "R文件中不存在id为" + name + "的值");
                    }
                    /*
                     * IdUtils.findViewByName(name, source).setOnClickListener(new DebouncingOnClickListener(){
                     * @Override
                     * public void doClick(View v){
                     *     target.click(v);
                     * }
                     * });
                     */
                    methodBuilder.beginControlFlow("$T.findViewByName($S, source).setOnClickListener(new $T()",
                            JptConstants.IDUTILS, name, JptConstants.BOUNDCLICKLISTENER)
                            .beginControlFlow("public void doClick($T v)", JptConstants.VIEW)
                            .addStatement("$N." + methodName + "(v)", "target")
                            .endControlFlow()
                            .endControlFlow(")")
                            .build();
                }
            }

            /*
             * 构建类：classBuilder
             * 构建文件并指定生成文件目录：JavaFile
             * 生成代码
             * */
            try {
                JavaFile.builder(targetClassName.packageName(),
                        TypeSpec.classBuilder(targetClassName.simpleName() + "$ViewBinder")
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

    /**
     * 解析OnClick注解
     * @return
     */
    private Map<TypeElement, List<ExecutableElement>> parseOnClick(){
        Set<Element> elements = (Set<Element>) mRoundEnvironment.getElementsAnnotatedWith(OnClick.class);
        if (!Utils.isEmpty(elements)) {
            Map<TypeElement, List<ExecutableElement>> map = new HashMap<>();
            for (Element element : elements) {
                if (element instanceof ExecutableElement) {
                    TypeElement targetElement = (TypeElement) element.getEnclosingElement();
                    mTargetSet.add(targetElement);
                    if (map.get(targetElement) == null) {
                        List<ExecutableElement> targetStringLists = new ArrayList<>();
                        targetStringLists.add((ExecutableElement) element);
                        map.put(targetElement, targetStringLists);
                    } else {
                        map.get(targetElement).add((ExecutableElement) element);
                    }
                }
            }
            return map;
        }
        return null;
    }

    /**
     * 解析BindView注解
     * @return
     */
    private Map<TypeElement, List<VariableElement>> parseBindView(){
        Set<Element> elements = (Set<Element>) mRoundEnvironment.getElementsAnnotatedWith(BindView.class);
        if (!Utils.isEmpty(elements)) {
            Map<TypeElement, List<VariableElement>> map = new HashMap<>();
            for (Element element : elements) {
                if (element instanceof VariableElement) {
                    //获取该属性所在类
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

}
