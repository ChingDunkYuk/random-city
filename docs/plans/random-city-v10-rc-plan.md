# v0.9.5 打 tag + v1.0 RC:R8/签名/数据完整性/Release 验证

## Summary

v0.9.5 验收通过,先提交打 tag;然后按主计划书 Roadmap(L173)完成 **v1.0 RC**:
R8 keep 规则验证、Release 签名、数据完整性测试补全(内置图)、全量单测、Release 构建出包。

## Current State Analysis

- 待提交:6 个修改文件(v0.9.5 动画/震动/DAO/Repository/版本号),工作区其余未跟踪项维持不入库惯例。
- [build.gradle.kts](../../app/build.gradle.kts):release 已开 `isMinifyEnabled=true` + `isShrinkResources=true`,挂 `proguard-rules.pro`;当前 `versionCode=8 / 0.9.5`;**无 signingConfig**(release 未签名,只能 debug 安装)。
- [proguard-rules.pro](../../app/proguard-rules.pro):已覆盖 kotlinx.serialization(`data.model.**`)+ Room,Coil/OkHttp 靠 consumer rules。
- 单测已有 4 个:RandomEngineTest / CityFilterTest / RoutePlannerTest / CityDataValidationTest(数量≥100、id 唯一、必填、枚举、月份、预算/交通范围、大洲覆盖)。
- **缺口**:v0.9.0 引入的 103 张内置图(`assets/city_images/{id}.jpg`)没有完整性校验——以后加城市忘加图不会被发现。
- 构建环境:JDK 21 + keytool 可用;无 CI,签名 keystore 仅本机使用。

## Proposed Changes

### 1. 提交 v0.9.5 + tag

```text
git add 6 个修改文件 → commit "feat: v0.9.5 洗牌动画/Reveal 动画/震动反馈" → git tag v0.9.5
```

### 2. 版本号 — [build.gradle.kts](../../app/build.gradle.kts)

`versionCode = 9`,`versionName = "1.0.0"`。

### 3. 内置图完整性单测(新增)

`app/src/test/java/com/randomcity/app/CityImagesValidationTest.kt`:

- 每个 `cities.json` 城市存在对应 `src/main/assets/city_images/{id}.jpg` 且 >10KB(防空/占位图);
- 反向校验:`city_images/` 下无游离文件(存在但无对应城市 id,提示清理);
- 图片数量 == 城市数量。

### 4. 全量单测

`.\gradlew.bat testDebugUnitTest` 全绿(5 个测试类)。

### 5. Release 签名

1. `keytool -genkeypair` 生成 `keystore.jks`(RSA 2048、有效期 10000 天,放项目根目录);
2. 随机密码写入 `keystore.properties`(根目录,格式 storeFile/storePassword/keyAlias/keyPassword);
3. `.gitignore` 追加 `/keystore.jks`、`/keystore.properties`(签名材料不入库);
4. [build.gradle.kts](../../app/build.gradle.kts):
   ```kotlin
   val ksProps = Properties().apply {
       rootProject.file("keystore.properties").takeIf { it.exists() }
           ?.inputStream()?.use { load(it) }
   }
   signingConfigs {
       create("release") {
           if (ksProps.isNotEmpty) {
               storeFile = rootProject.file(ksProps.getProperty("storeFile"))
               storePassword = ksProps.getProperty("storePassword")
               keyAlias = ksProps.getProperty("keyAlias")
               keyPassword = ksProps.getProperty("keyPassword")
           }
       }
   }
   release {
       if (ksProps.isNotEmpty) signingConfig = signingConfigs.getByName("release")
       // 无 keystore 时回落 debug 签名,保证他机可构建
       ...
   }
   ```

### 6. Release 构建验证

`.\gradlew.bat assembleRelease`:
- R8 全量混淆 + 资源压缩通过;
- 若有 `Missing class`/dontwarn 类错误(kotlinx.serialization/OkHttp/Coil 常见),在 [proguard-rules.pro](../../app/proguard-rules.pro) 补最小 `-dontwarn`,不放宽 keep;
- 产物 `app/build/outputs/apk/release/app-release.apk`(已签名),报告体积(debug 包对比)。

### 7. 真机冒烟(用户执行)

安装 **release** 包走查:冷启动 → 随机(洗牌动画)→ 详情(图/天气/Reveal)→ 收藏 → Saved → 筛选;确认无 R8 导致的崩溃/数据异常(serialization 反序列化、Room 查询为高风险点)。

## Assumptions & Decisions

1. 签名材料**只存本机**(根目录 keystore.jks + keystore.properties),`.gitignore` 排除;密码随机生成,用户需自行备份该文件(丢失无法更新签名)。
2. 性能/离线无代码改动:架构已满足(城市 Room、图片 assets、103 城内存过滤、远程仅天气/兜底图),以真机走查验证,不引入 Baseline Profile 等额外工程。
3. 版本名直接 `1.0.0`(RC 即 v1.0 候选,冒烟通过后同版本号正式发布)。
4. 计划书§51–58 中的性能/冷启动指标以手动走查确认,不新增自动化基准测试(超出个人项目必要度)。

## Verification

1. `git tag` 含 `v0.9.5`;`git status` 无 app 源码遗留。
2. `testDebugUnitTest`:5 个测试类全绿(含新 CityImagesValidationTest)。
3. `assembleRelease`:R8 通过,`app-release.apk` 已签名且体积显著小于 debug(预期 ~15-25MB,图片占大头)。
4. `git status`:keystore.jks / keystore.properties 被 ignore(不出现在 untracked)。
5. 真机 release 冒烟(用户):核心流程无崩溃,收藏重启仍在,断网可用。
