# 自定义替身制作指南

本指南面向**整合包作者与玩家**：不改一行 Java 代码，仅靠两个文件（一份 JSON + 若干 JS 脚本），
就能把自制的替身注册进本模组，供玩家通过觉醒之箭、替身disc等方式获得。

文中所有 API、字段名、默认值和行为，均标注了实际源码位置 `文件:行号`，可逐条对照。
凡是源码中读不到、无法确认的行为，一律标注"未验证"，不作猜测。

---

## 1. 概述

### 1.1 自定义替身系统能做什么

一份自定义替身由两类资源组成：

| 资源 | 作用                                                                                               | 必需性 |
| --- |--------------------------------------------------------------------------------------------------| --- |
| 替身定义 JSON | 声明替身注册名、显示名 lang key、可用状态列表、属性数值、Disc id                                                         | 必需（至少要有一份 JSON，否则替身根本不存在） |
| 状态脚本 JS | 声明每个状态（default / heal / idle / punch …）的模型、标签、阶段门槛，以及 `update` / `timeOut` / `capability` 三个行为函数 | 每个状态一个 JS；一个状态都没有的替身没有任何效果 |

JSON 被解析成 `StandCustomInfo`（`src\main\java\org\huajiager\stand\custom\StandCustomInfo.java:20`），
JS 被解析成 `StandStateInfo` + 保留原始脚本对象（`src\main\java\org\huajiager\stand\custom\StandStateInfo.java:11`），
然后由 `StandCustom` 把两者组装成一个可注册的替身（`src\main\java\org\huajiager\stand\custom\StandCustom.java:23`）。

### 1.2 资源放在哪里

源码中的路径常量（`src\main\java\org\huajiager\stand\StandResourceLoader.java:48-53`）：

```java
private static final Path CONFIG_FOLDER      = Paths.get("config", HuajiAgeRemastered.MOD_ID, "custom_stand");
private static final Path CONFIG_STATE_FOLDER = Paths.get("config", HuajiAgeRemastered.MOD_ID, "custom_stand/states");
private static final String RES_FOLDER        = "/assets/" + HuajiAgeRemastered.MOD_ID + "/custom_stand";
private static final String RES_STATE_FOLDER  = "/assets/" + HuajiAgeRemastered.MOD_ID + "/custom_stand/states";
private static final String ACCEPTED_STAND_SUFFIX = ".json";
private static final String ACCEPTED_STATE_SUFFIX = ".js";
```

`HuajiAgeRemastered.MOD_ID` 的值是 `"huajiager"`（`src\main\java\org\huajiager\HuajiAgeRemastered.java:16`），因此实际路径为：

| 类别 | 实际路径 | 扫描方式 |
| --- | --- | --- |
| 内置替身 JSON | 模组 jar 内 `/assets/huajiager/custom_stand/*.json` | `getResourceAsStream`，名单在代码里硬编码（`StandResourceLoader.java:67-71`） |
| 内置状态 JS | 模组 jar 内 `/assets/huajiager/custom_stand/states/*.js` | 同上，名单硬编码（`StandResourceLoader.java:74-82`） |
| 用户替身 JSON | `.minecraft/config/huajiager/custom_stand/*.json` | `File.listFiles()` 列目录，逐个判断后缀（`StandResourceLoader.java:122-132`） |
| 用户状态 JS | `.minecraft/config/huajiager/custom_stand/states/*.js` | `File.listFiles()` 列目录（`StandResourceLoader.java:134-144`） |

三点必须注意（均来自源码行为）：

1. **用户目录不递归**。只扫描该层目录下的直接文件（`StandResourceLoader.java:123`、`136`），
   子目录里的文件不会被读取。
2. **后缀大小写敏感**。判定方式是 `file.getName().endsWith(suffix)`（`StandResourceLoader.java:128`、`140`），
   所以 `MyStand.JSON`、`state.JS` 不会被加载。
3. **配置状态目录会自动创建**：`Files.createDirectories(CONFIG_STATE_FOLDER)`（`StandResourceLoader.java:86`），
   创建失败只记 WARN（`StandResourceLoader.java:88`）。JSON 目录与 state 目录同属一条路径链，所以一并被创建。

### 1.3 什么时候加载

- 模组初始化时：`HuajiAgeRemastered.onInitialize()` 执行 `new StandLoader()`（`src\main\java\org\huajiager\HuajiAgeRemastered.java:36`），
  `StandLoader` 构造函数末尾调用 `reloadStands()`（`src\main\java\org\huajiager\init\loaders\StandLoader.java:50`）。
- `reloadStands()` 内部先清 API 注册表、摘掉上一轮的 `StandCustom`，再调用
  `StandResourceLoader.loadCustomStand()` 重新加载全部 JSON 与 JS（`StandLoader.java:53-62`），
  最后遍历 `CUSTOM_STAND_SERVER` 逐个 `new StandCustom(info)` 并注册（`StandLoader.java:71-75`）。
- `loadCustomStand()` 每次都会先清空两张注册表，再走"内置 → 用户配置"的顺序
  （`StandResourceLoader.java:56-64`）。因此**用户配置的同名替身会覆盖内置替身**
  ——两张表都是 `HashMap.put`，后写入者生效（`StandResourceLoader.java:100` 内置、
  `150` 用户文件）。

### 1.4 如何重载

在游戏内（需在服务器/单人世界内执行）输入：

```
/reloadStand
```

- 命令字面量就是 `reloadStand`（大小写敏感），权限等级要求 `0`，即所有玩家都能执行，
  没有子命令与参数（`src\main\java\org\huajiager\command\CommandStandReload.java:30-32`）。
- 执行内容是 `StandLoader.reloadStands()`（`CommandStandReload.java:36`），
  成功后反馈一行 `HUAJI Age: stand data reloaded (<替身总数> stands).`（`CommandStandReload.java:37-39`）。
- **注意**：`StandResourceLoader.CONFIG_FOLDER` 是相对路径
  （`Paths.get("config", ...)`，`StandResourceLoader.java:48`），解析基准是**进程工作目录**，
  对专用服务器通常就是服务器根目录。未验证在非标准启动方式下的解析结果。

---

## 2. 替身定义 JSON

### 2.1 字段表

JSON 用一个原生 `Gson` 实例反序列化成 `StandCustomInfo`
（`StandResourceLoader.java:41` 的 `new Gson()`；反序列化在 `StandResourceLoader.java:156-161`）。

> **关键约定**：源码用的是**默认 Gson**（`StandResourceLoader.java:41` 的 `new Gson()`），没有 `GsonBuilder`、
> 没有 `FieldNamingPolicy`（全工程检索 `FieldNamingPolicy|GsonBuilder|setFieldNaming` 无任何命中），
> 但 `StandCustomInfo` 的 11 个字段**全部带 `@SerializedName`**（`StandCustomInfo.java:25-56`），
> 绑定的 JSON key 依次是 `stand` / `name` / `states` / `disc` / `stand_tags` / `stages` / `attributes`
> / `sounds` / `sounds_repeat` / `gravity` / `author`。
> 因此 **JSON 的 key 必须与注解绑定的名字逐字符一致**，多词字段要写 **snake_case**：
> `stand_tags`、`sounds_repeat` 这样写才能生效；写成 `standTags`、`soundsRepeat` 会被 Gson **静默忽略**
> （字段一旦带 `@SerializedName`，字段名本身不再参与匹配）。
> 详见第 6.1 节与第 7 节第 1 条。

字段一览（"默认值"一栏 = `decorate()` 里实际写入的值，或 Java 字段初始值）：

| 字段名 | 类型 | 必填 | 默认值 | 含义                                                           | 源码位置 |
| --- | --- | --- | --- |--------------------------------------------------------------| --- |
| `stand` | string | **是** | 无（为空直接抛错） | 替身注册名，约定带命名空间，如 `huajiager:crazy_diamond`                    | 字段 `StandCustomInfo.java:26`；校验 `StandCustomInfo.java:114-116` |
| `name` | string | 否 | `"stand." + stand.replace(':','.') + ".name"` | 显示名 **lang key**（客户端再翻译），赋给 `StandBase.localName`            | 字段 `StandCustomInfo.java:28`；默认值 `StandCustomInfo.java:117-119`；使用 `StandCustom.java:31` |
| `states` | string[] | 否 | `["default"]`，并强制包含 `"default"` | 本替身可用状态 id 列表；每个 id 对应 `custom_stand/states/<stand>_<id>.js` | 字段 `StandCustomInfo.java:32`；默认与补全 `StandCustomInfo.java:120-125`；使用 `StandCustom.java:43` |
| `disc` | string | 否 | `"huajiager:disc/disc_huajiager_" + <stand 的 path 部分>` | 替身 HUD 信息块左上角**光碟图标**的贴图 id（只取它的 path 部分，命名空间被丢弃）                                             | 字段 `StandCustomInfo.java:35`；默认值 `StandCustomInfo.java:126-130`；消费点 `StandUtil.java:348-359`（`getDiscTex`）→ `EventStandHudRender.java:85-87` |
| `stand_tags` | string[] | 否 | `[]`（空列表） | 替身级标签；目前源码只用 `arrow` 一个值（**精确匹配小写**），决定能否进觉醒之箭抽取池                        | 字段 `StandCustomInfo.java:38`；默认值 `StandCustomInfo.java:131-133`；唯一消费点 `StandUtil.java:281-292` |
| `stages` | int | 否 | `1`（`<= 0` 一律改成 `1`） | 阶段总数                                                         | 字段 `StandCustomInfo.java:41`；默认值 `StandCustomInfo.java:134-136`；**消费点未找到**（真正的阶段门槛来自 JS 的 `stage`，见第 7 节第 5 条） |
| `attributes` | number[] | 否 | `[1.2, 10, 200, 2, 60000, 75, 100000]` | 属性数组，**至少 7 个元素才会被采用**（少于 7 个时未提供的项保持 0，并在加载时打一条 WARN；多于 7 个时多余项被忽略），下标含义见 2.2                           | 字段 `StandCustomInfo.java:44`；默认值与警告 `StandCustomInfo.java:137-144`；使用 `StandCustom.java:32-40` |
| `sounds` | string[] | 否 | `[]` | 召唤音池：未内置召唤音的自定义替身从中随机播一条（受配置「是否需要替身的音效」控制）                                                          | 字段 `StandCustomInfo.java:47`；默认值 `StandCustomInfo.java:145-147`；消费点 `StandUtil.java:304-317` → `MessageStandUp.java:157-170`、`EntityStandBase.java:225-230`、`420-440` |
| `sounds_repeat` | string[] | 否 | `[]` | 循环音效列表，条目格式 `音效id-音量`（如 `entity.player.attack.strong-0.7`），随替身实体循环播放（受配置「是否需要替身的移动音效」控制）                                                       | 字段 `StandCustomInfo.java:50`；默认值 `StandCustomInfo.java:148-150`；消费点 `StandUtil.java:320-345` → `EntityStandBase.java:434-439` |
| `gravity` | boolean | 否 | `false`（Java 字段初始值，`decorate()` 不赋值） | 是否受重力                                                        | 字段 `StandCustomInfo.java:53`；getter `StandCustomInfo.java:104-106`；**仍未消费**（`isGravity()` 无调用点） |
| `author` | string | 否 | `""`（Gson 反序列化时为 `null`，`decorate()` 兜成空串） | 作者；非空时在替身 Disc / 塔罗牌 tooltip 追加一行灰字                                                           | 字段 `StandCustomInfo.java:56`；默认值 `StandCustomInfo.java:151-153`；消费点 `ItemDiscStand.java:135-141`、`ItemTarot.java:150-156` |

JSON 里出现的、但不在上表内的 key 会被 Gson 静默丢弃（不报错、不警告）。

### 2.2 `attributes` 数组下标含义

只有 `info.getAttributes().size() >= 7` 时才会读取，否则 7 个属性**全部保持 0**
（`StandCustom.java:32-40`）。下标依次是：

| 下标 | 目标字段（`StandBase`） | 类型转换 | 含义 | 源码位置 |
| --- | --- | --- | --- | --- |
| 0 | `speed` | `float` | 速度 | `StandCustom.java:33` |
| 1 | `damage` | `float` | 伤害 | `StandCustom.java:34` |
| 2 | `duration` | `intValue()`，**小数被截断** | 持续时间（tick） | `StandCustom.java:35` |
| 3 | `distance` | `float` | 距离 | `StandCustom.java:36` |
| 4 | `cost` | `intValue()`，**小数被截断** | 技能精神力消耗 | `StandCustom.java:37` |
| 5 | `charge` | `intValue()`，**小数被截断** | 充能（每 tick 回充量一类） | `StandCustom.java:38` |
| 6 | `maxMP` | `intValue()`，**小数被截断** | 精神力上限 | `StandCustom.java:39` |

`getAttributes()` 会把 JSON 里的每个数字统一转成 `float` 返回（`StandCustomInfo.java:72-81`）。

> `cost` 与技能门槛的关系：客户端按 `data.getStage() <= 0` 拦截技能键，阶段 0 时不发技能包
> （`src\client\java\org\huajiager\client\event\EventStandKey.java:105-107`）。
> `charge` / `maxMP` 与召唤、技能扣费的联动未逐一验证。

### 2.3 状态如何与 JS 文件对上号

`StandCustom` 按 `info.getStand() + "_" + <state>` 作为 key 去 `CUSTOM_STATE_SERVER` 查
（`StandCustom.java:42-49`），而脚本注册时用的 key 是 `info.getStand() + "_" + info.getStateId()`
（`StandResourceLoader.java:115`、`168`）。

**结论：JSON 的 `states` 里的每一个 id，必须与对应 JS 里 `stand` + `stateId` 的组合完全相同（含命名空间）。**
例如 JSON 写 `"stand": "huajiager:crazy_diamond"`、`states` 含 `"heal"`，
则脚本必须写 `stand:"huajiager:crazy_diamond"` + `stateId:"heal"`。

对不上的状态会被**静默跳过**：查不到就 `infoState == null`，不进 `addState`
（`StandCustom.java:45-48`）；即使 key 撞上了，还有一层
`key.equals(info.getStand() + "_" + infoState.getStateId())` 的二次校验（`StandCustom.java:46`）。

### 2.4 模型与贴图

- 状态实际使用的模型 id 来自 JS 的 `modelId`（见第 3 节），经 `StandStateCustom.getModelID()` 返回
  （`src\main\java\org\huajiager\stand\custom\StandStateCustom.java:50-56`）。
- 贴图路径由 `StandStateCustom.getTex()` 推导：取 `modelId` 的 path 部分，若以 `_default` 结尾就
  去掉该后缀，再拼成 `textures/entity/<path>.png`（`StandStateCustom.java:64-78`）。
  例：`modelId = "huajiager:crazy_diamond_default"` → `huajiager:textures/entity/crazy_diamond.png`。
  若 `modelId` 不是合法 Identifier，回落父类贴图（`StandStateCustom.java:69-72`）。
- **客户端渲染的实际取贴图路径不同**：`RenderStandBase.getTexture` 对疯狂钻石 / 隐者之紫 / 白蛇三个
  自定义替身做了**按替身名硬编码**的贴图分支（`src\client\java\org\huajiager\client\render\entity\RenderStandBase.java:708-725`），
  自定义替身 `StandCustom` 从不设置 `texPath`（`StandCustom` 构造器未赋值，`StandBase.getTexPath()` 因此为 `null`），
  所以**新做的自定义替身会一路兜底到 `FALLBACK_TEXTURE`（世界贴图）**
  （`RenderStandBase.java:726-729`，注释见 `708-710`）。想让新替身有独立贴图，只能改客户端代码，
  纯配置路径下未验证可行方案。
- 模型造型同样是硬编码注册表：`RenderStandBase` 只注册了原生 5 替身 + 3 个自定义替身的模型 key
  （`RenderStandBase.java:128-164`），`pickModel` 查不到就回落 `defaultModel`
  （`RenderStandBase.java:217-222`）。**新自定义替身会用默认人形模型。**

---

## 3. 状态脚本 JS

### 3.1 脚本必须定义什么

状态脚本的 `eval` 结果**必须是一个对象**（`ScriptObjectMirror` / `Map`），其字段即状态信息；
并且该对象上按需定义 `update` / `timeOut` / `capability` 三个方法。

加载链路（`StandResourceLoader.java:175-184`）：

```java
String script = new String(input.readAllBytes(), StandardCharsets.UTF_8);
Object scriptObject = JsEngineHelper.ENGINE.eval(script);
return transObjectToEntry(scriptObject);
```

- 引擎是全局共享的 `JsEngineHelper.ENGINE`（`src\main\java\org\huajiager\util\JsEngineHelper.java:33`），
  按 `graal.js → js → javascript → nashorn` 依次尝试（`JsEngineHelper.java:49-64`）；
  一个都找不到会直接 `IllegalStateException`（`JsEngineHelper.java:65-66`）。
- 引擎启动时注入了 `Java.asJSONCompatible` 的幂等 polyfill（`JsEngineHelper.java:36-39`、`73`），
  所以脚本里可以像内置样例那样用它包一层对象。
- `eval` 结果**不是 Map** 时会报错并返回 `null`：
  `[HuajiAge] Custom state script eval result is not a Map: <类名或 null>`（`StandResourceLoader.java:192-197`）。

**脚本对象字段表**（读取逻辑全部在 `transObjectToEntry`，`StandResourceLoader.java:192-266`；
字段名枚举在 `StandResourceLoader.java:268-287`）：

| JS 字段名 | 类型 | 必填 | 默认值 | 含义 | 源码位置 |
| --- | --- | --- | --- | --- | --- |
| `stand` | string | **必填** | 无（缺失即判无效） | 替身注册名；与 JSON 的 `stand` 一起决定注册 key。**缺失或空白时该脚本被判为无效、直接跳过，并打一条 ERROR 日志**（不再注册成 `null_<stateId>` 这类脏 key） | `StandResourceLoader.java:203-209` |
| `stateId` | string | 否 | `"default"` | 状态 id；与 JSON `states` 里的 id 对应 | `StandResourceLoader.java:204-205` |
| `stateKey` | string | 否 | `null` | 状态显示名 lang key，HUD 与切换提示用它 | `StandResourceLoader.java:207-208`；使用 `MessageStandModeSwitch.java:143-148`、`EventStandHudRender.java:96-103` |
| `stage` | number / 数字字符串 | 否 | `0` | 该状态所需的替身阶段门槛；阶段不足时模式切换会跳过该状态 | `StandResourceLoader.java:210-220`；门槛判定 `MessageStandModeSwitch.java:167-171` |
| `modelId` | string | 否 | `null` | 模型 id；`null` 时回落父类生成值 `huajiager:<stand>_<stateId>` | `StandResourceLoader.java:222-229`；回落逻辑 `StandStateCustom.java:51-56` |
| `stateTags` | string[] | 否 | `[]` | 状态标签，供内置能力判断 | `StandResourceLoader.java:231-237`；标签常量 `EnumStandTag.java:8-26` |
| `soundRepeat` | boolean / 布尔字符串 | 否 | `false` | 是否有循环音效（写入 `StandStateBase.soundLoop`） | `StandResourceLoader.java:239-245`；赋值 `StandStateCustom.java:43` |
| `hand` | boolean / 布尔字符串 | 否 | `true` | 第一人称是否显示手臂（写入 `StandStateBase.isHandPlay`） | `StandResourceLoader.java:247-253`；赋值 `StandStateCustom.java:41` |
| `update` | function | 建议有 | 无（方法缺失即跳过） | 替身放出期间每 tick 调用 | 调用点 `StandStateCustom.java:81-83` |
| `timeOut` | function | 建议有 | 无（缺失即跳过） | 替身出场时间耗尽时调用 | 调用点 `StandStateCustom.java:85-88` |
| `capability` | function | 可选 | 无（缺失即跳过） | 技能键触发时调用；返回 `false` 表示"未真正发动" | 调用点 `StandStateCustom.java:90-96` |

`modelId` 的一个细节：如果 `stateId` **不包含** `%custom` 子串，源码会给 `modelId` 追加 `"_" + stateId`
（`StandResourceLoader.java:222-229`）。所以 `stateId:"default"` + `modelId:"huajiager:crazy_diamond"`
最终模型 id 是 `huajiager:crazy_diamond_default`。

`decorate()` 不接受 `null` 的 `stateKey`/`modelId`：它们不参与 `decorate()`
（`StandCustomInfo.decorate()` 只处理 JSON 字段，`StandCustomInfo.java:99-137`），
`null` 会原样保留到 `StandStateInfo`。

### 3.2 脚本被如何调用

`invokeScript` 是唯一的调用入口（`StandStateCustom.java:98-129`），核心是
`Invocable.invokeMethod(scriptObject, methodName, worldWrapper, entityWrapper, dataWrapper)`
（`StandStateCustom.java:118`）。也就是：

| 调用时机 | Java 方法 | 传入的 JS 方法名 | 源码位置 |
| --- | --- | --- | --- |
| 替身在场、每 tick | `StandStateBase.doTask` 覆写 | `update` | `StandStateCustom.java:81-83` |
| 出场时间耗尽 | `StandStateBase.doTaskOutOfTime` 覆写 | `timeOut` | `StandStateCustom.java:85-88` |
| 技能键（Hold / 技能包） | `doTaskCapability`（经 `StandCustom.doStandCapabilityResult`） | `capability` | `StandStateCustom.java:90-96`；`StandCustom.java:81-92` |

三个实参**按位置**传入，**没有任何全局变量绑定**。所以脚本必须这样接收：

```javascript
update: function (worldWrapper, entityWrapper, dataWrapper) { /* ... */ }
```

内置样例的参数名就是这三个（`src\main\resources\assets\huajiager\custom_stand\states\crazy_diamond_default.js:25`），
注释里写的 `@param world` / `@param entity` 与实参名不一致，属于注释滞后。

`capability` 的返回值语义：只有返回 `Boolean` 才被采用，其他返回值按 `true` 处理
（`StandStateCustom.java:91-95`）。返回 `false` 会让服务端退还本次精神力
（设计意图见 `StandCustom.java:76-80`）。

异常与日志行为：

| 情况 | 行为 | 源码位置 |
| --- | --- | --- |
| 脚本未定义该方法 | `NoSuchMethodException`，**静默忽略**（无日志） | `StandStateCustom.java:119-120` |
| 脚本方法内部抛错 | `ERROR`：`[HuajiAge] Stand state script error in [<stateId>] method [<method>]: <消息>` | `StandStateCustom.java:121-123` |
| 其他异常 | `ERROR`：`[HuajiAge] Unexpected error invoking method [<method>] on state [<stateId>]` | `StandStateCustom.java:124-126` |
| `stateInfo == null`（脚本对象丢失） | 直接返回 `null`，不调用、不报错 | `StandStateCustom.java:99-102` |
| 引擎不是 `Invocable` | `WARN`：`Stand state script engine is not invocable, method <stateId> skipped.` | `StandStateCustom.java:108-112` |

### 3.3 `StandDadaWrapper` 可用方法参考表

`StandDadaWrapper`（`src\main\java\org\huajiager\stand\custom\script\StandDadaWrapper.java:18`）
是脚本读写替身数据的门面，构造时用 `StandUtil.getStandData(user)` 与
`StandUtil.getStandHandler(user)` 取数据（`StandDadaWrapper.java:28-31`），
并对"未觉醒替身"的实体做了 `null` 兜底。全部公开方法如下：

| 方法 | 返回 | 说明 | 源码位置 |
| --- | --- | --- | --- |
| `getData()` | `IExposedData` | 裸露的替身数据对象（可能为 `null`） | `StandDadaWrapper.java:33-35` |
| `getStandName()` | `String` | 当前替身注册名；无数据时返回 `ExposedData.EMPTY_STAND` | `StandDadaWrapper.java:37-42` |
| `getStage()` | `int` | 当前替身阶段；无数据时 `0` | `StandDadaWrapper.java:44-49` |
| `getModel()` | `String` | 当前模型 id；无数据时 `ExposedData.EMPTY_STAND` | `StandDadaWrapper.java:51-56` |
| `getState()` | `String` | 当前状态 id；无数据时 `ExposedData.States.DEFAULT.getName()` | `StandDadaWrapper.java:58-63` |
| `isTriggered()` | `boolean` | 替身是否已召唤 | `StandDadaWrapper.java:65-67` |
| `getMP()` | `int` | 当前精神力；无 handler 时 `0` | `StandDadaWrapper.java:69-71` |
| `getMaxMP()` | `int` | 精神力上限；无 handler 时 `0` | `StandDadaWrapper.java:73-75` |
| `getBuffer()` | `int` | 缓冲值（时停剩余 tick / 联动标记等）；无 handler 时 `0` | `StandDadaWrapper.java:77-79` |
| `getBufferTag()` | `String` | 缓冲标签；无 handler 或标签为 `null` 时返回 `"empty"` | `StandDadaWrapper.java:81-83` |
| `setMP(int)` | `void` | 写精神力 | `StandDadaWrapper.java:85-89` |
| `setMaxMP(int)` | `void` | 写精神力上限 | `StandDadaWrapper.java:91-95` |
| `setBuffer(int)` | `void` | 写缓冲值 | `StandDadaWrapper.java:97-101` |
| `setBufferTag(String)` | `void` | 写缓冲标签 | `StandDadaWrapper.java:103-107` |

> **没有** `setStage` / `setStandName` / `setState` 这类写入方法（本表即全部公开方法）。
> 脚本要改阶段/状态，需要自己 `Java.type(...)` 调 `StandUtil` 或从 `getData()` 拿 `IExposedData`
> 直接调用（`IExposedData` 提供 `setStand`/`setStage`/`setTrigger`/`setHandDisplay`/`setState`/`setModel`，
> 见 `src\main\java\org\huajiager\capability\IExposedData.java:9-35`）——这一用法**内置样例中没有出现，未验证**。

内置样例的真实用法：读 `dataWrapper.getStage()`（`hermit_purple_default.js:28`、
`white_snake_punch.js:28`）、读 `dataWrapper.getMP()` 一类未出现、
写 `dataWrapper.setBuffer(200)` / `dataWrapper.setBufferTag("huajiager.buff.overdrive")`
（`hermit_purple_overdrive.js:39-40`、`67-68`）。

### 3.4 另外两个实参与可用全局对象

**`EntityLivingBaseWrapper`**（`src\main\java\org\huajiager\stand\custom\script\EntityLivingBaseWrapper.java:13`）公开方法：

| 方法 | 返回 | 说明 | 源码位置 |
| --- | --- | --- | --- |
| `getLivingBase()` | `LivingEntity` | 底层实体，内置脚本几乎都通过它把实体交给 `StandPowerHelper` | `EntityLivingBaseWrapper.java:21-23` |
| `ticksExisted()` | `int` | 实体 age | `EntityLivingBaseWrapper.java:25-27` |
| `getYaw()` | `float` | 偏航角 | `EntityLivingBaseWrapper.java:29-31` |
| `getPitch()` | `float` | 俯仰角 | `EntityLivingBaseWrapper.java:33-35` |
| `getPos()` | `Vec3dWrapper` | 坐标 | `EntityLivingBaseWrapper.java:37-39` |
| `getEyePos()` | `Vec3dWrapper` | 眼睛坐标 | `EntityLivingBaseWrapper.java:41-43` |
| `getLookVec()` | `Vec3dWrapper` | 视线向量 | `EntityLivingBaseWrapper.java:45-47` |
| `getSpeed()` | `float` | 速度；有替身实体时取替身速度并减 `0.784`，否则取自身速度模长 | `EntityLivingBaseWrapper.java:49-58` |
| `getStandEntity()` | `EntityStandBase` | 使用者已召唤的替身实体（可能 `null`） | `EntityLivingBaseWrapper.java:60-62` |

`Vec3dWrapper` 的公开方法未在本次阅读范围内逐一确认（**未验证**），
内置样例也没有直接使用它。

**`WorldWrapper`**（`src\main\java\org\huajiager\stand\custom\script\WorldWrapper.java:9`）只有一个方法：

| 方法 | 返回 | 源码位置 |
| --- | --- | --- |
| `getWorld()` | `World` | `WorldWrapper.java:17-19` |

**全局对象**：引擎启动脚本只注入了 `Java.asJSONCompatible` 兜底
（`JsEngineHelper.java:36-39`），`Java.type` 由引擎的 Nashorn 兼容模式提供
（`JsEngineHelper.java:19-25` 注释、`:46` 设置 `polyglot.js.nashorn-compat`）。
内置样例只用到了这两个全局（`crazy_diamond_default.js:1-3` 等），
其余全局（如 `console`、计时器）未验证。

**可调用的 Java 静态方法**：内置样本统一 `Java.type("org.huajiager.stand.helper.StandPowerHelper")`
（`crazy_diamond_default.js:1`）。该类共 30 个 `public static` 方法
（`src\main\java\org\huajiager\stand\helper\StandPowerHelper.java`），
内置脚本用到的方法与签名如下（均可在脚本里直接调用）：

| 方法签名 | 作用 | 源码位置 |
| --- | --- | --- |
| `MPCharge(LivingEntity, int)` | 回充精神力 | `StandPowerHelper.java:83` |
| `potionEffectAdd(LivingEntity, String, int, int)` | 按注册名施加药水效果（等级按 1 基减一） | `StandPowerHelper.java:314`、`331-337` |
| `potionEffectAdd(LivingEntity, StatusEffectInstance...)` | 变长参数版 | `StandPowerHelper.java:307` |
| `rangePunchAttack(LivingEntity, float degree, float damage, float distance)` | 视野角内范围连打，命中返回 `true` | `StandPowerHelper.java:109` |
| `playStandSoundWithCooldown(LivingEntity, String soundId, float volume)` | 带时长冷却的替身音效，返回是否真正播放 | `StandPowerHelper.java:230` |
| `playSound(Entity, String soundId, float volume, float pitch)` | 按注册名播放音效（服务端自动走广播） | `StandPowerHelper.java:447` |
| `getPlayerHoldItem(LivingEntity, boolean mainHand)` | 取主/副手物品 | `StandPowerHelper.java:425` |
| `repairItem(ItemStack)` | 全量修复耐久 | `StandPowerHelper.java:438` |
| `consumeItem(ItemStack, int)` | 消耗物品 | `StandPowerHelper.java:571` |
| `getItemRegistryName(ItemStack)` | 取物品注册名 | `StandPowerHelper.java:470` |
| `sendMessage(LivingEntity, String langKey)` | 发聊天栏消息（传 lang key） | `StandPowerHelper.java:481` |
| `getRangeLiving(LivingEntity, float distance, float degree)` | 取范围内活体（原生数组，脚本按 `length` / `[i]` 遍历） | `StandPowerHelper.java:497` |
| `healEntity(LivingEntity, int)` | 治疗 | `StandPowerHelper.java:515` |
| `isNeedHeal(LivingEntity)` | 是否未满血 | `StandPowerHelper.java:525` |
| `createParticleEffect(Entity, int type)` | 粒子/世界事件，`type=1` 有实现 | `StandPowerHelper.java:282-291` |
| `potionDefaultOutOfTime(LivingEntity)` | 超时默认效果 | `StandPowerHelper.java:358` |
| `increaseStandTime(LivingEntity, int ticks)` | 延长替身在场时间 | `StandPowerHelper.java:393` |
| `removeBadPotion(LivingEntity)` | 清除负面药水 | `StandPowerHelper.java:375` |
| `addItemToplayer(LivingEntity, String itemId, int amount)` | 给玩家发物品 | `StandPowerHelper.java:532` |
| `giveDisc(LivingEntity, String type)` | 发放命令 disc | `StandPowerHelper.java:552` |
| `telepathizeItem(LivingEntity, ItemStack)` | 念写定位结构 | `StandPowerHelper.java:586` |

其余未在内置样例中出现的公开方法（`potionEffect`、`isStandSoundReady`、`playEvent`、
`getPotion`、`newPotion`、`getUserStand`、`increasePotionTime`、`playEvent(World, BlockPos, int, int)`）
按同样方式调用即可，行为以源码为准。

---

## 4. 完整示例

以下两份文件是**可运行的最小骨架**，字段名与内置样例保持一致（只是把 id 换成了 `mypack:my_stand`），
直接改 id / lang key / 数值就能用。

### 4.1 替身定义 JSON

放置于 `.minecraft/config/huajiager/custom_stand/my_stand.json`：

```json
{
  "stand": "mypack:my_stand",
  "name": "stand.mypack.my_stand.name",
  "disc": "huajiager:disc/disc_huajiager_my_stand",
  "stand_tags": ["arrow"],
  "stages": 1,
  "states": ["default", "idle"],
  "attributes": [1, 12, 250, 2, 170000, 80, 180000],
  "sounds": ["huajiager:stand_crazy_diamond_1"],
  "sounds_repeat": ["entity.player.attack.strong-0.7"],
  "gravity": false,
  "author": "你的名字"
}
```

逐行说明：

| 行 | 字段 | 说明 |
| --- | --- | --- |
| 2 | `stand` | 替身注册名，必须与 JS 里的 `stand` 完全一致；建议带你自己的命名空间 |
| 3 | `name` | 显示名 lang key，需要你在自己的资源包/整合包语言文件里补 `stand.mypack.my_stand.name`（未验证整合包语言文件能否覆盖，默认回落显示原 key） |
| 4 | `disc` | 替身 HUD 信息块左上角**光碟图标**的贴图 id（`StandUtil.java:348-359`，绘制见 `EventStandHudRender.java:85-87`）。**图标贴图要作者自备**：放在 `assets/huajiager/textures/item/<disc 的 path>.png`（命名空间恒为 `huajiager`，字段里写的命名空间不参与拼接）；它**不影响**创造模式 Disc 物品的生成（那走 `stand`，见第 7 节第 4 条） |
| 5 | `stand_tags` | 注意是 snake_case。写 `arrow`（**精确匹配小写**）才能进觉醒之箭抽取池。写成 `standTags` 会被 Gson 忽略 |
| 6 | `stages` | 阶段总数，`<= 0` 会被强制改成 `1` |
| 7 | `states` | 状态 id 列表，**必须包含 `default`**（源码会自动补，`StandCustomInfo.java:120-125`）；这里声明几个，就要有对应的几个 JS |
| 8 | `attributes` | 7 个数字，依次是 速度 / 伤害 / 持续时间 / 距离 / 消耗 / 充能 / 精神力上限；少于 7 个则全部为 0，并在加载时打一条 WARN（`StandCustomInfo.java:139-144`） |
| 9 | `sounds` | 召唤音池：未内置召唤音的自定义替身从中随机播一条，受配置「是否需要替身的音效」（`allowStandSound`）控制（`MessageStandUp.java:157-170`、`EntityStandBase.java:420-432`） |
| 10 | `sounds_repeat` | 也是 snake_case。循环音效，条目格式 `音效id-音量`；随替身实体循环播放，受配置「是否需要替身的移动音效」（`allowStandMovingSound`）控制（`StandUtil.java:320-345`、`EntityStandBase.java:434-439`） |
| 11 | `gravity` | **仍未消费**（`isGravity()` 无调用点，`StandCustomInfo.java:104-106`） |
| 12 | `author` | 非空时在替身 Disc / 塔罗牌 tooltip 追加一行灰字（`ItemDiscStand.java:135-141`、`ItemTarot.java:150-156`） |

### 4.2 状态脚本 JS（default 态）

放置于 `.minecraft/config/huajiager/custom_stand/states/my_stand_default.js`。
文件名本身不参与解析（注册 key 由脚本内容决定，`StandResourceLoader.java:115`），
但为了可读性建议遵循 `<stand>_<state>.js` 命名：

```javascript
var Helper = Java.type("org.huajiager.stand.helper.StandPowerHelper");

Java.asJSONCompatible({
//  替身注册名，必须与 JSON 的 stand 字段完全一致
    stand: "mypack:my_stand",
//  状态 id，必须出现在替身 JSON 的 states 数组里
    stateId: "default",
//  状态显示名 lang key（HUD / 切换提示用）
    stateKey: "stand.mypack.my_stand.default",
//  模型 id。stateId 不含 "%custom" 时，源码会自动追加 "_default"
    modelId: "mypack:my_stand",
//  状态标签，可按需增删；可用值见 3.1 节与 EnumStandTag.java:8-26
    stateTags: [],
//  第一人称是否显示手臂，不写默认 true
    hand: true,
//  是否循环音效，不写默认 false
    soundRepeat: false,
//  解锁该状态所需替身阶段，不写默认 0
    stage: 0,

//  替身放出期间每 tick 调用一次
    update: function (worldWrapper, entityWrapper, dataWrapper) {
        // 范围连打：返回 true 表示本 tick 命中过至少一个目标
        var hit = Helper.rangePunchAttack(entityWrapper.getLivingBase(), 90, 5, 2);
        if (hit) {
            Helper.playStandSoundWithCooldown(entityWrapper.getLivingBase(),
                "huajiager:stand_crazy_diamond_1", 0.6);
        }
    },

//  出场时间耗尽时调用一次
    timeOut: function (worldWrapper, entityWrapper, dataWrapper) {
        Helper.potionDefaultOutOfTime(entityWrapper.getLivingBase());
    },

//  技能键触发时调用；返回 false 表示"未真正发动"
    capability: function (worldWrapper, entityWrapper, dataWrapper) {
        Helper.potionEffectAdd(entityWrapper.getLivingBase(), "huajiager:potion_huaji_repair", 200, 0);
        return true;
    }
});
```

第二个状态（`idle`）只需另存一份
`.minecraft/config/huajiager/custom_stand/states/my_stand_idle.js`，
把 `stateId` 改成 `"idle"`、`stateKey` 改成 `"stand.mypack.my_stand.idle"`，
并按需要给不同的 `stage`（阶段不足时切换模式会跳过它，`MessageStandModeSwitch.java:167-171`）。

> `capability` 里若使用了 `dataWrapper.getStage()`、`dataWrapper.setBuffer(...)` 等，
> 记得三个形参名必须写全（`worldWrapper, entityWrapper, dataWrapper`），否则会变成 `undefined`。

---

## 5. `standTags` 与觉醒之箭

觉醒之箭（`huajiager:arrow_stand`）在玩家尚无替身时右键会授予替身，授予口径在
`src\main\java\org\huajiager\item\ItemArrowStand.java:70-100`：

1. 若整合包配置项 `ConfigHuaji.Stands.arrowStand` 填了有效替身注册名 → **直接授予**，不判失败概率
   （`ItemArrowStand.java:74`、`109-121`）。
2. 否则从 `StandUtil.getArrowStands()` 抽取池里按索引取一个，并按
   `ConfigHuaji.Stands.chanceStandFail` 判定失败（`ItemArrowStand.java:75-77`）。

抽取池的构成（`src\main\java\org\huajiager\stand\StandUtil.java:281-292`）：

```java
public static List<StandBase> getArrowStands() {
    List<StandBase> pool = new ArrayList<>(ARROW_STANDS_NATIVE);
    for (StandBase stand : StandLoader.STAND_LIST) {
        if (stand instanceof StandCustom custom && custom.getInfo() != null) {
            List<String> tags = custom.getInfo().getStandTags();
            if (tags != null && tags.contains(ARROW_STAND_TAG)) {
                pool.add(stand);
            }
        }
    }
    return pool;
}
```

- 固定白名单是 The World / Star Platinum / Hierophant Green / Killer Queen
  （`StandUtil.java:276-278`）。
- 标签常量：`private static final String ARROW_STAND_TAG = "arrow";`（`StandUtil.java:273`）。
  即 **`stand_tags` 里包含字符串 `"arrow"`（精确匹配、区分大小写）的自定义替身会自动进入抽取池**。
- 抽取索引是 `world.random.nextInt(100)` 再对池大小取模
  （`ItemArrowStand.java:76`；取模在 `StandUtil.java:294-301`）。

**所以，把自定义替身加入觉醒之箭抽取池的唯一配置动作是：**

```json
"stand_tags": ["arrow"]
```

以下三个坑会让这一步"看起来做了却无效"：

1. 写成 `"standTags"`（camelCase）→ Gson 静默忽略（key 由 `@SerializedName("stand_tags")` 绑定，
   `StandCustomInfo.java:37-38`），`standTags` 在 `decorate()` 里被兜成空列表
   （`StandCustomInfo.java:131-133`），`tags.contains("arrow")` 永远为 `false`。
2. 在 `config/huajiager/custom_stand/states/` 里放 JSON → 不会被扫描（只读 `.js`，
   `StandResourceLoader.java:134-144`）。
3. 加完标签没有 `/reloadStand` → 内存里的 `CUSTOM_STAND_SERVER` 还是旧数据
   （`StandLoader.STAND_LIST` 与 `HuajiAgeAPI` 只有重载时才重建，`StandLoader.java:53-76`）。

> 另外：内置样例的三个 JSON 全部写的是 `"stand_tags"`（`crazy_diamond.json:5`、
> `hermit_purple.json:5`、`white_snake.json:5`），与 `@SerializedName("stand_tags")`
> （`StandCustomInfo.java:37-38`）一致，所以这三个替身**能**被读进觉醒之箭抽取池；
> 反而写成 camelCase 的 `standTags` 会被忽略，详见第 7 节第 1 条。

---

## 6. 常见问题

### 6.1 字段缺失

| 情况 | 实际行为 | 源码位置 |
| --- | --- | --- |
| JSON 缺 `stand` 或为空串 | 抛 `JsonSyntaxException("Custom stand file needs a stand name")` | `StandCustomInfo.java:101-103` |
| JSON 缺 `name` | 自动生成 `stand.<把冒号换成点>.name` | `StandCustomInfo.java:104-106` |
| JSON 缺 `states` | 补成 `["default"]`；若已有列表但不含 `default`，会**追加** `default` 到列表尾部 | `StandCustomInfo.java:107-112` |
| JSON 缺 `disc` | 自动生成 `huajiager:disc/disc_huajiager_<stand 的 path>` | `StandCustomInfo.java:113-117` |
| JSON 缺 `standTags` | 补成**空列表**（不是 `null`） | `StandCustomInfo.java:118-120` |
| JSON 缺 `stages` 或 `<= 0` | 补成 `1` | `StandCustomInfo.java:121-123` |
| JSON 缺 `attributes` 或为空数组 | 补成 `[1.2, 10, 200, 2, 60000, 75, 100000]` | `StandCustomInfo.java:124-126` |
| JSON 缺 `sounds` / `soundsRepeat` | 补成空列表 | `StandCustomInfo.java:127-132` |
| JSON 缺 `author` | 补成 `""` | `StandCustomInfo.java:133-135` |
| JSON 缺 `gravity` | 保持 Java 默认 `false`（`decorate()` **不赋值**） | `StandCustomInfo.java:41`、`100-137` |
| JS 缺 `stand` | 该脚本被判无效、直接跳过，并打一条 ERROR 日志（不再注册成 `null_<stateId>` 这类脏 key） | `StandResourceLoader.java:203-209` |
| JS 缺 `stateId` | 该字段落 `"default"` | `StandResourceLoader.java:204-205` |
| JS 缺 `stateKey` | 保持 `null`；HUD 回落到 `"stand.state.huajiage." + stateId` | `StandResourceLoader.java:207-208`；回落 `EventStandHudRender.java:96-103` |
| JS 缺 `modelId` | 保持 `null`，模型 id 回落父类生成的 `huajiager:<stand>_<stateId>` | `StandResourceLoader.java:222-229`；回落 `StandStateCustom.java:51-56` |
| JS 缺 `stateTags` | 空列表 | `StandResourceLoader.java:231-237` |
| JS 缺 `soundRepeat` | `false` | `StandResourceLoader.java:239-245` |
| JS 缺 `hand` | `true` | `StandResourceLoader.java:247-253` |
| JS 缺 `update` / `timeOut` / `capability` | 该方法被跳过且**不记日志**——这些是可选方法，故意不打日志避免每 tick 刷屏；但方法名打错时同样静默，排错需自查 | `StandStateCustom.java:119-121` |

### 6.2 数组长度不足

- **`attributes` 少于 7 个元素**：整个 `if (size() >= 7)` 不成立，**7 个属性全部停留 0**
  （`StandCustom.java:32-40`）。同时 `decorate()` 只在 `attributes` 为 `null` 或空数组时才填默认值
  （`StandCustomInfo.java:124-126`），所以 `[1,2,3]` 这种"填了一半"的写法**不会**得到默认值补全，
  而是照旧全 0。注意 `attributes` 多于 7 个时多余的会被忽略（只读下标 0~6）。
- **`disc` / `sounds` / `sounds_repeat` / `stand_tags` 的长度**：源码中没有基于长度的校验，长度不影响加载
  （`disc` 是单个字符串，本身没有长度上限校验）。其中 `stand_tags` 走 `contains("arrow")` **精确匹配**，
  `sounds` / `sounds_repeat` 里的无效音效 id、解析失败的条目会被逐条跳过
  （`StandUtil.java:304-345`）；四个字段的真实消费点见 2.1 字段表。
- **JSON `states` 里有 id、但没有对应 JS**：该状态被静默跳过（`StandCustom.java:45-48`），
  不报错；结果是这个状态不在 `StandBase.states` 里，切换模式时取不到它。
- **`stateTags` 里的标签名写错**：只是不生效，没有校验也没有日志。
  源码中被真正读取的状态标签只有：`ride`（`StandUtil.java:165`、`MessageStandUp.java:102`）、
  `fly`（`RenderStandBase.java:673`、`EventPlayerFlying.java:47`）、
  `block_move`（`EventCrazyDiamond.java:74`）、`disc_deprive`（`EventWhiteSnake.java:126`）。
  `EnumStandTag.java:9-15` 里还定义了 `undead`（仅由 `StateOrgaRequiemDefault.java:26`、
  `StateOrgaRequiemFly.java:27` 在 Java 侧写入）以及 `element_light` / `sound-die:`，
  后两者**没有找到任何读取点**。

### 6.3 脚本异常

| 情况 | 实际行为 | 源码位置 |
| --- | --- | --- |
| 脚本抛 `ScriptException`（语法错、运行时错） | `ERROR`：`[HuajiAge] Failed to eval custom state script`（带堆栈），该状态不注册 | `StandResourceLoader.java:180-183` |
| 脚本读文件失败 | `ERROR`：`[HuajiAge] Failed to load custom state file: <绝对路径>` | `StandResourceLoader.java:170-172` |
| `eval` 结果不是对象/Map | `ERROR`：`[HuajiAge] Custom state script eval result is not a Map: <类名或 null>` | `StandResourceLoader.java:193-196` |
| 上面任一条导致 `transObjectToEntry` 返回 `null` | 用户配置路径下**直接跳过、不再补日志**（内置路径同样静默） | `StandResourceLoader.java:166-169`、`113-116` |
| 脚本每 tick 报错 | 每 tick 一条 `ERROR`（`[HuajiAge] Stand state script error in [<state>] method [update]`） | `StandStateCustom.java:121-123` |
| 脚本返回 `undefined` 给 `capability` | 按 `true` 处理（视为已发动，不退还精神力） | `StandStateCustom.java:91-95` |

### 6.4 JSON 异常

| 情况 | 实际行为 | 源码位置 |
| --- | --- | --- |
| JSON 语法错（内置文件） | 被捕获，`ERROR`：`[HuajiAge] Failed to load internal stand: <名字>` | `StandResourceLoader.java:99-103` |
| JSON 语法错（`config` 下的文件） | **`JsonSyntaxException` 不在 `loadStand(File)` 的 `catch (IOException)` 范围内**（`StandResourceLoader.java:146-154`），会向上抛到 `reloadStands()` → `/reloadStand` 命令执行处。未验证游戏对命令执行期异常的最终处理（命令失败 / 断开连接），但**该路径不会产生本模组的日志**。 |
| JSON 文件是空的 / 内容为 `null` | `GSON.fromJson` 返回 `null`，紧接着 `info.decorate()` 抛 `NullPointerException`（`StandResourceLoader.java:160`）。内置路径会捕获并记 `ERROR`（`StandResourceLoader.java:101-103`），**用户配置路径同样不在 `catch (IOException)` 范围内**，行为同上。 |
| JSON 文件读不出来（IO 错） | `ERROR`：`[HuajiAge] Failed to load custom stand file: <绝对路径>` | `StandResourceLoader.java:151-153` |
| JSON key 写成 camelCase（如 `standTags` / `soundsRepeat`） | Gson 静默忽略该 key，字段保持 `null`，随后被 `decorate()` 兜成默认值；**无任何警告**。正确写法是 snake_case（`stand_tags` / `sounds_repeat`），其余单词字段名（`stand` / `name` / `states` / `disc` / `stages` / `attributes` / `sounds` / `gravity` / `author`）不受影响 | 反序列化 `StandResourceLoader.java:41`、`158-163`；key 绑定 `StandCustomInfo.java:37-38`、`49-50`；兜底 `StandCustomInfo.java:131-133`、`148-150` |

### 6.5 加载顺序与覆盖

- 顺序固定为：清空注册表 → 建目录 → 内置 3 份 JSON → 内置 7 份 JS → 用户 JSON → 用户 JS
  （`StandResourceLoader.java:56-64`）。
- 同名替身：用户 JSON 覆盖内置（`HashMap.put`，后写生效，`StandResourceLoader.java:100` 与 `150`）。
- 同名状态：key 是 `stand + "_" + stateId`，用户 JS 覆盖内置（`StandResourceLoader.java:115` 与 `168`）。
- 每次 `/reloadStand` 都会重建 `StandCustom` 实例并重新 `addState`，同时
  `STAND_LIST.removeIf(StandCustom.class::isInstance)` 先摘掉上一轮的自定义替身，
  避免重复累积（`StandLoader.java:59`）。

---

## 7. 附：写文档过程中发现的源码疑点 / 潜在 bug

以下问题都是"读源码就能确认"的事实，附 `文件:行号`，供维护者参考；
本文档正文中的"消费点未找到"也就是指这一节。

1. **JSON 的 snake_case key 与 POJO 字段名不匹配（已在 1.0.2 修复：补回 `@SerializedName`）**
    - 字段：`private List<String> standTags;` 带 `@SerializedName("stand_tags")`
      （`StandCustomInfo.java:37-38`）、`private List<String> soundsRepeat;` 带
      `@SerializedName("sounds_repeat")`（`StandCustomInfo.java:49-50`）；
      `StandCustomInfo` 的 11 个字段现在全部有 `@SerializedName`（`StandCustomInfo.java:25-56`）。
    - 源码用的仍是裸 `new Gson()`（`StandResourceLoader.java:41`），无 `GsonBuilder`、
      无 `FieldNamingPolicy`（全工程检索 `FieldNamingPolicy|GsonBuilder|setFieldNaming` 零命中）；
      但因为注解已显式绑定 key，Gson 只按注解名匹配，**snake_case 现在能正常读入**
      （本版本 `mod_version=1.0.2`，`gradle.properties:16`）。
    - 3 份内置 JSON 写的正是 snake_case：`crazy_diamond.json:5`（`stand_tags`）、
      `crazy_diamond.json:13`（`sounds_repeat`）、`hermit_purple.json:5`、`white_snake.json:5`、
      `white_snake.json:12`，因此内置 3 个自定义替身的 `standTags` 会被读入，
      `StandUtil.getArrowStands()`（`StandUtil.java:281-292`）能把它们加进抽取池——与
      `ItemArrowStand` 的注释"从 StandUtil.getArrowStands() 抽取池"（`ItemArrowStand.java:31`）
      以及配置项 Tooltip 的说法（`assets\huajiager\lang\zh_cn.json:675`）一致。
    - 文档处理：字段表按**实际生效的 snake_case** 写（`stand_tags` / `sounds_repeat`），
      并显式提示 camelCase（`standTags` / `soundsRepeat`）会被忽略。

2. **`loadStand(File)` / `loadStates(File)` 的异常覆盖面（已在 1.0.2 修复）**
    - 原实现只 `catch (IOException)`，而 `loadStand(InputStream)` 会抛 `JsonSyntaxException`
      （`StandCustomInfo.java` 的 `decorate()` 入口一带）与 `NullPointerException`（`info.decorate()`），
      用户配置目录里放一份坏 JSON 会直接把异常抛到 `/reloadStand` 执行处。
    - 现在两处都是 `catch (IOException | RuntimeException e)` 并记 ERROR 日志
      （`StandResourceLoader.java:151`、`172`）：坏文件只跳过该文件，不中断整次重载。

3. **`transObjectToEntry` 返回 `null` 的分支（已在 1.0.2 修复）**
    - JS 缺 `stand` 字段原本会走到静默分支，注册成 `null_<stateId>` 且日志空白。
    - 现在缺 `stand`（或为空白）直接判该脚本无效、`return null`，并打一条 ERROR
      （`StandResourceLoader.java:203-209`）；`loadStates(File)` 侧对 `info == null` 也不再注册。

4. **`StandCustomInfo` 各字段的消费状态（1.0.2 现状）**
    - `disc`：被 `StandUtil.getDiscTex`（`StandUtil.java:348-359`）消费，用于替身 HUD 信息块
      左上角的光碟图标（绘制点 `EventStandHudRender.java:85-87`）。图标贴图要作者自备，路径是
      `assets/huajiager/textures/item/<disc 的 path>.png`——`Identifier.of` 的命名空间恒为 `huajiager`
      （`StandUtil.java:356`），字段里写的命名空间被丢弃，只取 path 部分。
    - `sounds`：被 `StandUtil.getCustomStandSounds`（`StandUtil.java:304-317`）消费，调用点为
      `MessageStandUp.java:157-170`（未内置召唤音的自定义替身，从池里随机播一条）与
      `EntityStandBase.java:420-440`（客户端播放，首次 tick 触发见 `EntityStandBase.java:225-230`）；
      受配置「是否需要替身的音效」（`ConfigHuaji.Stands.allowStandSound`，`zh_cn.json:668`）控制。
    - `soundsRepeat`：被 `StandUtil.getCustomStandRepeatSounds`（`StandUtil.java:320-345`）+
      `EntityStandBase.java:434-439` 消费，播成跟随实体的循环音，条目格式 `音效id-音量`
      （如 `entity.player.attack.strong-0.7`）；受配置「是否需要替身的移动音效」
      （`ConfigHuaji.Stands.allowStandMovingSound`，`zh_cn.json:667`）控制。
    - `author`：被 `ItemDiscStand.appendTooltip`（`ItemDiscStand.java:135-141`）与
      `ItemTarot.appendTooltip`（`ItemTarot.java:150-156`）消费，字段非空时追加一行灰字。
    - `standTags`：仍由 `StandUtil.getArrowStands` 消费（`StandUtil.java:281-292`），
      精确匹配小写 `arrow`。
    - `gravity`：**仍未消费**（`isGravity()` 无调用点，`StandCustomInfo.java:104-106`）。
    - `stages`：**仍未消费**，真正的阶段门槛来自 JS 的 `stage`（见本节第 5 条）。
    - `attributes`：少于 7 个时未提供的项**仍为 0**（行为没变），但现在会在加载时打一条 WARN
      （`StandCustomInfo.java:139-144`）。
    - `disc` 与创造模式 Disc 物品无关：`ItemLoader` 的 ItemGroup 变体是遍历 `StandLoader.STAND_LIST`，
      用 `ItemDiscStand.createDisc(...)` 把替身名写进 NBT 生成的（`ItemLoader.java:350-357`、
      `ItemDiscStand.java:169-175`），Disc 物品本身只有 `huajiager:disc_stand` 一个注册名
      （`ItemLoader.java:272`），全程不读 `disc` 字段。

5. **`stages` 字段与实际阶段门槛机制脱节**
    - JSON 的 `stages`（`StandCustomInfo.java:33`）无消费点（见第 7 节第 4 条）。
    - 真正的阶段门槛来自 JS 的 `stage`，经 `StandStateInfo.setStage`（`StandResourceLoader.java:259`）
      → `StandStateCustom` 构造器（`StandStateCustom.java:42`）→ `StandStateBase.getStage()`
      → 模式切换时的 `base.getStage() <= data.getStage()` 判定（`MessageStandModeSwitch.java:167-171`）。
    - 也就是说 `stages` 是个"写了没用"的字段。

6. **资源数量注释（已在 1.0.2 修正）**
    - `StandLoader` 类头与 `reloadStands()` 行内注释原先都写"4 个内置 JSON + 8 个 state JS"，
      现已统一为实际数量：3 个 JSON 与 7 个 state JS（见 `StandResourceLoader` 的
      `loadInternalStands()` / `loadInternalStates()` 两份硬编码名单）。

7. **`EventStandHudRender` 的注释与 `EventStandUpgrade` 的逻辑（已在 1.0.2 修正）**
    - 原注释称"当前实现无 stage 推进机制（stage 恒为 0）"，与事实相反：
      `EventStandUpgrade.upgradeTick` 在特异点标记剩 3 时执行 `StandUtil.setStandStage(player, 1)`
      （`EventStandUpgrade.java:58-59`），且 `EventStandKey.performSkill` 用 `data.getStage() <= 0` 拦截技能
      （`EventStandKey.java:105`）。注释现已改为与实现一致。

8. **`db`/命名一致性风险：状态 key 依赖 `stand` 字符串完全一致**
    - 匹配逻辑是字符串拼接 + 二次 `equals`（`StandCustom.java:44-46`），
      JSON 写 `huajiager:crazy_diamond` 而 JS 写 `crazy_diamond` 就会静默失配。
    - 没有大小写归一化、没有命名空间补全，也没有任何"状态清单对不上"的警告日志。

9. **`StandStateCustom.getModelID()` 的 `%custom` 分支行为未在样例中出现**
    - 源码支持 `stateId` 含 `%custom` 时不对 `modelId` 追加后缀
      （`StandResourceLoader.java:226-228`），但 7 份内置脚本没有一个使用该后缀，
      该分支的预期用法**未验证**。

10. **`EntityLivingBaseWrapper.getSpeed()` 里的魔数 `0.784`**
    - 有替身实体时返回 `替身速度模长 - 0.784`（`EntityLivingBaseWrapper.java:53-56`），
      无注释解释该常数的来源；负值未做钳制（**未验证**是否会在脚本里返回负数）。

11. **类头行数断言（已在 1.0.2 修正）**
    - `StandPowerHelper` 类头原先写"共 871 行"（与实际不符），该断言与一处损坏的"（ 收尾版）"
      片段已删除；本文档也不再引用具体行数，因为行数会随改动漂移。
