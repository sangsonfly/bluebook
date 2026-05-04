# Nginx — BlueBook 完整环境

## 快速开始

双击运行 `setup.bat`，自动下载 Nginx 并完成初始化。

## 启动 / 停止

```bash
nginx.exe                  # 启动
nginx.exe -s reload        # 重载配置（修改配置后）
nginx.exe -s stop          # 停止
```

## 配置说明

| 文件 | 用途 |
|------|------|
| `conf/nginx.conf` | 主配置（已集成 BlueBook 前端、API、图片映射） |
| `conf/mime.types` | MIME 类型映射 |
| `html/` | 默认站点目录（可忽略，由 vue-dist 接管根路径） |

## 监听端口

- `80` — 前端静态 + API 代理 + 图片访问

## 项目对接

| Nginx 路径 | 后端配置项 | 值 |
|-----------|-----------|-----|
| `/www/wwwroot/bluebook/vue-dist` | — | 前端打包后上传到此 |
| `http://127.0.0.1:9090` | `server.port` | 9090 |
| `/www/wwwroot/bluebook/files/` | `file.upload.path` | 上传文件目录 |
