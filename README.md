canal-utils
=====================================

[![License](http://img.shields.io/:license-apache-brightgreen.svg)](http://www.apache.org/licenses/LICENSE-2.0.html)
[![Build Status](https://github.com/wz2cool/canal-utils/actions/workflows/test.yml/badge.svg?branch=master)](https://github.com/wz2cool/canal-utils/actions/workflows/test.yml)
[![Coverage Status](https://coveralls.io/repos/github/wz2cool/canal-utils/badge.svg?branch=master)](https://coveralls.io/github/wz2cool/canal-utils?branch=master)
[![Maven central](https://maven-badges.herokuapp.com/maven-central/com.github.wz2cool/canal-utils/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.wz2cool/canal-utils)

# 简介
本项目是服务阿里同步项目 [canal](https://github.com/alibaba/canal) 的一个工具箱帮助类库。

# Maven
${version} 占位符可以用上面 maven central 中对应的版本替换
```xml
<dependency>
    <groupId>com.github.wz2cool</groupId>
    <artifactId>canal-utils</artifactId>
    <version>${version}</version>
</dependency>
```

# 功能
## 转化器
Mysql DDL 语句转化到不同数据库上，支持：db2, hive, mssql, oracle, postgresql. [wiki文档](https://github.com/wz2cool/canal-utils/wiki/converter)
