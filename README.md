# Kingdee EAS Linux 客户端打包脚本

本仓库只包含打包脚本和字体修复研究源码，不包含金蝶客户端、安装包或用户数据。请自行取得有使用权限的 EAS 客户端，并设置本机源目录：

```bash
SOURCE_ROOT=/path/to/kingdee ./packaging/build-deb.sh
```

构建结果写入 `dist/`，请勿提交客户端程序或业务数据。
