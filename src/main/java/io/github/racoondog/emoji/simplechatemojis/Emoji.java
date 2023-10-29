package io.github.racoondog.emoji.simplechatemojis;

import com.google.gson.JsonObject;
import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.texture.MissingSprite;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import org.lwjgl.stb.STBImage;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

public class Emoji {
    private static final Emoji MISSING = new Emoji(
            MissingSprite.getMissingSpriteId(),
            MissingSprite.getMissingSpriteId().getPath(),
            MissingSprite.getMissingSpriteTexture().getImage().getHeight(),
            MissingSprite.getMissingSpriteTexture().getImage().getWidth(),
            false
    );

    public final Identifier id;
    public final String name;
    public boolean animated;
    private final int texWidth;
    private final int texHeight;
    private int fullHeight = 0;
    private int fullWidth = 0;
    private final int frametime;
    private int ticks = 0;
    private int pos = 0;

    private Emoji(Identifier identifier, String name, int texWidth, int texHeight, boolean animated, int frametime) {
        this.id = identifier;
        this.name = name;
        this.texHeight = texHeight;
        this.texWidth = texWidth;
        this.animated = animated;
        this.frametime = frametime;
        ResourceManager resourceManager = MinecraftClient.getInstance().getResourceManager();
        if (resourceManager.getResource(identifier).isPresent()) {
            try {
                InputStream inputStream = resourceManager.getResource(identifier).get().getInputStream();
                NativeImage image = NativeImage.read(inputStream);
                fullHeight = image.getHeight();
                fullWidth = image.getWidth();
                inputStream.close();
                image.close();
            } catch (IOException ignored) {}
        } else {
        fullWidth = texWidth;
        fullHeight = texHeight;
        }
    }

    private Emoji(Identifier identifier, String name, int texWidth, int texHeight, boolean animated) {
        this(identifier, name, texWidth, texHeight, animated, 0);
    }

    private Emoji(Identifier identifier, String name, int texWidth, int texHeight) {
        this(identifier, name, texWidth, texHeight, false, 0);
    }

    public static Emoji fromResource(Identifier identifier, Resource texture) {
        Identifier mcmetaId = new Identifier(identifier.getNamespace(), identifier.getPath()+".mcmeta");
        ResourceManager resourceManager = MinecraftClient.getInstance().getResourceManager();
        if (resourceManager.getResource(mcmetaId).isPresent()) {
            String json = Utils.readFromIdentifier(mcmetaId);
            JsonObject mcmeta = JsonHelper.deserialize(json);
            JsonObject animation = mcmeta.get("animation").getAsJsonObject();
            return new Emoji(identifier, identifier.getPath(), animation.get("width").getAsInt(), animation.get("height").getAsInt(), true, animation.get("frametime").getAsInt());
        } else {
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
    }
    public void render(DrawContext context, int x, int y, int fontHeight, int color) {
        int ratio = Math.max(texHeight, texWidth) / fontHeight;
        int height = texHeight / ratio;
        int width = texWidth / ratio;

        if (animated) {
            int newTicks = MinecraftClient.getInstance().inGameHud.getTicks();
            if (newTicks - ticks >= frametime) {
                if (pos >= (fullHeight / texHeight) - 1) {
                    pos = 0;
                } else {
                    pos++;
                }
                ticks = newTicks;
            }

            // drawtexture with right frame
            drawTexture(context, id, x, y - 1, width, height, 0, texHeight*pos, texWidth, texHeight, fullWidth, fullHeight, color);
        } else {
            drawTexture(context, id, x, y - 1, width, height, 0, 0, texWidth, texHeight, fullWidth, fullHeight, color);
        }
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
