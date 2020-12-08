package com.wuzhizhan.mybatis.dom.model;

import com.intellij.psi.PsiClass;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.xml.Attribute;
import com.intellij.util.xml.Convert;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.SubTagList;
import com.wuzhizhan.mybatis.dom.converter.AliasConverter;
import com.wuzhizhan.mybatis.dom.converter.DaoMethodConverter;
import com.wuzhizhan.mybatis.dom.converter.ParameterMapConverter;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * @author yanglin
 */
public interface GroupTwo extends GroupOne, IdDomElement {

    @SubTagList("bind")
    public List<Bind> getBinds();

    @NotNull
    @Attribute("parameterMap")
    @Convert(ParameterMapConverter.class)
    public GenericAttributeValue<XmlTag> getParameterMap();

    @Attribute("id")
    @Convert(DaoMethodConverter.class)
    public GenericAttributeValue<String> getId();

    @NotNull
    @Attribute("parameterType")
    @Convert(AliasConverter.class)
    public GenericAttributeValue<PsiClass> getParameterType();
}
