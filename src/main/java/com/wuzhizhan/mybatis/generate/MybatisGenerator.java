package com.wuzhizhan.mybatis.generate;


import cn.kt.DbRemarksCommentGenerator;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.intellij.credentialStore.CredentialAttributes;
import com.intellij.database.model.RawConnectionConfig;
import com.intellij.database.psi.DbDataSource;
import com.intellij.database.psi.DbElement;
import com.intellij.database.psi.DbTable;
import com.intellij.database.psi.DbTableImpl;
import com.intellij.ide.passwordSafe.PasswordSafe;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.PsiElement;
import com.mysql.cj.conf.ConnectionUrlParser;
import com.wuzhizhan.mybatis.model.Config;
import com.wuzhizhan.mybatis.model.DbType;
import com.wuzhizhan.mybatis.model.User;
import com.wuzhizhan.mybatis.setting.PersistentConfig;
import com.wuzhizhan.mybatis.ui.MybatisGeneratorPasswordUI;
import com.wuzhizhan.mybatis.util.GeneratorCallback;
import com.wuzhizhan.mybatis.util.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.mybatis.generator.api.MyBatisGenerator;
import org.mybatis.generator.api.ShellCallback;
import org.mybatis.generator.config.*;
import org.mybatis.generator.internal.DefaultShellCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.*;

/**
 * 生成mybatis相关代码
 */
public class MybatisGenerator {
    private static Logger logger = LoggerFactory.getLogger(MybatisGenerator.class);
    private AnActionEvent anActionEvent;
    private Project project;
    private PersistentConfig persistentConfig;//持久化的配置
    private Config config;//界面默认配置
    private DbType dbType;//数据库类型
    private String driverClass;//数据库驱动
    private String url;//数据库连接url
    private String urlKey;
    public MybatisGenerator(Config config) {
        this.config = config;
    }

    /**
     * 自动生成的主逻辑
     *
     * @param anActionEvent
     * @throws Exception
     */
    public void execute(final AnActionEvent anActionEvent, boolean saveConfig) throws Exception {
        this.anActionEvent = anActionEvent;
        this.project = anActionEvent.getData(PlatformDataKeys.PROJECT);
        this.persistentConfig = PersistentConfig.getInstance(project);

        if (saveConfig) {
            saveConfig();//执行前 先保存一份当前配置
        }
        final PsiElement[] psiElements = anActionEvent.getData(LangDataKeys.PSI_ELEMENT_ARRAY);

        if (psiElements == null || psiElements.length == 0) {
            return;
        }
        RawConnectionConfig connectionConfig = ((DbDataSource) psiElements[0].getParent().getParent()).getConnectionConfig();
        String currentDbName = ((DbTableImpl) psiElements[0]).getParent().getName();
        driverClass = connectionConfig.getDriverClass();
        url = connectionConfig.getUrl();
        urlKey = url;
        if (driverClass.contains("mysql")) {
            dbType = DbType.MySQL;
            ConnectionUrlParser parser = ConnectionUrlParser.parseConnectionString(url);
            //schema名不同，则替换
            if (!currentDbName.equals(parser.getPath())) {
                url = parser.getScheme()+"//"+parser.getAuthority()+"/"+currentDbName;
                if (!Strings.isNullOrEmpty(parser.getQuery())) {
                    url += "?"+parser.getQuery();

                }
            }
        } else if (driverClass.contains("oracle")) {
            dbType = DbType.Oracle;
        } else if (driverClass.contains("postgresql")) {
            dbType = DbType.PostgreSQL;
        } else if (driverClass.contains("sqlserver")) {
            dbType = DbType.SqlServer;
        } else if (driverClass.contains("mariadb")) {
            url= url.replaceFirst("mariadb","mysql");
            ConnectionUrlParser parser = ConnectionUrlParser.parseConnectionString(url);
            //schema名不同，则替换
            if (!currentDbName.equals(parser.getPath())) {
                url = "jdbc:mariadb://"+parser.getAuthority()+"/"+currentDbName;
                if (!Strings.isNullOrEmpty(parser.getQuery())) {
                    url += "?"+parser.getQuery();

                }
            }
            dbType = DbType.MariaDB;
        } else {
            String failMessage = String.format("db type not support!" +
                            "\n your driver class:%s" +
                            "\n current support db type:mysql，mariadb，oracle,postgresql",
                    driverClass);
            Messages.showMessageDialog(project, failMessage,
                    "Test Connection Error", Messages.getInformationIcon());
            return;
        }

        //用后台任务执行代码生成
        ApplicationManager.getApplication().invokeLater(new Runnable() {
            @Override
            public void run() {
                ProgressManager
                        .getInstance().run(new Task.Backgroundable(project, "mybatis generating...") {
                    @Override
                    public void run(@NotNull ProgressIndicator indicator) {

                        for (PsiElement psiElement : psiElements) {
                            if (!(psiElement instanceof DbTable)) {
                                continue;
                            }
                            Configuration configuration = new Configuration();
                            Context context = new Context(ModelType.CONDITIONAL);
                            configuration.addContext(context);

                            context.setId("myid");
                            context.addProperty("autoDelimitKeywords", "true");
                            context.addProperty("beginningDelimiter", "`");
                            context.addProperty("endingDelimiter", "`");
                            context.addProperty("javaFileEncoding", "UTF-8");
                            context.addProperty(PropertyRegistry.CONTEXT_JAVA_FILE_ENCODING, "UTF-8");
                            context.setTargetRuntime("MyBatis3");

                            JDBCConnectionConfiguration jdbcConfig = buildJdbcConfig(psiElement);
                            if (jdbcConfig == null) {
                                return;
                            }
                            TableConfiguration tableConfig = buildTableConfig(psiElement, context);
                            JavaModelGeneratorConfiguration modelConfig = buildModelConfig();
                            SqlMapGeneratorConfiguration mapperConfig = buildMapperXmlConfig();
                            JavaClientGeneratorConfiguration daoConfig = buildDaoConfig();
                            CommentGeneratorConfiguration commentConfig = buildCommentConfig();

                            context.addTableConfiguration(tableConfig);
                            context.setJdbcConnectionConfiguration(jdbcConfig);
                            context.setJavaModelGeneratorConfiguration(modelConfig);
                            context.setSqlMapGeneratorConfiguration(mapperConfig);
                            context.setJavaClientGeneratorConfiguration(daoConfig);
                            context.setCommentGeneratorConfiguration(commentConfig);
                            addPluginConfiguration(psiElement, context);

                            createFolderForNeed(config);
                            List<String> warnings = new ArrayList<>();
                            ShellCallback shellCallback = new DefaultShellCallback(true); // override=true
                            Set<String> fullyqualifiedTables = new HashSet<>();
                            Set<String> contexts = new HashSet<>();
                            try {
                                MyBatisGenerator myBatisGenerator = new MyBatisGenerator(configuration, shellCallback, warnings);
                                myBatisGenerator.generate(new GeneratorCallback(), contexts, fullyqualifiedTables);
                                if (!warnings.isEmpty()) {
                                    logger.error("generator has some warnnings", Joiner.on("\n").join(warnings));
                                }
                            } catch (Exception e) {
                                //                                Messages.showMessageDialog(e.getMessage() + " if use mysql,check version8?", "MybatisGenerator failure", Messages.getInformationIcon());
                                logger.error("生成错误", e);

                            }
                            project.getBaseDir().refresh(false, true);
                        }
                    }
                });
            }
        });
    }

    /**
     * 创建所需目录
     *
     * @param config
     */
    private void createFolderForNeed(Config config) {

        String modelMvnPath = config.getModelMvnPath();
        String daoMvnPath = config.getDaoMvnPath();
        String xmlMvnPath = config.getXmlMvnPath();

        String modelPath = config.getProjectFolder() + "/" + modelMvnPath + "/";
        String daoPath = config.getProjectFolder() + "/" + daoMvnPath + "/";
        String xmlPath = config.getProjectFolder() + "/" + xmlMvnPath + "/";

        File modelFile = new File(modelPath);
        if (!modelFile.exists() && !modelFile.isDirectory()) {
            modelFile.mkdirs();
        }

        File daoFile = new File(daoPath);
        if (!daoFile.exists() && !daoFile.isDirectory()) {
            daoFile.mkdirs();
        }

        File xmlFile = new File(xmlPath);
        if (!xmlFile.exists() && !xmlFile.isDirectory()) {
            xmlFile.mkdirs();
        }

    }


    /**
     * 保存当前配置到历史记录
     */
    private void saveConfig() {
        Map<String, Config> historyConfigList = persistentConfig.getHistoryConfigList();
        if (historyConfigList == null) {
            historyConfigList = new HashMap<>();
        }

        String daoName = config.getDaoName();
        String modelName = config.getModelName();
        String daoPostfix = daoName.replace(modelName, "");
        config.setDaoPostfix(daoPostfix);

        historyConfigList.put(config.getName(), config);
        persistentConfig.setHistoryConfigList(historyConfigList);

    }

    /**
     * 生成数据库连接配置
     *
     * @param psiElement
     * @return
     */
    private JDBCConnectionConfiguration buildJdbcConfig(PsiElement psiElement) {

        JDBCConnectionConfiguration jdbcConfig = new JDBCConnectionConfiguration();
        jdbcConfig.addProperty("nullCatalogMeansCurrent", "true");


        Map<String, User> users = persistentConfig.getUsers();
        if (users != null && users.containsKey(urlKey)) {
            User user = users.get(urlKey);

            String username = user.getUsername();

            CredentialAttributes attributes_get = new CredentialAttributes("better-mybatis-generator-" + urlKey, username, this.getClass(), false);
            String password = PasswordSafe.getInstance().getPassword(attributes_get);
            if (StringUtils.isEmpty(password)) {
                new MybatisGeneratorPasswordUI(driverClass, urlKey, anActionEvent);
                return null;
            }

            jdbcConfig.setUserId(username);
            jdbcConfig.setPassword(password);

            jdbcConfig.setDriverClass(driverClass);
            jdbcConfig.setConnectionURL(url);
            return jdbcConfig;
        } else {
            new MybatisGeneratorPasswordUI(driverClass, urlKey, anActionEvent);
            return null;
        }

    }

    /**
     * 生成table配置
     *
     * @param psiElement
     * @param context
     * @return
     */
    private TableConfiguration buildTableConfig(PsiElement psiElement, Context context) {
        TableConfiguration tableConfig = new TableConfiguration(context);
        tableConfig.setTableName(config.getTableName());
        tableConfig.setDomainObjectName(config.getModelName());

        DbElement parent = ((DbTableImpl) psiElement).getParent();
        String schema;
        if (parent != null) {
            schema = parent.getName();
        } else {
            throw new RuntimeException("can not find schema");

        }
        if (dbType.equals(DbType.MySQL)) {
            tableConfig.setSchema(schema);
        } else {
            tableConfig.setCatalog(schema);
        }

        if (!config.isUseExample()) {
            tableConfig.setUpdateByExampleStatementEnabled(false);
            tableConfig.setCountByExampleStatementEnabled(false);
            tableConfig.setDeleteByExampleStatementEnabled(false);
            tableConfig.setSelectByExampleStatementEnabled(false);
        }
        if (config.isUseSchemaPrefix()) {
            if (DbType.MySQL.equals(dbType)) {
                tableConfig.setSchema(schema);
            } else if (DbType.Oracle.equals(dbType)) {
                tableConfig.setSchema(schema);
            } else {
                tableConfig.setCatalog(schema);
            }
        }

        if (DbType.PostgreSQL.equals(dbType)) {
            tableConfig.setDelimitIdentifiers(true);
        }

        if (!StringUtils.isEmpty(config.getPrimaryKey())) {
            if (DbType.MySQL.equals(dbType)) {
                //dbType为JDBC，且配置中开启useGeneratedKeys时，Mybatis会使用Jdbc3KeyGenerator,
                //使用该KeyGenerator的好处就是直接在一次INSERT 语句内，通过resultSet获取得到 生成的主键值，
                //并很好的支持设置了读写分离代理的数据库
                //例如阿里云RDS + 读写分离代理 无需指定主库
                //当使用SelectKey时，Mybatis会使用SelectKeyGenerator，INSERT之后，多发送一次查询语句，获得主键值
                //在上述读写分离被代理的情况下，会得不到正确的主键
            }
            tableConfig.setGeneratedKey(new GeneratedKey(config.getPrimaryKey(), "JDBC", true, null));
        }

        if (config.isUseActualColumnNames()) {
            tableConfig.addProperty("useActualColumnNames", "true");
        }

        if (config.isUseTableNameAlias()) {
            tableConfig.setAlias(config.getTableName());
        }

//        if (ignoredColumns != null) {
//            ignoredColumns.stream().forEach(new Consumer<IgnoredColumn>() {
//                @Override
//                public void accept(IgnoredColumn ignoredColumn) {
//                    tableConfig.addIgnoredColumn(ignoredColumn);
//                }
//            });
//        }
//        if (columnOverrides != null) {
//            for (ColumnOverride columnOverride : columnOverrides) {
//                tableConfig.addColumnOverride(columnOverride);
//            }
//        }

        tableConfig.setMapperName(config.getDaoName());
        return tableConfig;
    }


    /**
     * 生成实体类配置
     *
     * @return
     */
    private JavaModelGeneratorConfiguration buildModelConfig() {
        String projectFolder = config.getProjectFolder();
        String modelPackage = config.getModelPackage();
        String modelPackageTargetFolder = config.getModelTargetFolder();
        String modelMvnPath = config.getModelMvnPath();

        JavaModelGeneratorConfiguration modelConfig = new JavaModelGeneratorConfiguration();

        if (!StringUtils.isEmpty(modelPackage)) {
            modelConfig.setTargetPackage(modelPackage);
        } else {
            modelConfig.setTargetPackage("generator");
        }

        if (!StringUtils.isEmpty(modelPackageTargetFolder)) {
            modelConfig.setTargetProject(modelPackageTargetFolder + "/" + modelMvnPath + "/");
        } else {
            modelConfig.setTargetProject(projectFolder + "/" + modelMvnPath + "/");
        }
        return modelConfig;
    }

    /**
     * 生成mapper.xml文件配置
     *
     * @return
     */
    private SqlMapGeneratorConfiguration buildMapperXmlConfig() {

        String projectFolder = config.getProjectFolder();
        String mappingXMLPackage = config.getXmlPackage();
        String mappingXMLTargetFolder = config.getXmlTargetFolder();
        String xmlMvnPath = config.getXmlMvnPath();

        SqlMapGeneratorConfiguration mapperConfig = new SqlMapGeneratorConfiguration();

        if (!StringUtils.isEmpty(mappingXMLPackage)) {
            mapperConfig.setTargetPackage(mappingXMLPackage);
        } else {
            mapperConfig.setTargetPackage("generator");
        }

        if (!StringUtils.isEmpty(mappingXMLTargetFolder)) {
            mapperConfig.setTargetProject(mappingXMLTargetFolder + "/" + xmlMvnPath + "/");
        } else {
            mapperConfig.setTargetProject(projectFolder + "/" + xmlMvnPath + "/");
        }

        if (config.isOverrideXML()) {//14
            String mappingXMLFilePath = getMappingXMLFilePath(config);
            File mappingXMLFile = new File(mappingXMLFilePath);
            if (mappingXMLFile.exists()) {
                mappingXMLFile.delete();
            }
        }

        return mapperConfig;
    }

    /**
     * 生成dao接口文件配置
     *
     * @return
     */
    private JavaClientGeneratorConfiguration buildDaoConfig() {

        String projectFolder = config.getProjectFolder();
        String daoPackage = config.getDaoPackage();
        String daoTargetFolder = config.getDaoTargetFolder();
        String daoMvnPath = config.getDaoMvnPath();

        JavaClientGeneratorConfiguration daoConfig = new JavaClientGeneratorConfiguration();
        daoConfig.setConfigurationType("XMLMAPPER");
        daoConfig.setTargetPackage(daoPackage);

        if (!StringUtils.isEmpty(daoPackage)) {
            daoConfig.setTargetPackage(daoPackage);
        } else {
            daoConfig.setTargetPackage("generator");
        }

        if (!StringUtils.isEmpty(daoTargetFolder)) {
            daoConfig.setTargetProject(daoTargetFolder + "/" + daoMvnPath + "/");
        } else {
            daoConfig.setTargetProject(projectFolder + "/" + daoMvnPath + "/");
        }

        return daoConfig;
    }

    /**
     * 生成注释配置
     *
     * @return
     */
    private CommentGeneratorConfiguration buildCommentConfig() {
        CommentGeneratorConfiguration commentConfig = new CommentGeneratorConfiguration();
        commentConfig.setConfigurationType(DbRemarksCommentGenerator.class.getName());

        if (config.isComment()) {
            commentConfig.addProperty("columnRemarks", "true");
        }
        if (config.isAnnotation()) {
            commentConfig.addProperty("annotations", "true");
        }

        return commentConfig;
    }

    /**
     * 添加相关插件（注意插件文件需要通过jar引入）
     *
     * @param context
     */
    private void addPluginConfiguration(PsiElement psiElement, Context context) {


        //实体添加序列化
        PluginConfiguration serializablePlugin = new PluginConfiguration();
        serializablePlugin.addProperty("type", "org.mybatis.generator.plugins.SerializablePlugin");
        serializablePlugin.setConfigurationType("org.mybatis.generator.plugins.SerializablePlugin");
        context.addPluginConfiguration(serializablePlugin);


        if (config.isNeedToStringHashcodeEquals()) {
            PluginConfiguration equalsHashCodePlugin = new PluginConfiguration();
            equalsHashCodePlugin.addProperty("type", "org.mybatis.generator.plugins.EqualsHashCodePlugin");
            equalsHashCodePlugin.setConfigurationType("org.mybatis.generator.plugins.EqualsHashCodePlugin");
            context.addPluginConfiguration(equalsHashCodePlugin);
            PluginConfiguration toStringPluginPlugin = new PluginConfiguration();
            toStringPluginPlugin.addProperty("type", "org.mybatis.generator.plugins.ToStringPlugin");
            toStringPluginPlugin.setConfigurationType("org.mybatis.generator.plugins.ToStringPlugin");
            context.addPluginConfiguration(toStringPluginPlugin);
        }

        // limit/offset插件
        if (config.isOffsetLimit()) {
            if (DbType.MySQL.equals(dbType)
                    || DbType.PostgreSQL.equals(dbType)) {
                PluginConfiguration mySQLLimitPlugin = new PluginConfiguration();
                mySQLLimitPlugin.addProperty("type", "cn.kt.MySQLLimitPlugin");
                mySQLLimitPlugin.setConfigurationType("cn.kt.MySQLLimitPlugin");
                context.addPluginConfiguration(mySQLLimitPlugin);
            }
        }

        //for JSR310
        if (config.isJsr310Support()) {
            JavaTypeResolverConfiguration javaTypeResolverPlugin = new JavaTypeResolverConfiguration();
            javaTypeResolverPlugin.setConfigurationType("cn.kt.JavaTypeResolverJsr310Impl");
            context.setJavaTypeResolverConfiguration(javaTypeResolverPlugin);
        }

        //forUpdate 插件
        if (config.isNeedForUpdate()) {
            if (DbType.MySQL.equals(dbType)
                    || DbType.PostgreSQL.equals(dbType)) {
                PluginConfiguration mySQLForUpdatePlugin = new PluginConfiguration();
                mySQLForUpdatePlugin.addProperty("type", "cn.kt.MySQLForUpdatePlugin");
                mySQLForUpdatePlugin.setConfigurationType("cn.kt.MySQLForUpdatePlugin");
                context.addPluginConfiguration(mySQLForUpdatePlugin);
            }
        }

        //repository 插件
        if (config.isAnnotationDAO()) {
            if (DbType.MySQL.equals(dbType)
                    || DbType.PostgreSQL.equals(dbType)) {
                PluginConfiguration repositoryPlugin = new PluginConfiguration();
                repositoryPlugin.addProperty("type", "cn.kt.RepositoryPlugin");
                repositoryPlugin.setConfigurationType("cn.kt.RepositoryPlugin");
                context.addPluginConfiguration(repositoryPlugin);
            }
        }

        if (config.isUseDAOExtendStyle()) {//13
            if (DbType.MySQL.equals(dbType)
                    || DbType.PostgreSQL.equals(dbType)) {
                PluginConfiguration commonDAOInterfacePlugin = new PluginConfiguration();
                commonDAOInterfacePlugin.addProperty("type", "cn.kt.CommonDAOInterfacePlugin");
                commonDAOInterfacePlugin.setConfigurationType("cn.kt.CommonDAOInterfacePlugin");
                context.addPluginConfiguration(commonDAOInterfacePlugin);
            }
        }

    }

    /**
     * 获取xml文件路径 用以删除之前的xml
     *
     * @param config
     * @return
     */
    private String getMappingXMLFilePath(Config config) {
        StringBuilder sb = new StringBuilder();
        String mappingXMLPackage = config.getXmlPackage();
        String mappingXMLTargetFolder = config.getXmlTargetFolder();
        String xmlMvnPath = config.getXmlMvnPath();
        sb.append(mappingXMLTargetFolder + "/" + xmlMvnPath + "/");

        if (!StringUtils.isEmpty(mappingXMLPackage)) {
            sb.append(mappingXMLPackage.replace(".", "/")).append("/");
        }
        if (!StringUtils.isEmpty(config.getDaoName())) {
            sb.append(config.getDaoName()).append(".xml");
        } else {
            sb.append(config.getModelName()).append("Dao.xml");
        }

        return sb.toString();
    }
}
