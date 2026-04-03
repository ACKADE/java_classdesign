# SimpleClassManager JDBC 版

这个项目已经从内存数组改为通过 JDBC 读取 MySQL 数据。

## 1. 数据库准备

应用启动时会自动执行以下动作：
- 创建数据库 `text`
- 创建表 `cs_students`、`cs_teacher`
- 插入示例数据（使用 `INSERT IGNORE`，可重复启动）

你也可以手动执行 `database_init.sql`。

## 2. 修改数据库账号

在 `untitled/src/DBUtil.java` 中修改：
- `USER`
- `PASSWORD`

## 3. 编译运行（PowerShell）

```powershell
Set-Location "C:\Users\32654\Desktop\untitled\untitled\src"
javac -encoding UTF-8 -cp ".;..\..\lib\mysql-connector-j-9.6.0.jar" *.java
java -cp ".;..\..\lib\mysql-connector-j-9.6.0.jar" SimpleClassManager
```

## 4. 界面使用

左侧树形导航：
- `学生表 cs_students` -> 选择专业，查询该专业学生
- `教师表 cs_teacher` -> 选择专业，查询该专业教师

