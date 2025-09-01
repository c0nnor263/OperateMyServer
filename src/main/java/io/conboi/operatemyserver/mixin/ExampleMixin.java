package io.conboi.operatemyserver.mixin;

import net.minecraft.server.dedicated.DedicatedServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DedicatedServer.class)
public class ExampleMixin {
    @Inject(method = "initServer", at = @At("HEAD"))
    private void onServerStart(CallbackInfoReturnable<Boolean> cir) {
//        OperateMyServer.INSTANCE.getLOGGER().info("");
    }
}
