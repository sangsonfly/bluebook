# Image Storage Operations

This guide is used to keep uploaded images persistent and recoverable.

## 1) Permissions

Ensure the Spring Boot runtime user can read and write:

```bash
mkdir -p /www/wwwroot/bluebook/files
chown -R www:www /www/wwwroot/bluebook/files
chmod -R 755 /www/wwwroot/bluebook/files
```

## 2) Nginx Validation

```bash
nginx -t
systemctl reload nginx
```

Verify image URL:

```bash
curl -I https://your-domain.com/uploads/your-image-name.jpg
```

## 3) Cleanup Task Audit

Check and remove accidental cleanup tasks targeting upload directory:

```bash
crontab -l
```

Also check panel scheduler and custom scripts for `/www/wwwroot/bluebook/files`.

## 4) Backup Strategy

Recommended:
- daily incremental backup
- weekly full backup
- keep at least 14 days of retention

Example cron (daily 02:30):

```bash
30 2 * * * tar -czf /www/backup/bluebook-files-$(date +\%F).tar.gz /www/wwwroot/bluebook/files
```

## 5) Restart Verification

After restarting Spring Boot or server:
- open one old image URL under `/uploads/`
- upload one new image and verify it appears
- confirm both old and new images can be accessed
