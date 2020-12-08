package com.wuzhizhan.mybatis.dom.model;

import com.intellij.psi.PsiClass;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.util.xml.Attribute;
import com.intellij.util.xml.Convert;
import com.intellij.util.xml.GenericAttributeValue;
import com.wuzhizhan.mybatis.dom.converter.AliasConverter;
import com.wuzhizhan.mybatis.dom.converter.ResultMapConverter;
import org.jetbrains.annotations.NotNull;

/**
 * @author yanglin
 */
public interface ResultMap extends GroupFour, IdDomElement {

    @NotNull
    @Attribute("extends")
    @Convert(ResultMapConverter.class)
    public GenericAttributeValue<XmlAttributeValue> getExtends();

    @NotNull
    @Attribute("type")
    @Convert(AliasConverter.class)
    public GenericAttributeValue<PsiClass> getType();

}