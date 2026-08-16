package com.cbdfix.mixin;

import com.cbdfix.util.BdNetHelper;
import com.simibubi.create.content.logistics.packager.IdentifiedInventory;
import com.simibubi.create.content.logistics.packager.PackagerBlockEntity;
import net.neoforged.neoforge.items.IItemHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 加固 {@link PackagerBlockEntity#isTargetingSameInventory(IdentifiedInventory)}：
 * <p>
 * 当对比双方（本打包机的目标库存与被忽略的库存）都解析出超越维度网络 id 时，直接按网络 id 判断是否为
 * 同一库存。这覆盖了 {@link com.cbdfix.inventory.NetInventoryIdentifier#contains} 无法解析的场景
 * （BlockFace 不携带 Level），例如 FactoryPanel 补货器模式排除自家打包机指向的通道方块：
 * 同一通道方块（等价于原 handler == 比较）与同网不同通道方块（原逻辑失效）两种情况均正确。
 * <p>
 * 任一方不是 BD 网络库存时直接回落原逻辑，行为与未安装本模组时完全一致。
 */
@Mixin(value = PackagerBlockEntity.class, remap = false)
public abstract class PackagerBlockEntityMixin {

    @Inject(method = "isTargetingSameInventory", at = @At("HEAD"), cancellable = true)
    private void cbdFix$sameBdNet(IdentifiedInventory inventory, CallbackInfoReturnable<Boolean> cir) {
        if (inventory == null)
            return;

        IItemHandler mine = ((PackagerBlockEntity) (Object) this).targetInventory.getInventory();
        Integer myNet = BdNetHelper.getNetIdOf(mine);
        Integer otherNet = BdNetHelper.getNetIdOf(inventory.handler());
        if (myNet != null && otherNet != null)
            cir.setReturnValue(myNet.intValue() == otherNet.intValue());
    }
}
