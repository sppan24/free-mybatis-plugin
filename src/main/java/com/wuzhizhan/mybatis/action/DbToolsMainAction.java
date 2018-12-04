package com.wuzhizhan.mybatis.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.project.Project;
import com.wuzhizhan.mybatis.service.TableInfoService;
import org.jetbrains.annotations.Nullable;

/**
 * 日期 2018-11-30
 *
 * @author 吴志展
 */
public class DbToolsMainAction extends AnAction {
    /**
     * 构造方法
     *
     * @param text 菜单名称
     */
    DbToolsMainAction(@Nullable String text) {
        super(text);
    }

    /**
     * 处理方法
     *
     * @param event 事件对象
     */
    @Override
    public void actionPerformed(AnActionEvent event) {
        Project project = event.getProject();

        if (project == null) {
            return;
        }

//        // 校验类型映射
//        if (!TableInfoService.getInstance(project).typeValidator(CacheDataUtils.getInstance().getSelectDbTable())) {
//            // 没通过不打开窗口
//            return;
//        }
        //开始处理
//        new SelectSavePath(event.getProject()).open();
    }
}
