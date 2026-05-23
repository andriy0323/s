package me.regionblocks.util;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;

/**
 * Утилита для создания «голов» (player heads) с кастомными текстурами.
 *
 * Поддерживает два формата:
 *   - Полная Mojang-base64 строка (как с minecraft-heads.com → For developers → Value).
 *   - 64-символьный хеш текстуры (как с minecraft-heads.com → Texture URL).
 *
 * Если строка пустая или некорректная, возвращается обычная PLAYER_HEAD без
 * текстуры (без падений).
 */
public final class HeadUtil {

    private static final String BASE_URL = "http://textures.minecraft.net/texture/";

    private HeadUtil() {}

    /** Голова с кастомной текстурой. Принимает либо base64-строку, либо texture-hash. */
    public static ItemStack customHead(String textureValue) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        if (textureValue == null || textureValue.isEmpty()) return head;
        applyTexture(head, textureValue);
        return head;
    }

    /** Применяет текстуру к уже существующему ItemStack-PLAYER_HEAD'у (in-place). */
    public static void applyTexture(ItemStack head, String textureValue) {
        if (head == null || head.getType() != Material.PLAYER_HEAD) return;
        if (textureValue == null || textureValue.isEmpty()) return;

        SkullMeta meta = (SkullMeta) head.getItemMeta();
        if (meta == null) return;

        String base64;
        if (textureValue.length() == 64 && isHex(textureValue)) {
            // Это texture-hash → собираем JSON и base64-кодируем.
            String json = "{\"textures\":{\"SKIN\":{\"url\":\"" + BASE_URL + textureValue + "\"}}}";
            base64 = Base64.getEncoder().encodeToString(json.getBytes(StandardCharsets.UTF_8));
        } else {
            // Считаем, что это уже готовая Mojang-base64 строка.
            base64 = textureValue;
        }

        UUID id = UUID.nameUUIDFromBytes(base64.getBytes(StandardCharsets.UTF_8));
        PlayerProfile profile = Bukkit.createProfile(id, null);
        profile.getProperties().add(new ProfileProperty("textures", base64));
        meta.setPlayerProfile(profile);
        head.setItemMeta(meta);
    }

    private static boolean isHex(String s) {
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (!((c >= '0' && c <= '9') || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F'))) return false;
        }
        return true;
    }
}
