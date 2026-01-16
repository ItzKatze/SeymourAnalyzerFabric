package schnerry.seymouranalyzer.render;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages and renders highlighted block positions in the world.
 * Renders through walls by temporarily disabling depth test during render.
 */
public class BlockHighlighter {
    private static BlockHighlighter instance;
    private final List<BlockPos> highlightedBlocks = new ArrayList<>();

    private BlockHighlighter() {
        // Use LAST event to render after everything else
        WorldRenderEvents.LAST.register(this::renderHighlights);
    }

    public static BlockHighlighter getInstance() {
        if (instance == null) {
            instance = new BlockHighlighter();
        }
        return instance;
    }

    public void addBlock(BlockPos pos) {
        if (!highlightedBlocks.contains(pos)) {
            highlightedBlocks.add(pos);
        }
    }

    public void addBlocks(List<BlockPos> positions) {
        for (BlockPos pos : positions) {
            addBlock(pos);
        }
    }

    public void clearAll() {
        highlightedBlocks.clear();
    }

    private void renderHighlights(WorldRenderContext context) {
        if (highlightedBlocks.isEmpty()) return;

        MatrixStack matrices = context.matrixStack();
        if (matrices == null) return;

        VertexConsumerProvider.Immediate immediate = context.consumers() instanceof VertexConsumerProvider.Immediate imm
            ? imm : null;
        if (immediate == null) return;

        Vec3d camera = context.camera().getPos();

        matrices.push();
        matrices.translate(-camera.x, -camera.y, -camera.z);

        Matrix4f positionMatrix = matrices.peek().getPositionMatrix();

        // Green color
        float red = 0.0f;
        float green = 1.0f;
        float blue = 0.0f;
        float alpha = 1.0f;

        // Get vertex consumer for lines
        VertexConsumer consumer = immediate.getBuffer(RenderLayer.getLines());

        // Draw all highlights
        for (BlockPos pos : highlightedBlocks) {
            drawBoxOutline(consumer, positionMatrix, pos, red, green, blue, alpha);
        }

        // Force draw the buffered vertices
        immediate.draw(RenderLayer.getLines());

        matrices.pop();
    }

    private void drawBoxOutline(VertexConsumer consumer, Matrix4f matrix, BlockPos pos,
                                 float red, float green, float blue, float alpha) {
        float minX = pos.getX();
        float minY = pos.getY();
        float minZ = pos.getZ();
        float maxX = pos.getX() + 1.0f;
        float maxY = pos.getY() + 1.0f;
        float maxZ = pos.getZ() + 1.0f;

        // Bottom edges
        line(consumer, matrix, minX, minY, minZ, maxX, minY, minZ, red, green, blue, alpha);
        line(consumer, matrix, maxX, minY, minZ, maxX, minY, maxZ, red, green, blue, alpha);
        line(consumer, matrix, maxX, minY, maxZ, minX, minY, maxZ, red, green, blue, alpha);
        line(consumer, matrix, minX, minY, maxZ, minX, minY, minZ, red, green, blue, alpha);

        // Top edges
        line(consumer, matrix, minX, maxY, minZ, maxX, maxY, minZ, red, green, blue, alpha);
        line(consumer, matrix, maxX, maxY, minZ, maxX, maxY, maxZ, red, green, blue, alpha);
        line(consumer, matrix, maxX, maxY, maxZ, minX, maxY, maxZ, red, green, blue, alpha);
        line(consumer, matrix, minX, maxY, maxZ, minX, maxY, minZ, red, green, blue, alpha);

        // Vertical edges
        line(consumer, matrix, minX, minY, minZ, minX, maxY, minZ, red, green, blue, alpha);
        line(consumer, matrix, maxX, minY, minZ, maxX, maxY, minZ, red, green, blue, alpha);
        line(consumer, matrix, maxX, minY, maxZ, maxX, maxY, maxZ, red, green, blue, alpha);
        line(consumer, matrix, minX, minY, maxZ, minX, maxY, maxZ, red, green, blue, alpha);
    }

    private void line(VertexConsumer consumer, Matrix4f matrix,
                      float x1, float y1, float z1, float x2, float y2, float z2,
                      float r, float g, float b, float a) {
        // Calculate normal for line
        float dx = x2 - x1;
        float dy = y2 - y1;
        float dz = z2 - z1;
        float len = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (len == 0) len = 1;
        float nx = dx / len;
        float ny = dy / len;
        float nz = dz / len;

        consumer.vertex(matrix, x1, y1, z1).color(r, g, b, a).normal(nx, ny, nz);
        consumer.vertex(matrix, x2, y2, z2).color(r, g, b, a).normal(nx, ny, nz);
    }
}
