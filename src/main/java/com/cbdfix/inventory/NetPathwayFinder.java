package com.cbdfix.inventory;

import com.simibubi.create.api.packager.InventoryIdentifier;
import com.wintercogs.beyonddimensions.api.dimensionnet.DimensionsNet;
import com.wintercogs.beyonddimensions.common.block.entity.NetPathwayBlockEntity;
import net.createmod.catnip.math.BlockFace;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * 为超越维度的维度网络通道（net_pathway）解析库存标识：
 * 返回按网络 id 归一化的 {@link NetInventoryIdentifier}，同一网络的所有通道方块共享同一标识值。
 * <p>
 * 注意：仅处理 net_pathway。rs_net_pathway 不暴露物品能力，net_energy_pathway 只暴露能量能力——
 * 二者均不会进入 Create 的物品物流，无需处理。
 */
public class NetPathwayFinder implements InventoryIdentifier.Finder {

    public static final NetPathwayFinder INSTANCE = new NetPathwayFinder();

    private NetPathwayFinder() {
    }

    @Override
    @Nullable
    public InventoryIdentifier find(Level level, BlockState state, BlockFace face) {
        // 物流汇总只在服务端计算；客户端返回 null 保持 Create 原有行为
        if (level.isClientSide())
            return null;

        if (!(level.getBlockEntity(face.getPos()) instanceof NetPathwayBlockEntity pathway))
            return null;

        // getNet() 仅在 ServerLevel 上刷新缓存；null / 已删除网络不参与去重
        DimensionsNet net = pathway.getNet();
        if (net == null || net.deleted)
            return null;

        return new NetInventoryIdentifier(net.getId());
    }
}
