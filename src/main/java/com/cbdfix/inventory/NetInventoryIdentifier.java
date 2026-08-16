package com.cbdfix.inventory;

import com.simibubi.create.api.packager.InventoryIdentifier;
import net.createmod.catnip.math.BlockFace;

/**
 * 按维度网络 id 归一化的库存标识。
 * <p>
 * 同一网络的所有 net_pathway 方块都会解析出 netId 相同的实例，record 的 equals/hashCode 基于 netId，
 * 因此 Create 物流管理器（{@code LogisticsManager.createSummaryOfNetwork} 的
 * {@code Set<InventoryIdentifier> processedInventories}）会把它们视为同一个库存，只计数一次；
 * 请求分组（{@code findPackagersForRequest}）也会把同网络的链接归为一组。
 */
public record NetInventoryIdentifier(int netId) implements InventoryIdentifier {

    @Override
    public boolean contains(BlockFace face) {
        // BlockFace 不携带 Level，无法从坐标反查方块实体所属网络，因此这里恒返回 false。
        // contains 的唯一调用点是 PackagerBlockEntity.isTargetingSameInventory 的 identifier 分支，
        // 该分支在 BD 网络场景下已由 PackagerBlockEntityMixin 在方法入口处按 handler → netId 完整兜底，
        // 与这里的 false 组合后对 BD 场景无遗漏、对非 BD 场景无回归。
        return false;
    }
}
