package io.github.racoondog.emoji.simplechatemojis;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.texture.MissingSprite;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.resource.Resource;
import net.minecraft.util.Identifier;

import java.io.IOException;

public class Emoji {
    private static final Emoji MISSING = new Emoji(
            MissingSprite.getMissingSpriteId(),
            MissingSprite.getMissingSpriteTexture().getImage().getHeight(),
            MissingSprite.getMissingSpriteTexture().getImage().getWidth()
    );

    public final Identifier id;
    private final int texHeight;
    private final int texWidth;

    private Emoji(Identifier identifier, int texHeight, int texWidth) {
        this.id = identifier;
        this.texHeight = texHeight;
        this.texWidth = texWidth;
    }

    public static Emoji fromResource(Identifier identifier, Resource texture) {
        try (var inputStream = texture.getInputStream();
             var nativeImage = NativeImage.read(inputStream)) {
            return new Emoji(identifier, nativeImage.getHeight(), nativeImage.getWidth());
        } catch (IOException e) {
            e.printStackTrace();
        }

        return MISSING;
    }

    public void render(DrawContext context, int x, int y, int fontHeight) {
        int ratio = Math.max(texHeight, texWidth) / fontHeight;

        int height = texHeight / ratio;
        int width = texWidth / ratio;

        context.drawTexture(id, x, y, width, height, 0, 0, texWidth, texHeight, texWidth, texHeight);
    }
}
