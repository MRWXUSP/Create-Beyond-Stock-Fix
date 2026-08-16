package com.cbdfix;

import com.cbdfix.inventory.NetPathwayFinder;
import com.mojang.logging.LogUtils;
import com.simibubi.create.api.packager.InventoryIdentifier;
import com.wintercogs.beyonddimensions.common.init.BDBlocks;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import org.slf4j.Logger;

/**
 * Create × BeyondDimensions 兼容修复（NeoForge 1.21.1）。
 * <p>
 * 修复：多个仓储链接站（stock_link）经打包机指向同一维度网络的不同维度网络通道（net_pathway）方块时，
 * 仓储发报机（stock_ticker）会重复计数网络物品。
 * <p>
 * 原理：向 Create 的官方扩展点 {@link InventoryIdentifier#REGISTRY} 注册一个 Finder，
 * 使同一网络的所有通道方块解析出同一个 {@link com.cbdfix.inventory.NetInventoryIdentifier}（按网络 id 归一化），
 * 从而复用 Create 自身的按库存去重机制。
 */
@Mod(CbdFix.MOD_ID)
public class CbdFix {

    public static final String MOD_ID = "cbd_fix";

    private static final Logger LOGGER = LogUtils.getLogger();

    public CbdFix(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::onCommonSetup);
    }

    private void onCommonSetup(FMLCommonSetupEvent event) {
        registerNetPathwayFinder();
    }

    private static void registerNetPathwayFinder() {
        try {
            Block pathway = BDBlocks.NET_PATHWAY.get();
            // 幂等防护：SimpleRegistry 对已注册的键会抛 IllegalArgumentException
            if (InventoryIdentifier.REGISTRY.get(pathway) == null)
                InventoryIdentifier.REGISTRY.register(pathway, NetPathwayFinder.INSTANCE);
        } catch (IllegalArgumentException e) {
            LOGGER.warn("cbd_fix: net_pathway already has an InventoryIdentifier registered, skipping ours", e);
        }
    }
}
