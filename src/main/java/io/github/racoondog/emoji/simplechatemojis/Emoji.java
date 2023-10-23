package io.github.racoondog.emoji.simplechatemojis;

import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.texture.MissingSprite;
import net.minecraft.resource.Resource;
import net.minecraft.util.Identifier;
import org.lwjgl.stb.STBImage;

import java.io.IOException;
import java.nio.ByteBuffer;

public class Emoji {
    private static final Emoji MISSING = new Emoji(
            MissingSprite.getMissingSpriteId(),
            MissingSprite.getMissingSpriteId().getPath(),
            MissingSprite.getMissingSpriteTexture().getImage().getHeight(),
            MissingSprite.getMissingSpriteTexture().getImage().getWidth()
    );

    public final Identifier id;
    public final String name;
    private final int texWidth;
    private final int texHeight;

    private Emoji(Identifier identifier, String name, int texWidth, int texHeight) {
        this.id = identifier;
        this.name = name;
        this.texHeight = texHeight;
        this.texWidth = texWidth;
    }

    public static Emoji fromResource(Identifier identifier, Resource texture) {
        try (var inputStream = texture.getInputStream()) {
            ByteBuffer buffer = TextureUtil.readResource(inputStream).rewind();
            int[] x = new int[1];
            int[] y = new int[1];
            STBImage.stbi_info_from_memory(buffer, x, y, new int[1]);
            return new Emoji(identifier, identifier.getPath(), x[0], y[0]);
        } catch (IOException e) {
            e.printStackTrace();
            return MISSING;
        }
    }

    public void render(DrawContext context, int x, int y, int fontHeight, int color) {
        int ratio = Math.max(texHeight, texWidth) / fontHeight;

        int height = texHeight / ratio;
        int width = texWidth / ratio;

        drawTexture(context, id, x, y - 1, width, height, 0, 0, texWidth, texHeight, texWidth, texHeight, color);
    }

    public void drawTexture(DrawContext context, Identifier texture, int x, int y, int width, int height, float u, float v, int regionWidth, int regionHeight, int textureWidth, int textureHeight, int color) {
        RenderSystem.enableBlend();
        int alpha = (color >> 24) & 0xFF;
        if (alpha == 0) alpha = 0xFF; // Chat box has alpha set to 0
        RenderSystem.setShaderColor(1, 1, 1, alpha / 255f);

        context.drawTexture(texture, x, y, width, height, u, v, regionWidth, regionHeight, textureWidth, textureHeight);

        RenderSystem.setShaderColor(1, 1, 1, 1);
        RenderSystem.disableBlend();
    }
}
