package com.wuzhizhan.mybatis.dom.model;

import com.intellij.psi.xml.XmlTag;
import com.intellij.util.xml.Attribute;
import com.intellij.util.xml.Convert;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericAttributeValue;
import com.wuzhizhan.mybatis.dom.converter.ResultMapConverter;
import org.jetbrains.annotations.NotNull;

/**
 * @author yanglin
 */
public interface ResultMapGroup extends DomElement {

    @NotNull
    @Attribute("resultMap")
    @Convert(ResultMapConverter.class)
    public GenericAttributeValue<XmlTag> getResultMap();
}
