package com.wuzhizhan.mybatis.action;

import com.intellij.database.psi.DbTable;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * 日期 2018-11-30
 *
 * @author 吴志展
 */
public class DbToolsMainActionGroup extends ActionGroup {

    /**
     * 缓存数据工具类
     */

    /**
     * 是否不存在子菜单
     */
    private boolean notExistsChildren;

    /**
     * 是否分组按钮
     *
     * @return 是否隐藏
     */
    @Override
    public boolean hideIfNoVisibleChildren() {
        return this.notExistsChildren;
    }


    /**
     * 根据右键在不同的选项上展示不同的子菜单
     *
     * @param event 事件对象
     * @return 动作组
     */
    @NotNull
    @Override
    public AnAction[] getChildren(@Nullable AnActionEvent event) {
        // 获取当前项目
        Project project = getEventProject(event);
        if (project == null) {
            return getEmptyAnAction();
        }

        //获取选中的PSI元素
        PsiElement psiElement = event.getData(LangDataKeys.PSI_ELEMENT);
        DbTable selectDbTable = null;
        if (psiElement instanceof DbTable) {
            selectDbTable = (DbTable) psiElement;
        }
        if (selectDbTable == null) {
            return getEmptyAnAction();
        }
        //获取选中的所有表
        PsiElement[] psiElements = event.getData(LangDataKeys.PSI_ELEMENT_ARRAY);
        if (psiElements == null || psiElements.length == 0) {
            return getEmptyAnAction();
        }
        List<DbTable> dbTableList = new ArrayList<>();
        for (PsiElement element : psiElements) {
            if (!(element instanceof DbTable)) {
                continue;
            }
            DbTable dbTable = (DbTable) element;
            dbTableList.add(dbTable);
        }
        if (dbTableList.isEmpty()) {
            return getEmptyAnAction();
        }

//        //保存数据到缓存
//        cacheDataUtils.setDbTableList(dbTableList);
//        cacheDataUtils.setSelectDbTable(selectDbTable);
        this.notExistsChildren = false;
        return getMenuList();
    }

    /**
     * 初始化注册子菜单项目
     *
     * @return 子菜单数组
     */
    private AnAction[] getMenuList() {
        String mainActionId = "com.sjhy.easy.code.action.generate";
        String configActionId = "com.sjhy.easy.code.action.config";
        ActionManager actionManager = ActionManager.getInstance();
        // 代码生成菜单
        AnAction mainAction = actionManager.getAction(mainActionId);
        if (mainAction == null) {
            mainAction = new DbToolsMainAction("MybatisGenerator Code");
            actionManager.registerAction(mainActionId, mainAction);
        }
        // 表配置菜单
        AnAction configAction = actionManager.getAction(configActionId);
        if (configAction == null) {
            configAction = new DbToolsConfigAction("Config Table");
            actionManager.registerAction(configActionId, configAction);
        }
        // 返回所有菜单
        return new AnAction[]{mainAction, configAction};
    }


    /**
     * 获取空菜单组
     *
     * @return 空菜单组
     */
    private AnAction[] getEmptyAnAction() {
        this.notExistsChildren = true;
        return AnAction.EMPTY_ARRAY;
    }
}
