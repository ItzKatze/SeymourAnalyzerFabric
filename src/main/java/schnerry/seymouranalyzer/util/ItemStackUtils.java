package schnerry.seymouranalyzer.util;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.DyedColorComponent;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;

public class ItemStackUtils {

    public static String getOrCreateItemUUID(ItemStack stack) {
        NbtComponent nbtComponent = stack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT);
        NbtCompound nbt = nbtComponent.copyNbt();

        if (nbt.contains("uuid")) {
            return nbt.getString("uuid").orElse(null);
        }

        return null;
    }

    public static String extractHex(ItemStack stack) {
        NbtComponent nbtComponent = stack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT);
        NbtCompound nbt = nbtComponent.copyNbt();

        if (nbt.contains("color")) {
            String colorStr = nbt.getString("color").orElse("");
            if (colorStr.contains(":")) {
                return ColorMath.rgbStringToHex(colorStr);
            }
        }

        DyedColorComponent dyedColor = stack.getOrDefault(DataComponentTypes.DYED_COLOR, null);
        if (dyedColor != null) {
            int rgb = dyedColor.rgb();
            return String.format("%02X%02X%02X", (rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, rgb & 0xFF);
        }

        return null;
    }
}
