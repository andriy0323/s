package me.arisdonate.commands;

import me.arisdonate.ArisDonatePlugin;
import me.arisdonate.managers.KitManager;
import me.arisdonate.util.BaseCommand;
import me.arisdonate.util.Msg;
import me.arisdonate.util.TimeUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class KitCommand extends BaseCommand {
    public KitCommand(ArisDonatePlugin plugin) { super(plugin); }

    @Override
    protected void execute(CommandSender sender, Command command, String label, String[] args) {
        Player p = requirePlayer(sender);
        if (p == null) return;
        if (args.length == 0) {
            plugin.getKitsGui().open(p);
            return;
        }
        KitManager.Kit kit = plugin.getKitManager().getKit(args[0]);
        if (kit == null) { p.sendMessage(Msg.parse("&cКит не найден. Открой &e/kits &cдля списка.")); return; }
        if (!plugin.getKitManager().canTakeKit(p, kit)) {
            p.sendMessage(Msg.parse("&cНет доступа к этому киту. Купите донат: &e/donate"));
            return;
        }
        long left = plugin.getKitManager().cooldownLeft(p, kit.id);
        if (left > 0) {
            p.sendMessage(Msg.parse("&cКит на кулдауне: ещё &e" + TimeUtil.fmt(left)));
            return;
        }
        plugin.getKitManager().giveKit(p, kit);
        p.sendMessage(Msg.parse("&aПолучен кит " + kit.displayName));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1) {
            List<String> ids = new ArrayList<>();
            for (KitManager.Kit k : plugin.getKitManager().all()) ids.add(k.id);
            return ids;
        }
        return List.of();
    }
}
