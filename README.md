# Create-Beyond Stock Fix（1.21.1 NeoForge）

修复机械动力（Create）与超越维度（Beyond Dimensions）的兼容问题：当同一物流网络中的多个**仓储链接站**（stock_link）经打包机分别指向**同一维度网络的不同维度网络通道**（net_pathway）方块时，**仓储发报机**（stock_ticker）会把整个维度网络的物品重复计数。本模组为 `net_pathway` 注册按网络归一化的库存标识，使 Create 的按库存去重机制按网络生效。

- 支持版本：1.21.1 NeoForge（本分支）/ 1.20.1 Forge（`1.20.1` 分支）
- 依赖：Create 6.0.x、Beyond Dimensions 0.7.x（硬依赖，缺失时阻止加载）
- 许可证：MIT

## 构建

需要 JDK 21（含 javac），依赖经 Modrinth Maven 自动下载：

```bash
./gradlew build
# 产物：build/libs/create-beyond-stock-fix-1.21.1-neoforge-1.0.0.jar
```

开发运行（自动带上 Create、BeyondDimensions、Mekanism 等依赖）：

```bash
./gradlew runClient
```
