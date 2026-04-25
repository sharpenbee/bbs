@echo off
chcp 65001 >nul
echo ========================================
echo 论坛项目本地启动脚本
echo ========================================
echo.

REM 设置Java环境变量（如果需要）
REM set JAVA_HOME=C:\Program Files\Java\jdk-21
REM set PATH=%JAVA_HOME%\bin;%PATH%

echo 检查Java版本...
java -version
if %errorlevel% neq 0 (
    echo 错误: 未找到Java 21，请先安装JDK 21
    echo 下载地址: https://adoptium.net/temurin/releases/?version=21
    pause
    exit /b 1
)

echo.
echo 构建项目...
call mvn clean package -DskipTests
if %errorlevel% neq 0 (
    echo 错误: 项目构建失败
    pause
    exit /b 1
)

echo.
echo 检查是否存在可执行jar包...
if not exist "target\bbs-jdk21-v7.0.jar" (
    echo 错误: 未找到可执行jar包
    pause
    exit /b 1
)

echo.
echo ========================================
echo 启动配置说明:
echo.
echo 1. 确保MySQL数据库已启动，并且创建了名为 'bbs-jdk21' 的数据库
echo 2. 确保Redis服务已启动（如果需要使用Redis缓存）
echo 3. 数据库连接信息在 src\main\resources\application.yml 中配置
echo.
echo 默认配置:
echo - 数据库地址: 127.0.0.1:3306
echo - 数据库名称: bbs-jdk21
echo - 数据库用户名: root
echo - Redis地址: 127.0.0.1:6379
echo.
echo 如果需要修改配置，请编辑 application.yml 文件
echo ========================================
echo.

set /p startNow="是否现在启动应用? (y/n): "
if /i not "%startNow%"=="y" (
    echo 已取消启动
    pause
    exit /b 0
)

echo.
echo 启动应用...
echo JVM参数: -Xms256m -Xmx512m
echo.

java -Xms256m -Xmx512m -jar target\bbs-jdk21-v7.0.jar

pause
