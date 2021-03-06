package com.sqinject.compile.checker;

import javax.annotation.processing.Messager;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

/**
 * 监测资源是否合法
 */
public class ResChecker {

    private String mPackageNeme;

    private Elements mElementUtils;

    private Types mTypesUtils;

    private Messager mMessager;

    public ResChecker(String pkgName, Elements elementUtils, Types typeUtils, Messager messager){
        mPackageNeme = pkgName;
        mElementUtils = elementUtils;
        mTypesUtils = typeUtils;
        mMessager = messager;
    }

    /**
     * 检测资源id在R文件中是否存在
     * @param name
     * @param type
     * @return
     */
    public boolean isIdValid(String name, String type) {
        String RClassName = mPackageNeme + ".R." + type;
        TypeElement RClassType = mElementUtils.getTypeElement(RClassName);
        if (RClassType == null) {
            mMessager.printMessage(Diagnostic.Kind.ERROR, RClassName + "不存在，请检查是否包名有误，或者类型错误");
        } else {
            //遍历属性
            for (Element element : RClassType.getEnclosedElements()) {
                String fieldName = element.getSimpleName().toString();
                if (name.equals(fieldName)) {
                    return true;
                }
            }
        }
        return false;
    }

}
