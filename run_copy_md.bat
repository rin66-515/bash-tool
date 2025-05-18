@echo off
REM Java执行路径（如果已配置环境变量，直接写 java 即可）
set JAVA_CMD=java

REM Java类文件所在目录（改成你自己的路径）
set CLASS_PATH=D:\project\java\script\markdown-auto

REM 源目录（改成你的技术文档所在目录）
set SOURCE_DIR=C:\Users\admin\Desktop\technology blog

REM 目标目录（改成你的项目post目录）
set TARGET_DIR=C:\Users\admin\Desktop\nextjs-ts-blog\posts

REM 切换到class目录
cd /d %CLASS_PATH%

REM 执行Java程序
%JAVA_CMD% -cp %CLASS_PATH% CopyMdDaily "%SOURCE_DIR%" "%TARGET_DIR%"

pause