# 图标导出

用户提供的 source.png 为共同来源。build_icons.py 使用 Pillow 裁去主体外的零散像素，保留绿色底板、羽毛球和记分板图案；四角保留透明。

## 尺寸依据

- Vela 安装包 manifest 图标：官方建议 192×192。https://iot.mi.com/vela/quickapp/zh/guide/framework/manifest.html
- 小米手环 8 Pro 屏幕：336×480。https://iot.mi.com/vela/quickapp/zh/guide/design/multi-screens.html
- 未找到该型号系统桌面图标槽位的独立官方像素规范。为修复用户反馈的图标过大，当前 192×192 画布内将图案设为 96×96，四周透明边距 48px；此比例为项目适配选择，待真机视觉反馈。
- Android 传统资源：mdpi 48、hdpi 72、xhdpi 96、xxhdpi 144、xxxhdpi 192 像素。
- Android 自适应图标：108dp 图层，图案底板 72dp 居中，羽毛球与计分图形保持在内部，绿色背景由系统裁切；五档密度分别输出。https://developer.android.com/develop/ui/compose/system/icon_design_adaptive
- store-512.png 是备用商店素材，不用于手环 manifest。当前方圆形视觉仍需按具体商店要求调整，不能视为上架审核通过。

运行 `python assets/icons/build_icons.py` 重建全部资源。

## 部署 2026-11-06

手机 com.yukuai.score 1.0.1，签名与已安装 1.0.0 相同，ADB install -r 返回 Success。

手环 com.yukuai.sync 1.1.5（含 1.1.4 退出修复），小米健康返回 handleInstallResultPacket code:0，insertWatchAppWithCallback success:true。手环视觉尺寸、透明边缘与真机退出行为仍需用户确认。
