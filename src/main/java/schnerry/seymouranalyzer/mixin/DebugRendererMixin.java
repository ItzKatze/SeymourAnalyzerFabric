package schnerry.seymouranalyzer.mixin;

import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.debug.DebugRenderer;
import net.minecraft.client.util.BufferAllocator;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import schnerry.seymouranalyzer.render.BlockHighlighter;

@Mixin(DebugRenderer.class)
public class DebugRendererMixin {
    @Inject(method = "render", at = @At("TAIL"))
    private void onDebugRender(
            Frustum frustum,
            double cameraX,
            double cameraY,
            double cameraZ,
            float tickProgress,
            CallbackInfo ci
    ) {
        BlockHighlighter highlighter = BlockHighlighter.getInstance();
        if (!highlighter.hasHighlights()) return;

        MatrixStack matrices = new MatrixStack();
        Vec3d cameraPos = new Vec3d(cameraX, cameraY, cameraZ);
        try (BufferAllocator allocator = new BufferAllocator(262_144)) {
            VertexConsumerProvider.Immediate vertexConsumers = VertexConsumerProvider.immediate(allocator);
            highlighter.renderHighlights(matrices, vertexConsumers, cameraPos);
            vertexConsumers.draw();
        }
    }
}



