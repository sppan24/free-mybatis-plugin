[中文](README.md) | 
[ENGLISH](README_EN.md)

## Description
A idea plugin for mybatis .
free-idea-mybatis是一款增强idea对mybatis支持的插件，主要功能如下：
- 生成mapper xml文件
- 快速从代码跳转到mapper及从mapper返回代码
- mybatis自动补全及语法错误提示

## 使用方法
free-idea-mybatis是一个提高mybatis编码的插件。实现了dao代码跳转到mapper，mapper跳转回dao，mapper文件、statement查询自动生成功能。
### 灵活使用alt+enter和ctrl+B实现提示和跳转
- 生成mapper文件
    - alter+enter弹出
![](https://raw.githubusercontent.com/wuzhizhan/free-idea-mybatis/master/doc/img/create_mapper.png)
![](https://raw.githubusercontent.com/wuzhizhan/free-idea-mybatis/master/doc/img/choose_mapper_folder.jpg)
- 生成statement语句
    - alter+enter弹出
![](https://raw.githubusercontent.com/wuzhizhan/free-idea-mybatis/master/doc/img/create_statement.jpg)
- dao跳转到mapper（也可以ctrl+B跳入）
- ![](https://raw.githubusercontent.com/wuzhizhan/free-idea-mybatis/master/doc/img/to_mapper.jpg)
- mapper跳转回dao（也可以ctrl+b跳入)
- ![](https://raw.githubusercontent.com/wuzhizhan/free-idea-mybatis/master/doc/img/to_code.jpg)
<br><br><br>
## mybatis generator gui使用方法
- 配置数据库<br>
![](https://github.com/wuzhizhan/free-idea-mybatis/blob/master/doc/img/mgu_1.png)
![](https://github.com/wuzhizhan/free-idea-mybatis/blob/master/doc/img/mgu_2.png)
- 在需要生成代码的表上右键，选择mybatis generator，打开预览界面。 <br>
![](https://github.com/wuzhizhan/free-idea-mybatis/blob/master/doc/img/mgu_3.png)
- 配置生成参数<br>
![](https://github.com/wuzhizhan/free-idea-mybatis/blob/master/doc/img/mgu_4.png)
- 注意：当数据库用mysql8，在URL上定义时区，推荐使用'?serverTimezone=GMT'，配置中勾选上mysql8选项。<br>
   
## TODO
- 根据数据库字段生产bean
- 快捷生成CURD操作

## 参考
- better-mybatis-generator https://github.com/kmaster/better-mybatis-generator
- mybatis-generator-gui https://github.com/zouzg/mybatis-generator-gui
- MyBatisCodeHelper-Pro https://github.com/gejun123456/MyBatisCodeHelper-Pro
## 我的微信公众号 - it新技术
更多内容，欢迎关注公众号 【it新技术】
- ![](https://raw.githubusercontent.com/wuzhizhan/free-idea-mybatis/master/doc/img/gzh_logo.jpg)
![](https://raw.githubusercontent.com/wuzhizhan/free-idea-mybatis/master/doc/img/gzh_qrcode.jpg)