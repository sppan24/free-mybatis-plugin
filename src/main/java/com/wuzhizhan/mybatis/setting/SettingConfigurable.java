package com.wuzhizhan.mybatis.setting;

import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.openapi.project.Project;
import com.wuzhizhan.mybatis.ui.SettingUI;
import javax.swing.JComponent;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 *配置设置界面
 * Created by kangtian on 2018/7/18.
 */
public class SettingConfigurable implements SearchableConfigurable {
    private SettingUI mainPanel;

    @SuppressWarnings("FieldCanBeLocal")
    private final Project project;


    public SettingConfigurable(@NotNull Project project) {
        this.project = project;
    }

    @Nls
    @Override
    public String getDisplayName() {
        return "Mybatis gene plugin";
    }

    @Nullable
    @Override
    public String getHelpTopic() {
        return "gene.helpTopic";
    }

    @NotNull
    @Override
    public String getId() {
        return "gene.id";
    }

    @Nullable
    @Override
    public Runnable enableSearch(String s) {
        return null;
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        mainPanel = new SettingUI();
        mainPanel.createUI(project);
        return mainPanel.getContentPane();
    }

    @Override
    public boolean isModified() {
        return mainPanel.isModified();
    }

    @Override
    public void apply() throws ConfigurationException {
        mainPanel.apply();
    }

    @Override
    public void reset() {
        mainPanel.reset();
    }

    @Override
    public void disposeUIResources() {
        mainPanel = null;
    }
}
