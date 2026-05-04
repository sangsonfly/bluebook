# Nginx 配置

## 使用方式

1. 将 `nginx.conf` 复制到服务器 Nginx 配置目录：

   ```bash
   cp nginx.conf /etc/nginx/conf.d/bluebook.conf
   ```

2. 修改 `server_name` 为你的域名或 IP

3. 测试并重载：

   ```bash
   nginx -t && systemctl reload nginx
   ```

## 路径对应

| 配置项 | 路径 | 说明 |
|--------|------|------|
| 前端文件 | `/www/wwwroot/bluebook/vue-dist` | `vue/dist/` 打包后上传到此目录 |
| 后端接口 | `http://127.0.0.1:9090` | Spring Boot 默认端口 |
| 上传图片 | `/www/wwwroot/bluebook/files/` | 对应 `file.upload.path` 配置 |
