package com.cbdfix.util;

import com.mojang.logging.LogUtils;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

/**
 * 从物品处理器解析其背后的超越维度网络 id。
 * <p>
 * 路径：{@code ItemUnifiedStorageHandler.storage}（私有字段，无 getter，需反射）→
 * {@code UnifiedStorage.getNet()} → {@code DimensionsNet.getId()}。
 * <p>
 * 全部反射句柄在首次使用时一次性初始化并缓存；任何异常都会永久降级（此后一律返回 null，调用方回落
 * Create 原逻辑），并且只记录一次日志，保证任何情况下都不会反复抛异常。
 */
public final class BdNetHelper {

    private static final Logger LOGGER = LogUtils.getLogger();

    private static final String HANDLER_CLS =
        "com.wintercogs.beyonddimensions.api.capability.helper.unordered.ItemUnifiedStorageHandler";
    private static final String STORAGE_CLS =
        "com.wintercogs.beyonddimensions.api.dimensionnet.UnifiedStorage";
    private static final String NET_CLS =
        "com.wintercogs.beyonddimensions.api.dimensionnet.DimensionsNet";

    private static Class<?> handlerClass;
    private static MethodHandle storageGetter;   // ItemUnifiedStorageHandler.storage
    private static MethodHandle netGetter;       // UnifiedStorage.getNet()
    private static MethodHandle netIdGetter;     // DimensionsNet.getId()
    private static MethodHandle deletedGetter;   // DimensionsNet.deleted
    private static boolean initialized;
    private static boolean failed;

    private BdNetHelper() {
    }

    /**
     * @return 该处理器背后维度网络的 id；不是 BD 的处理器或解析失败返回 null（调用方回落原逻辑）
     */
    public static synchronized @Nullable Integer getNetIdOf(@Nullable IItemHandler handler) {
        if (handler == null || !ensureInit() || !handlerClass.isInstance(handler))
            return null;
        try {
            Object storage = storageGetter.invoke(handler);
            if (storage == null)
                return null;
            Object net = netGetter.invoke(storage);
            if (net == null || (boolean) deletedGetter.invoke(net))
                return null;
            return (int) netIdGetter.invoke(net);
        } catch (Throwable t) {
            failed = true; // 永久降级：之后一律返回 null，不再刷新日志
            LOGGER.error("cbd_fix: BD net id lookup failed, net-pathway dedup disabled", t);
            return null;
        }
    }

    private static synchronized boolean ensureInit() {
        if (initialized || failed)
            return initialized;
        try {
            handlerClass = Class.forName(HANDLER_CLS);
            Class<?> storageClass = Class.forName(STORAGE_CLS);
            Class<?> netClass = Class.forName(NET_CLS);

            MethodHandles.Lookup caller = MethodHandles.lookup();
            storageGetter = MethodHandles.privateLookupIn(handlerClass, caller)
                .findGetter(handlerClass, "storage", storageClass);
            netGetter = caller.findVirtual(storageClass, "getNet", MethodType.methodType(netClass));
            netIdGetter = caller.findVirtual(netClass, "getId", MethodType.methodType(int.class));
            deletedGetter = caller.findGetter(netClass, "deleted", boolean.class);

            initialized = true;
        } catch (Throwable t) {
            failed = true;
            LOGGER.error("cbd_fix: BD reflection setup failed, net-pathway dedup disabled", t);
        }
        return initialized;
    }
}
