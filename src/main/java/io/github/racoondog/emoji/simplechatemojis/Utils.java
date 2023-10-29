package io.github.racoondog.emoji.simplechatemojis;

import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.ObjectObjectMutablePair;
import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Utils {
    public static List<Pair<String, Style>> dissect(OrderedText orderedText) {
        List<Pair<String, Style>> list = new ArrayList<>();

        orderedText.accept((index, style, codePoint) -> {
            if (list.isEmpty() || !list.get(list.size() - 1).right().equals(style)) {
                list.add(new ObjectObjectMutablePair<>(Character.toString(codePoint), style));
            } else {
                Pair<String, Style> last = list.get(list.size() - 1);
                last.left(last.left() + Character.toString(codePoint));
            }

            return true;
        });

        return list;
    }

    public static OrderedText toOrderedText(String text, Style style) {
        return Text.literal(text).setStyle(style).asOrderedText();
    }

    public static String nameFromIdentifier(Identifier id) {
        StringBuilder sb = new StringBuilder(":");
        int slashIdx = id.getPath().lastIndexOf('/');
        int periodIdx = id.getPath().indexOf('.', slashIdx);
        sb.append(id.getPath(), slashIdx == -1 ? 0 : slashIdx + 1, periodIdx == -1 ? id.getPath().length() - 4 : periodIdx);
        sb.append(':');
        return sb.toString();
    }

    public static String readFromIdentifier(Identifier id) {
        ResourceManager resourceManager = MinecraftClient.getInstance().getResourceManager();
        try {
            Optional<Resource> resource = resourceManager.getResource(id);
            if (resource.isPresent()) {
                InputStream resourceStream = resource.get().getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(resourceStream));
                StringBuilder builder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) builder.append(line).append("\n");
                resourceStream.close();
                return builder.toString();
            }
            return "";
        } catch (IOException e) {
            return "";
        }
    }
}
