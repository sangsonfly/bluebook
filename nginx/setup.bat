@echo off
chcp 65001 >nul
echo ========================================
echo   BlueBook Nginx 环境初始化
echo ========================================
echo.

if not exist "nginx.exe" (
    echo [1/2] 下载 Nginx Windows 版...
    curl -L -o nginx-temp.zip "https://nginx.org/download/nginx-1.26.3.zip"
    echo [2/2] 解压...
    powershell -Command "Expand-Archive -Path nginx-temp.zip -DestinationPath . -Force"
    move nginx-1.26.3\nginx.exe nginx.exe
    rd /s /q nginx-1.26.3
    del nginx-temp.zip
    echo 完成！Nginx 已就绪。
) else (
    echo Nginx 已存在，跳过下载。
)

echo.
echo 启动方式：
echo   nginx.exe                启动 Nginx
echo   nginx.exe -s reload      重载配置
echo   nginx.exe -s stop        停止 Nginx
echo.
echo 默认端口 80，浏览器访问 http://localhost
pause
