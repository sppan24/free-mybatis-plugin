Based on <https://gitee.com/wuzhizhan/free-mybatis-plugin> support to idea 2020.2.4。

[中文](README.md) | 
[ENGLISH](README_EN.md)


## Description
A idea plugin for mybatis .
free-mybatis-plugin is an enchange plugin for idea to supoort mybatis,here is the main functions: 
- generate mapper xml files
- navigate from the code to mapper and from the mapper back to code
- auto code and error tips

## How To Use
- generate mapper files
    - alter+enter 
![](https://raw.githubusercontent.com/wuzhizhan/free-idea-mybatis/master/doc/img/create_mapper.png)
![](https://raw.githubusercontent.com/wuzhizhan/free-idea-mybatis/master/doc/img/choose_mapper_folder.jpg)
- generate statement 
    - alter+enter
![](https://raw.githubusercontent.com/wuzhizhan/free-idea-mybatis/master/doc/img/create_statement.jpg)
- from dao to  mapper（can also use ctrl+B）
![](https://raw.githubusercontent.com/wuzhizhan/free-idea-mybatis/master/doc/img/to_mapper.jpg)
- from mapper to dao（can also use ctrl+b)
![](https://raw.githubusercontent.com/wuzhizhan/free-idea-mybatis/master/doc/img/to_code.jpg)


## mybatis generator gui usage
- database configuration<br>
![](https://github.com/wuzhizhan/free-idea-mybatis/blob/master/doc/img/mgu_1.png)
![](https://github.com/wuzhizhan/free-idea-mybatis/blob/master/doc/img/mgu_2.png)
- elect one or more tables,right click and select mybatis generator to open generatoe main UI.<br>
![](https://github.com/wuzhizhan/free-idea-mybatis/blob/master/doc/img/mgu_3.png)
- mybatis generator configuration<br>
![](https://github.com/wuzhizhan/free-idea-mybatis/blob/master/doc/img/mgu_4.png)
- notice：If your database is mysql8，please add '?serverTimezone=GMT' and select mysql8 option<br>

## Reference
- better-mybatis-generator https://github.com/kmaster/better-mybatis-generator
- mybatis-generator-gui https://github.com/zouzg/mybatis-generator-gui
- MyBatisCodeHelper-Pro https://github.com/gejun123456/MyBatisCodeHelper-Pro
## TODO
- generate bean accourding to database field
- generate CURD code
