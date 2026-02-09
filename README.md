# Auto Call - 自动拨打电话应用

一个原生Android应用，使用Kotlin开发，实现自动拨打电话的功能。

## 功能特性

### 核心功能
- ✅ **定时任务**: 每隔30秒自动调用API接口获取电话号码
- ✅ **后台服务**: 使用前台服务(ForegroundService)保证应用在后台运行
- ✅ **API调用**: 通过Retrofit + OkHttp调用HTTP接口获取电话号码
- ✅ **自动拨打**: 获取到电话号码后自动拨打
- ✅ **自动挂断**: 拨打电话后20秒自动挂断
- ✅ **屏幕解锁**: 当手机息屏时自动唤醒屏幕并解锁
- ✅ **配置管理**: 可通过界面配置API接口地址

## 技术栈

- **开发语言**: Kotlin
- **最低SDK版本**: Android 8.0 (API 26)
- **目标SDK版本**: Android 14 (API 34)
- **架构组件**:
  - ForegroundService - 前台服务
  - Coroutines - 协程处理异步任务
  - Retrofit + OkHttp - 网络请求
  - ViewBinding - 视图绑定
  - SharedPreferences - 配置存储

## 项目结构

```
app/
├── src/main/
│   ├── java/com/callme/autocall/
│   │   ├── MainActivity.kt              # 主界面
│   │   ├── api/
│   │   │   ├── ApiService.kt           # API接口定义
│   │   │   └── PhoneNumberResponse.kt  # 数据模型
│   │   ├── manager/
│   │   │   ├── CallManager.kt          # 通话管理
│   │   │   └── ScreenManager.kt        # 屏幕管理
│   │   ├── service/
│   │   │   └── AutoCallService.kt      # 前台服务
│   │   └── utils/
│   │       ├── PermissionHelper.kt     # 权限工具
│   │       └── PreferenceHelper.kt     # 配置存储
│   ├── res/                            # 资源文件
│   └── AndroidManifest.xml             # 清单文件
├── build.gradle.kts                    # 模块级构建配置
└── proguard-rules.pro                  # 混淆规则
```

## 安装和配置

### 1. 环境要求
- Android Studio Arctic Fox 或更高版本
- JDK 8 或更高版本
- Android SDK 34
- 测试设备: Android 8.0+ (API 26+)

### 2. 构建应用
```bash
# 克隆仓库
git clone https://github.com/callme1994/auto-call.git
cd auto-call

# 使用Android Studio打开项目
# 或使用命令行构建
./gradlew assembleDebug

# 安装到设备
./gradlew installDebug
```

### 3. 权限配置

应用需要以下权限才能正常工作:

#### 基本权限 (自动申请)
- `CALL_PHONE` - 拨打电话
- `READ_PHONE_STATE` - 读取电话状态
- `ANSWER_PHONE_CALLS` - 接听和挂断电话 (Android 9+)
- `WAKE_LOCK` - 保持CPU唤醒
- `FOREGROUND_SERVICE` - 前台服务
- `INTERNET` - 网络访问
- `POST_NOTIFICATIONS` - 通知权限 (Android 13+)

#### 特殊权限 (需要手动授予)
1. **悬浮窗权限** (`SYSTEM_ALERT_WINDOW`)
   - 用于在其他应用上层显示内容
   - 在应用中点击"检查权限"按钮会跳转到设置页面

2. **电池优化**
   - 需要将应用添加到电池优化白名单
   - 防止系统杀死后台服务
   - 在应用中点击"检查权限"按钮会引导设置

### 4. API配置

#### API接口格式

应用支持以下两种JSON响应格式:

**格式1:**
```json
{
  "phoneNumber": "13800138000",
  "timestamp": 1234567890
}
```

**格式2:**
```json
{
  "phone": "13800138000"
}
```

#### 配置步骤
1. 启动应用
2. 在"API Configuration"区域输入API接口地址
3. 点击"Save API URL"保存配置
4. 点击"Start Service"启动服务

## 使用说明

### 1. 首次使用
1. 安装应用后首次打开
2. 点击"检查权限"按钮
3. 按照提示授予所有必需权限
4. 配置API接口地址
5. 点击"启动服务"

### 2. 服务运行
- 服务启动后会在通知栏显示持久通知
- 每30秒自动调用一次API接口
- 如果API返回电话号码，会自动拨打
- 每次通话会在20秒后自动挂断
- 息屏状态下会自动唤醒屏幕

### 3. 停止服务
- 点击"停止服务"按钮即可停止自动拨打

## 不同厂商手机的特殊设置

### 小米 (MIUI)
1. 设置 → 应用设置 → 应用管理 → Auto Call
2. 启用"自启动"
3. 启用"后台弹出界面"
4. 电池与性能 → 选择"无限制"

### 华为 (EMUI/HarmonyOS)
1. 设置 → 应用 → 应用启动管理
2. 找到Auto Call，设置为"手动管理"
3. 启用"自动启动"、"后台活动"、"锁屏显示"

### OPPO (ColorOS)
1. 设置 → 电池 → 耗电保护
2. 找到Auto Call，选择"允许后台运行"
3. 设置 → 应用管理 → Auto Call → 权限
4. 启用"自启动"、"后台运行"

### vivo (Funtouch OS)
1. i管家 → 应用管理 → 权限管理
2. 自启动 → 开启Auto Call
3. i管家 → 应用管理 → Auto Call
4. 设置为"允许后台高耗电"

### 一加 (OxygenOS)
1. 设置 → 电池 → 电池优化
2. 找到Auto Call，选择"不优化"

## 常见问题

### Q1: 服务为什么会被系统杀死？
**A:** 需要完成以下设置:
- 关闭电池优化
- 启用自启动权限
- 允许后台运行
- 在厂商的系统管理中添加白名单

### Q2: 息屏后无法拨打电话？
**A:** 确保授予了以下权限:
- 悬浮窗权限 (SYSTEM_ALERT_WINDOW)
- WAKE_LOCK 权限
- DISABLE_KEYGUARD 权限

### Q3: 无法自动挂断电话？
**A:** 
- Android 9+: 确保授予了 ANSWER_PHONE_CALLS 权限
- 某些设备可能需要ROOT权限或系统签名才能挂断电话
- 部分设备可能需要开启辅助功能权限

### Q4: API调用失败？
**A:** 检查以下内容:
- API URL格式是否正确 (必须是完整的HTTP/HTTPS地址)
- 网络连接是否正常
- API接口是否返回正确的JSON格式
- 检查logcat日志获取详细错误信息

### Q5: 如何查看日志？
**A:** 使用adb命令查看日志:
```bash
adb logcat | grep -E "AutoCallService|CallManager|ScreenManager"
```

## 安全提醒

⚠️ **重要提示**:
- 此应用具有自动拨打电话的功能，请谨慎使用
- 确保API接口的安全性，防止恶意调用
- 建议仅在测试环境或受控环境中使用
- 请遵守当地法律法规关于自动拨号的规定
- 频繁拨打电话可能会产生通话费用

## 技术限制

1. **挂断功能限制**:
   - Android 9+可以通过TelecomManager挂断电话
   - 更早版本需要使用反射，可能在某些设备上不工作
   - 部分设备可能需要系统权限或ROOT

2. **屏幕解锁限制**:
   - Android 10+对后台应用的限制更严格
   - 某些设备的安全锁屏无法通过应用解锁
   - 建议设置为滑动解锁或无密码

3. **后台运行限制**:
   - 不同厂商的ROM对后台服务限制不同
   - 需要手动配置各厂商的系统管理设置
   - 建议在设置中禁用电池优化

## 依赖库

```kotlin
// Core AndroidX
implementation("androidx.core:core-ktx:1.12.0")
implementation("androidx.appcompat:appcompat:1.6.1")
implementation("com.google.android.material:material:1.11.0")

// Retrofit for API calls
implementation("com.squareup.retrofit2:retrofit:2.9.0")
implementation("com.squareup.retrofit2:converter-gson:2.9.0")

// OkHttp
implementation("com.squareup.okhttp3:okhttp:4.12.0")
implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

// Coroutines
implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")

// Lifecycle
implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
implementation("androidx.lifecycle:lifecycle-service:2.7.0")
```

## 开发计划

- [ ] 添加通话记录显示
- [ ] 支持自定义拨打间隔时间
- [ ] 支持自定义挂断时间
- [ ] 添加通话统计功能
- [ ] 支持多个API接口配置
- [ ] 添加日志导出功能

## 许可证

本项目仅供学习和研究使用。使用本应用造成的任何后果由使用者自行承担。

## 联系方式

如有问题或建议，请提交Issue或Pull Request。

---

**注意**: 本应用为学习项目，请勿用于商业用途或非法用途。使用前请确保符合当地法律法规。
