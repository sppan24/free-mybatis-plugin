package com.wuzhizhan.mybatis.contributor;

import com.google.common.base.Optional;
import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.project.Project;
import com.intellij.patterns.XmlPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiIdentifier;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiParameter;
import com.intellij.util.ProcessingContext;
import com.wuzhizhan.mybatis.annotation.Annotation;
import com.wuzhizhan.mybatis.dom.model.IdDomElement;
import com.wuzhizhan.mybatis.util.Icons;
import com.wuzhizhan.mybatis.util.JavaUtils;
import com.wuzhizhan.mybatis.util.MapperUtils;
import com.wuzhizhan.mybatis.util.MybatisConstants;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author yanglin
 */
public class TestParamContributor extends CompletionContributor {
    private static final Logger logger = LoggerFactory.getLogger(TestParamContributor.class);

    public TestParamContributor() {
        extend(CompletionType.BASIC,
                XmlPatterns.psiElement()
                           .inside(XmlPatterns.xmlAttributeValue()
                                              .inside(XmlPatterns.xmlAttribute().withName("test"))),
                new CompletionProvider<CompletionParameters>() {
                    @Override
                    protected void addCompletions(
                            @NotNull final CompletionParameters parameters,
                            final ProcessingContext context,
                            @NotNull final CompletionResultSet result) {
                        final PsiElement position = parameters.getPosition();
                        addElementForPsiParameter(
                                position.getProject(),
                                result,
                                MapperUtils.findParentIdDomElement(position).orNull());
                    }
                });
    }

    static void addElementForPsiParameter(
            @NotNull final Project project,
            @NotNull final CompletionResultSet result,
            @Nullable final IdDomElement element) {
        if (element == null) {
            return;
        }

        final PsiMethod method = JavaUtils.findMethod(project, element).orNull();

        if (method == null) {
            logger.info("psiMethod null");
            return;
        }

        final PsiParameter[] parameters = method.getParameterList().getParameters();

        // For a single parameter MyBatis uses its name, while for a multitude they're
        // named as param1, param2, etc. I'll check if the @Param annotation [value] is present
        // and eventually I'll use its text.
        if (parameters.length == 1) {
            final PsiIdentifier identifier = parameters[0].getNameIdentifier();

            if (identifier != null) {
                result.addElement(buildLookupElementWithIcon(identifier.getText()));
            }
        } else {
            for (int i = 0; i < parameters.length; i++) {
                final Optional<String> value = JavaUtils.getAnnotationValueText(parameters[i], Annotation.PARAM);
                result.addElement(buildLookupElementWithIcon(value.isPresent() ? value.get() : "param" + (i + 1)));
            }
        }
    }

    private static LookupElement buildLookupElementWithIcon(final String text) {
        return PrioritizedLookupElement.withPriority(
                LookupElementBuilder.create(text).withIcon(Icons.PARAM_COMPLETION_ICON),
                MybatisConstants.PRIORITY);
    }
}
