# 1.1.4 退出修复记录

方向：从左向右划。设置页向右退出；记分页和操作页的系统返回先退到左侧相邻页。确认弹窗打开时，系统返回先关闭弹窗。

## 代码中确认的问题

- 原 `onswipe` 绑定在 swiper 的父容器；Vela 的 swipe 事件不冒泡，子组件手势不能依靠此入口可靠转发。
- 缺少 `onBackPress`。Vela 将系统右滑返回和返回实体键交给该生命周期回调。
- 原 touchend 仅取结束事件坐标，没有使用最后一次 touchmove 坐标作后备。
- `exitRequested` 原来在调用 API 前锁定，调用抛异常后不会复位，导致无法重试。

这些是静态代码与官方事件约定确认的缺口；目前没有真机事件日志，不能断言每次真机失败都由其中某一项引起。

## 修改

设置页直接监听 swipe 及触摸事件；右向位移至少 48 个坐标单位，且大于纵向位移的 1.3 倍时退出。系统返回通过 onBackPress 处理。退出按钮调用同一出口，手指拖动后产生的点击不触发按钮。退出前取消记分延时及重置倒计时；退出异常或重新显示时允许重试。

## 2026-11-06 验证

- `node tests/navigation.cjs` 通过：按钮、系统返回、事件去重、结束坐标缺失、左划切页、竖向拖动、关闭弹窗、API 抛异常后重试。
- `npm run build` 成功，产物 `dist/com.yukuai.sync.debug.1.1.4.rpk`。
- Vela xiaomi_band_pro 模拟器，336×480：设置页三个按钮完整可见；点击退出按钮后，运行日志记录页面 onDestroyed、应用 onDestroy 和 app destroyed。
- 重启模拟器后独立测试设置页从左向右拖动，运行日志同样记录页面与应用销毁。重启是因为独立 vapp 在销毁后重新运行出现卡住；未以白屏单独作为成功证据。
- 真机未连接，尚未安装本版本，也未确认真机右滑表现。

官方依据：

- https://iot.mi.com/vela/quickapp/zh/components/general/events.html
- https://iot.mi.com/vela/quickapp/zh/guide/framework/script/lifecycle.html
