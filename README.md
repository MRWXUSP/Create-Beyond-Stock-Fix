# Create-Beyond Stock Fix（1.20.1 Forge）

修复机械动力（Create）与超越维度（Beyond Dimensions）的一个兼容性问题：

> 当同一物流网络中的多个**仓储链接站**（stock_link）经打包机分别指向**同一维度网络的不同维度网络通道**（net_pathway）方块时，**仓储发报机**（stock_ticker）会把整个维度网络的物品重复计数（每个链接计一次）。

## 适用环境

| 项 | 版本 |
|---|---|
| Minecraft / 加载器 | 1.20.1 / Forge 47.1.x |
| Create（机械动力） | 6.0.x |
| Beyond Dimensions（超越维度） | 0.7.x |

## 原理

Create 物流网络对"多个链接指向同一存储"的去重依赖 `InventoryIdentifier`（按库存标识去重，见 `LogisticsManager.createSummaryOfNetwork` 的 `processedInventories`）。第三方方块可通过官方扩展点 `InventoryIdentifier.REGISTRY` 注册解析器。超越维度的 `net_pathway` 未注册任何解析器，因此标识为 null，去重失效——每个通道方块都被当作独立库存。

本模组为 `net_pathway` 注册一个 Finder，把同一维度网络的所有通道方块解析为**按网络 id 归一化**的同一标识（`NetInventoryIdentifier`），使 Create 的去重机制按网络生效。另含一个针对 `PackagerBlockEntity.isTargetingSameInventory` 的小型 mixin，让工厂面板（FactoryPanel）补货器模式排除自家打包机指向的通道库存时也能正确按网络判断。

## 构建

1. 运行 `./gradlew build`（Create 与 BeyondDimensions 依赖均经 Modrinth Maven 自动下载，无需手动准备 jar）。
2. 产物：`build/libs/create-beyond-stock-fix-1.20.1-forge-1.0.0.jar`。

开发运行（`./gradlew runClient`）会自动带上 Create、BeyondDimensions 与 Mekanism（BD 硬依赖）等全部依赖。

### 本机构建环境备注（WSL / 仅装 JRE 时）

- 构建需要**完整 JDK 17**（含 javac；NeoForm 重编译 Minecraft 源码时依赖它）。若系统默认只有 JRE 或更高版本 JDK，
  下载 Adoptium JDK 17 后设置 `JAVA_HOME` 再构建，例如：

  ```bash
  JAVA_HOME=/home/zmh/.local/jdks/jdk-17.0.20+8 ./gradlew build
  ```

- 若 Gradle 发行版下载失败（SSL 握手被重置），可从腾讯镜像手动放入 wrapper 缓存：

  ```bash
  curl -sL -o /tmp/gradle-8.14.3-bin.zip "https://mirrors.cloud.tencent.com/gradle/gradle-8.14.3-bin.zip"
  # 解压到 ~/.gradle/wrapper/dists/gradle-8.14.3-bin/<hash目录>/ 并创建 gradle-8.14.3-bin.zip.ok
  ```

## 游戏内验证场景

- 两个链接站 + 打包机分别指向同一网络的 A、B 两个通道方块（同一频率）→ 发报机显示网络实际物品量（修复前显示两倍）。
- 两个链接站指向**不同**网络 → 发报机显示两网之和（各自计一次，互不吞并）。
- 单链接、指向同一通道方块的双链接、纯原版箱子等原有场景 → 计数不变（无回归）。

## 目录结构

```
src/main/java/com/cbdfix/
├── CbdFix.java                       # @Mod 主类，FMLCommonSetupEvent 中注册 Finder
├── inventory/NetInventoryIdentifier.java  # 按网络 id 归一化的库存标识
├── inventory/NetPathwayFinder.java        # net_pathway 的 InventoryIdentifier 解析器
├── mixin/PackagerBlockEntityMixin.java    # FactoryPanel 自排除场景的按网络判断
└── util/BdNetHelper.java                  # handler → 网络 id 的反射工具（失败自动降级）
```

## 备注

- 本模组硬依赖 Create 与 BeyondDimensions，二者缺失时 Forge 会阻止加载。
- 核心修复（Finder 注册）不涉及 mixin；mixin 仅用于工厂面板补货器的边缘场景，注入失败会显式报错。
- 若 Create 6.1+ 或 BeyondDimensions 0.8+ 变更相关 API，需同步更新（版本范围已在 mods.toml 中约束）。
