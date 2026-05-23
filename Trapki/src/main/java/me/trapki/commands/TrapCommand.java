package me.trapki.commands;

import me.trapki.TrapkiPlugin;
import me.trapki.managers.TrapItemManager;
import me.trapki.models.TrapType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class TrapCommand implements CommandExecutor {

    private final TrapkiPlugin plugin;

    public TrapCommand(TrapkiPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd,
                             @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("trapki.admin")) {
            sender.sendMessage(Component.text("✗ Нет прав.").color(TextColor.color(0xFF4444)));
            return true;
        }
        if (args.length == 0) { usage(sender); return true; }

        switch (args[0].toLowerCase()) {
            case "list" -> {
                sender.sendMessage(Component.text("Доступные трапки:").color(TextColor.color(0xFFAA00)));
                for (TrapType t : TrapType.values()) {
                    sender.sendMessage(Component.text("  " + t.id() + "  ")
                        .color(TextColor.color(0xAAAAAA))
                        .append(Component.text("(" + t.size() + "×" + t.size() + ", " + t.defaultPrice() + " ✦)")
                            .color(TextColor.color(0xFFCC55))));
                }
            }
            case "give" -> {
                if (args.length < 3) { usage(sender); return true; }
                TrapType type = TrapType.byId(args[1]);
                if (type == null) {
                    sender.sendMessage(Component.text("✗ Нет такой трапки: " + args[1])
                        .color(TextColor.color(0xFF4444)));
                    return true;
                }
                Player target = Bukkit.getPlayerExact(args[2]);
                if (target == null) {
                    sender.sendMessage(Component.text("✗ Игрок не онлайн: " + args[2])
                        .color(TextColor.color(0xFF4444)));
                    return true;
                }
                int amount = 1;
                if (args.length >= 4) {
                    try { amount = Math.max(1, Math.min(64, Integer.parseInt(args[3]))); }
                    catch (NumberFormatException ignore) {}
                }
                ItemStack item = TrapItemManager.createTrap(type, amount);
                drop(target, item);
                sender.sendMessage(Component.text("✓ Выдано: " + type.id() + " ×" + amount + " → " + target.getName())
                    .color(TextColor.color(0x55FF55)));
            }
            case "scrap" -> {
                if (args.length < 2) { usage(sender); return true; }
                Player target = Bukkit.getPlayerExact(args[1]);
                if (target == null) {
                    sender.sendMessage(Component.text("✗ Игрок не онлайн: " + args[1])
                        .color(TextColor.color(0xFF4444)));
                    return true;
                }
                int amount = 1;
                if (args.length >= 3) {
                    try { amount = Math.max(1, Math.min(64, Integer.parseInt(args[2]))); }
                    catch (NumberFormatException ignore) {}
                }
                drop(target, TrapItemManager.createScrap(amount));
                sender.sendMessage(Component.text("✓ Выдан обломок незерита ×" + amount + " → " + target.getName())
                    .color(TextColor.color(0x55FF55)));
            }
            case "reload" -> {
                plugin.reloadConfig();
                plugin.reloadPricesFromConfig();
                sender.sendMessage(Component.text("✓ config.yml Trapki перезагружен.")
                    .color(TextColor.color(0x55FF55)));
            }
            default -> usage(sender);
        }
        return true;
    }

    private void usage(CommandSender s) {
        s.sendMessage(Component.text("/trap give <тип> <ник> [кол]  — выдать трапку").color(TextColor.color(0xAAAAAA)));
        s.sendMessage(Component.text("/trap scrap <ник> [кол]        — выдать обломок незерита").color(TextColor.color(0xAAAAAA)));
        s.sendMessage(Component.text("/trap list                      — список доступных типов").color(TextColor.color(0xAAAAAA)));
        s.sendMessage(Component.text("/trap reload                    — перезагрузить config.yml").color(TextColor.color(0xAAAAAA)));
    }

    private static void drop(Player target, ItemStack item) {
        Map<Integer, ItemStack> leftover = target.getInventory().addItem(item);
        for (ItemStack drop : leftover.values()) {
            target.getWorld().dropItemNaturally(target.getLocation(), drop);
        }
    }
}
