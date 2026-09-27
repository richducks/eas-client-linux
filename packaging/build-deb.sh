#!/usr/bin/env bash
set -euo pipefail

BASE_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
SOURCE_ROOT="${SOURCE_ROOT:-}"
[[ -n "$SOURCE_ROOT" ]] || { echo "请设置 SOURCE_ROOT 为已授权安装的 EAS 客户端目录" >&2; exit 1; }
VERSION="${VERSION:-8.8.0+fontfix2}"
ARCH="amd64"
BUILD_ROOT="$BASE_DIR/build/kingdee-eas-client_${VERSION}_${ARCH}"
APP_ROOT="$BUILD_ROOT/opt/kingdee-eas-client"
PACKAGE_OUT="$BASE_DIR/dist/client/kingdee-eas-client_${VERSION}_${ARCH}.deb"

[[ -d "$SOURCE_ROOT/eas/client" ]] || { echo "错误：缺少 $SOURCE_ROOT/eas/client" >&2; exit 1; }
[[ -x "$SOURCE_ROOT/eas/clientjdk/bin/java" ]] || { echo "错误：缺少可执行的内置 Java" >&2; exit 1; }
[[ -f "$SOURCE_ROOT/eas/client/bin/client.sh" ]] || { echo "错误：缺少 client.sh" >&2; exit 1; }
[[ "$(uname -m)" == "x86_64" ]] || { echo "错误：客户端和内置 JDK 仅支持 x86_64。" >&2; exit 1; }

rm -rf "$BUILD_ROOT"
mkdir -p \
  "$BUILD_ROOT/DEBIAN" \
  "$APP_ROOT/seed/eas" \
  "$BUILD_ROOT/usr/bin" \
  "$BUILD_ROOT/usr/share/applications" \
  "$BUILD_ROOT/usr/share/icons/hicolor/32x32/apps" \
  "$BUILD_ROOT/usr/share/doc/kingdee-eas-client" \
  "$(dirname "$PACKAGE_OUT")"

rsync -a \
  --exclude='logs/***' \
  --exclude='temporary/***' \
  --exclude='cache/F7Cache/***' \
  --exclude='cache/mdprefetch/***' \
  --exclude='cache/quicksearch/***' \
  --exclude='cache/datatask/***' \
  --exclude='cache\userconfig.xml' \
  --exclude='*.log' \
  --exclude='*.log.*' \
  --exclude='*.vmlog' \
  "$SOURCE_ROOT/eas/client" "$APP_ROOT/seed/eas/"
rsync -a "$SOURCE_ROOT/eas/clientjdk" "$APP_ROOT/seed/eas/"

# 旧的备用启动脚本带有开发者 Mac 目录；正式包中移除这一无效路径。
sed -i '\|^cd "/Users/aladdin/eas75client/eas/client/bin/"$|d' \
  "$APP_ROOT/seed/eas/client/bin/clientStartup.sh"

install -m 0755 "$BASE_DIR/packaging/set-client-env.sh" \
  "$APP_ROOT/seed/eas/client/bin/set-client-env.sh"
install -m 0755 "$BASE_DIR/packaging/kingdee-eas-launcher" \
  "$BUILD_ROOT/usr/bin/kingdee-eas-client"
install -m 0644 "$BASE_DIR/packaging/kingdee-eas-client.desktop" \
  "$BUILD_ROOT/usr/share/applications/kingdee-eas-client.desktop"
install -m 0644 "$BASE_DIR/packaging/kingdee-eas-settings.desktop" \
  "$BUILD_ROOT/usr/share/applications/kingdee-eas-settings.desktop"
install -m 0644 "$BASE_DIR/packaging/kingdee-eas-client.png" \
  "$BUILD_ROOT/usr/share/icons/hicolor/32x32/apps/kingdee-eas-client.png"
install -m 0644 "$BASE_DIR/packaging/README.txt" \
  "$BUILD_ROOT/usr/share/doc/kingdee-eas-client/README.txt"

find "$BUILD_ROOT" -type d -exec chmod go-w {} +
find "$BUILD_ROOT" -type f -exec chmod go-w {} +
INSTALLED_SIZE="$(du -sk "$BUILD_ROOT" | awk '{print $1}')"
sed \
  -e "s/@VERSION@/$VERSION/g" \
  -e "s/@INSTALLED_SIZE@/$INSTALLED_SIZE/g" \
  "$BASE_DIR/packaging/control.in" > "$BUILD_ROOT/DEBIAN/control"
chmod 0644 "$BUILD_ROOT/DEBIAN/control"

dpkg-deb --root-owner-group -Zzstd -z3 --build "$BUILD_ROOT" "$PACKAGE_OUT"
dpkg-deb --info "$PACKAGE_OUT"
(cd "$(dirname "$PACKAGE_OUT")" && sha256sum "$(basename "$PACKAGE_OUT")") > "$PACKAGE_OUT.sha256"

echo "构建完成：$PACKAGE_OUT"
echo "校验文件：$PACKAGE_OUT.sha256"
