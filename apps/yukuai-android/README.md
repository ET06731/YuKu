# 羽快记分板 Android 同步开发版

当前版本 1.2.0，Android 与手环统一包名 `com.yukuai.sync`，调试 APK 使用手环项目的 `sign/debug/match.p12`，证书一致。该证书为本地开发证书，正式发布需配置发布签名。旧独立计分包 `com.yukuai.score` 保留。

构建：JDK 17 + Android SDK 36，执行 `gradlew.bat --offline :app:testDebugUnitTest :app:assembleDebug`。
APK：`app/build/outputs/apk/debug/app-debug.apk`。

两端打开应用，小米运动健康保持已连接手环。手机连接后确认使用手环比分；手机原比分保存到 `local_backup`。默认手环控制，手机比赛页可申请接管，在手环确认。手机控制时所有操作由手环校验、保存并确认，手机收到确认后更新。手环设置页点击“手机计分”可确认切回手环，以便手机断电后恢复独立计分。

支持比分、规则、名称、上下显示顺序、撤销、重置、历史同步。手机离线不提交新计分；已提交但未确认指令保留并按同一编号重试。手环控制时离线继续计分，重新连接以手环快照恢复。应用进程被系统终止后需重新打开；后台实时同步、手机离线计分没有真机验收。

协议与验证记录见 `../xiaomi-band-development/examples/yukuai-sync-dev/docs/sync-v2.md`。官方接口依据：https://iot.mi.com/vela/quickapp/zh/features/network/interconnect.html 。
