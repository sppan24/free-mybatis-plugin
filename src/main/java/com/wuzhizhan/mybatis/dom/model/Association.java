package com.wuzhizhan.mybatis.dom.model;

import com.intellij.psi.PsiClass;
import com.intellij.util.xml.Attribute;
import com.intellij.util.xml.Convert;
import com.intellij.util.xml.GenericAttributeValue;
import com.wuzhizhan.mybatis.dom.converter.AliasConverter;

import org.jetbrains.annotations.NotNull;

/**
 * @author yanglin
 */
public interface Association extends GroupFour, ResultMapGroup, PropertyGroup {

    @NotNull
    @Attribute("javaType")
    @Convert(AliasConverter.class)
    public GenericAttributeValue<PsiClass> getJavaType();
}
