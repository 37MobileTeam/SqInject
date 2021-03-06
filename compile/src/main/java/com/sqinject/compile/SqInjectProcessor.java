package com.sqinject.compile;

import com.google.auto.service.AutoService;
import com.sqinject.annotation.BindColor;
import com.sqinject.annotation.BindId;
import com.sqinject.annotation.BindInt;
import com.sqinject.annotation.BindString;
import com.sqinject.annotation.BindView;
import com.sqinject.annotation.OnClick;
import com.sqinject.compile.builder.BindIdsBuilder;
import com.sqinject.compile.builder.BindViewBuilder;
import com.sqinject.compile.checker.ResChecker;

import java.lang.annotation.Annotation;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;



/**
 * （1）注解处理器是 javac 自带的一个工具，用来在编译时期扫描处理注解信息
 * （2）注解处理器在 Java 5 的时候就已经存在了，但直到 Java 6 （发布于2006看十二月）的时候才有可用的API，
 * 过了一段时间java的使用者们才意识到注解处理器的强大。所以最近几年它才开始流行。
 * （3）一个特定注解的处理器以 java 源代码（或者已编译的字节码）作为输入
 * （4）然后生成一些文件（通常是.java文件）作为输出，那意味着什么呢？你可以生成 java 代码！
 *
 *
 * APT(Annotation Processing Tool)是一种注解处理工具，
 * 它会对源文件进行扫描，找出相应的Annotation，
 * 并在注解处理器中进行操作，
 * 具体操作由注解处理器也就是用户自己去实现
 *
 *APT工具，常用的有2个：
 *      android-apt
 *      Gradle2.2以后的annotationProcessor功能。
 *
 *一个Gradle插件，帮助Android Studio处理annotation processors
 * Gradle2.2以后Gradle提供annotationProcessor的功能可以完全代替android-apt
 * annotationProcessor是Gradle2.2+内置的功能，不需要额外引入其他插件，可以向下面这样直接在gradle文件引入
 *
 * AbstractProcessor：Java内置注解处理器，注解处理器核心工作都在这个类进行
 * AutoService：Google开源用来自动注册我们自己的注解处理器
 *JavaPoet：Java代码生成器，方便我们生成Java文件
 *
 * */

@AutoService(Processor.class)
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class SqInjectProcessor extends AbstractProcessor {

    //用于操作元素的工具类
    private Elements mElementUtils;

    //用于操作类型的工具类
    private Types mTypeUtils;

    //写文件
    private Filer mFiler;

    //打印日志
    private Messager mMessager;

    //包名，用于寻找R类
    private String mPackageNeme;

    //检测资源名称合法性
    private ResChecker mResChecker;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        //特殊的方法init()，它会被注解处理工具调用
        super.init(processingEnvironment);
        mElementUtils = processingEnvironment.getElementUtils();
        mTypeUtils = processingEnvironment.getTypeUtils();
        mFiler = processingEnvironment.getFiler();
        mMessager = processingEnvironment.getMessager();
        mPackageNeme = processingEnvironment.getOptions().get("applicationId");
        if (mPackageNeme == null) {
            mMessager.printMessage(Diagnostic.Kind.ERROR, "注解处理器需要配置参数applicationId传递包名");
        }
        mMessager.printMessage(Diagnostic.Kind.NOTE, "传递过来的参数包名：" + mPackageNeme);
        mResChecker = new ResChecker(mPackageNeme, mElementUtils, mTypeUtils, mMessager);
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        //控件类注解解析,ResChecker检查资源id合法性，合法则生成"类名+$ViewBinder类,否则编译失败
        BindViewBuilder bindViewBuilder = new BindViewBuilder(roundEnvironment, mResChecker, mElementUtils, mTypeUtils, mFiler, mMessager);
        bindViewBuilder.build();
        //id类注解解析，ResChecker检查资源id合法性，合法则生成"类名+$IdBinder类",否则编译失败
        BindIdsBuilder bindIdsBuilder = new BindIdsBuilder(roundEnvironment, mResChecker, mElementUtils, mTypeUtils, mFiler, mMessager);
        bindIdsBuilder.build();
        return false;
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> types = new LinkedHashSet<>();
        for (Class<? extends Annotation> annotation : getSupportedAnnotations()) {
            types.add(annotation.getCanonicalName());
        }
        return types;
    }

    private Set<Class<? extends Annotation>> getSupportedAnnotations() {
        Set<Class<? extends Annotation>> annotations = new LinkedHashSet<>();
        annotations.add(BindColor.class);
        annotations.add(BindInt.class);
        annotations.add(BindId.class);
        annotations.add(BindString.class);
        annotations.add(BindView.class);
        annotations.add(OnClick.class);
        return annotations;
    }
}
