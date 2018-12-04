package com.wuzhizhan.mybatis.action;

import com.intellij.codeInsight.CodeInsightActionHandler;
import com.intellij.codeInsight.generation.actions.BaseGenerateAction;
import com.intellij.database.model.DasColumn;
import com.intellij.database.model.DasNamespace;
import com.intellij.database.model.DasObject;
import com.intellij.database.model.ObjectKind;
import com.intellij.database.psi.DbDataSource;
import com.intellij.database.psi.DbTable;
import com.intellij.database.util.DasUtil;
import com.intellij.database.util.DbUtil;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.util.PsiUtilBase;
import com.intellij.util.containers.JBIterable;
import com.wuzhizhan.mybatis.ui.PropertyGenerateUi;

import java.util.ArrayList;
import java.util.List;

/**
 * 日期 2018-11-30
 *
 * @author 吴志展
 */
public class DemoAction extends BaseGenerateAction {
    public DemoAction() {
        super(null);
    }

    public DemoAction(CodeInsightActionHandler handler) {
        super(handler);
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        Editor editor = e.getData(PlatformDataKeys.EDITOR);
        Project project = e.getData(PlatformDataKeys.PROJECT);
        if (editor == null || project == null) {
            return;
        }
        // 获取当前编辑器中的文件
        PsiFile psiFile = PsiUtilBase.getPsiFileInEditor(editor, project);

        //当前的文件类
        PsiClass targetClass = getTargetClass(editor, psiFile);


        WriteCommandAction.runWriteCommandAction(project, () -> {
            //创建class
            PsiElementFactory elementFactory = JavaPsiFacade.getElementFactory(project);
            PsiField fieldName = elementFactory.createFieldFromText("public String name; ", targetClass);
            PsiField fieldId = elementFactory.createField("id", PsiType.LONG);

            //将元素添加到class中
            targetClass.add(fieldId);
            targetClass.add(fieldName);

            CodeStyleManager.getInstance(project).reformat(targetClass);
        });

        JBIterable<DbDataSource> dataSources = DbUtil.getDataSources(project);
        PropertyGenerateUi ui = new PropertyGenerateUi(project);
        ui.pack();
        ui.setVisible(true);

        for (DbDataSource dataSource : dataSources) {
            JBIterable<? extends DasNamespace> schemas = DasUtil.getSchemas(dataSource);
            if (schemas.isEmpty()) {
                continue;
            }
            JBIterable<? extends DasObject> dbTables = schemas.get(0).getDasChildren(ObjectKind.TABLE);

            for (DasObject table : dbTables) {
                JBIterable<? extends DasColumn> columns = DasUtil.getColumns(table);
                for (DasColumn column : columns) {

                    System.out.println(column.getName());
                }
            }
        }
        List<DbTable> dbTableList = new ArrayList<>();


        System.out.println("size:" + dbTableList.size());
    }
}
