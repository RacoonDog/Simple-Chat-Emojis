package io.github.racoondog.emoji.simplechatemojis;

import com.mojang.logging.LogUtils;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourcePackActivationType;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;

import java.util.*;
import java.util.regex.Pattern;

@Environment(EnvType.CLIENT)
public class SimpleChatEmojis implements ClientModInitializer {
    public static final String MOD_ID = "simple-chat-emojis";
    public static final Pattern EMOJI_REGEX = Pattern.compile("(:[a-z0-9._-]+:)");
    public static final Map<String, Emoji> REGISTRY = new HashMap<>();
    public static final Logger LOG = LogUtils.getLogger();

    @Override
    public void onInitializeClient() {
        FabricLoader.getInstance().getModContainer(MOD_ID).ifPresent(container -> {
            ResourceManagerHelper.registerBuiltinResourcePack(new Identifier(MOD_ID, "developers-guild-emoji-pack"), container, Text.literal("Developer's Guild Emoji Pack"), ResourcePackActivationType.NORMAL);
            ResourceManagerHelper.registerBuiltinResourcePack(new Identifier(MOD_ID, "twemoji-emoji-pack"), container, Text.literal("Twemoji Emoji Pack"), ResourcePackActivationType.DEFAULT_ENABLED);
        });

        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
            @Override
            public Identifier getFabricId() {
                return new Identifier(MOD_ID, "chat_emojis_resources");
            }

            @Override
            public void reload(ResourceManager manager) {
                REGISTRY.clear();

                for (var resourcePair : manager.findResources("textures/simple-chat-emojis", path -> path.getPath().endsWith(".png")).entrySet()) {
                    String emojiName = Utils.nameFromIdentifier(resourcePair.getKey());

                    register(emojiName, Emoji.fromResource(resourcePair.getKey(), resourcePair.getValue()));
                }
            }
        });
    }

    public static void register(String emojiName, Emoji emoji) {
        Emoji old = REGISTRY.put(emojiName, emoji);
        if (old != null) {
            LOG.warn("Duplicate emojis registered under the same name! '%s' & '%s'".formatted(old.id, emoji.id));
        }
    }
}
