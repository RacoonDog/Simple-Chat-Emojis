package io.github.racoondog.emoji.simplechatemojis.mixin;

import io.github.racoondog.emoji.simplechatemojis.Emoji;
import io.github.racoondog.emoji.simplechatemojis.SimpleChatEmojis;
import io.github.racoondog.emoji.simplechatemojis.Utils;
import it.unimi.dsi.fastutil.Pair;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Style;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.regex.Matcher;

@Environment(EnvType.CLIENT)
@Mixin(DrawContext.class)
public abstract class DrawContextMixin {
    @Shadow public abstract int drawText(TextRenderer textRenderer, OrderedText text, int x, int y, int color, boolean shadow);

    @Inject(method = "drawTextWithShadow(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/text/OrderedText;III)I", at = @At("HEAD"), cancellable = true)
    private void injectDrawWithShadow(TextRenderer renderer, OrderedText text, int x, int y, int color, CallbackInfoReturnable<Integer> cir) {
        List<Pair<String, Style>> dissected = Utils.dissect(text);

        x = drawWithEmoji(renderer, dissected, x, y, color, true);
        cir.setReturnValue(x + 1);
    }

    @Unique
    private int drawWithEmoji(TextRenderer renderer, List<Pair<String, Style>> dissected, int x, int y, int color, boolean shadow) {
        for (var textEntry : dissected) {
            x = drawWithEmoji(renderer, textEntry.left(), textEntry.right(), x, y, color, shadow);
        }

        return x;
    }

    @Unique
    private int drawWithEmoji(TextRenderer renderer, String content, Style style, int x, int y, int color, boolean shadow) {
        Matcher matcher = SimpleChatEmojis.EMOJI_REGEX.matcher(content);

        while (matcher.find()) {
            @Nullable Emoji emoji = SimpleChatEmojis.REGISTRY.get(matcher.group());

            if (emoji == null) {
                if (matcher.end() == content.length())
                    return drawShadowFix(renderer, Utils.toOrderedText(content, style), x, y, color, shadow);
                else {
                    x = drawShadowFix(renderer, Utils.toOrderedText(content.substring(0, matcher.end()), style), x, y, color, shadow);
                    content = content.substring(matcher.end());
                    matcher.reset(content);
                    continue;
                }
            }

            if (matcher.start() > 0) {
                x = drawShadowFix(renderer, Utils.toOrderedText(content.substring(0, matcher.start()), style), x, y, color, shadow);
            }

            emoji.render((DrawContext) (Object) this, x, y, renderer.fontHeight, color);
            x += renderer.fontHeight;

            content = content.substring(matcher.end());
            matcher.reset(content);
        }

        if (content.isEmpty()) return x;
        return drawShadowFix(renderer, Utils.toOrderedText(content, style), x, y, color, shadow);
    }

    @Unique
    private int drawShadowFix(TextRenderer renderer, OrderedText text, int x, int y, int color, boolean shadow) {
        x = drawText(renderer, text, x, y, color, shadow);
        return x - (shadow ? 1 : 0);
    }
}
