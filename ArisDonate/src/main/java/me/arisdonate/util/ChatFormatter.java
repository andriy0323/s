package me.arisdonate.util;

import me.arisdonate.ArisDonatePlugin;
import me.arisdonate.models.DonateRank;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

/**
 * Префикс доната перед ником в чате и в табе.
 * Для таба используется scoreboard team prefix (стандартный способ
 * отображения цветного префикса в табе и над головой в vanilla).
 */
public class ChatFormatter {

    private final ArisDonatePlugin plugin;

    public ChatFormatter(ArisDonatePlugin plugin) {
        this.plugin = plugin;
    }

    public Component buildPrefix(Player p) {
        DonateRank rank = plugin.getDonateManager().getPlayerRank(p.getName());
        if (rank == null) return Component.empty();
        return Msg.parse(rank.gradientName());
    }

    /** Обновляет tab-prefix через scoreboard team (sortable по weight). */
    public void applyTabPrefix(Player p) {
        Scoreboard sb = Bukkit.getScoreboardManager().getMainScoreboard();
        DonateRank rank = plugin.getDonateManager().getPlayerRank(p.getName());

        // teamId должен сортировать игроков по весу (выше — раньше). Format: "Annn_id"
        String teamId;
        if (rank == null) {
            teamId = "Zzz_default";
        } else {
            int sortWeight = 9999 - rank.weight();
            String w = String.format("%04d", Math.max(0, sortWeight));
            teamId = "ad_" + w + "_" + rank.id();
        }
        if (teamId.length() > 16) teamId = teamId.substring(0, 16);

        // remove player from any existing arisdonate teams
        for (Team t : sb.getTeams()) {
            if (t.getName().startsWith("ad_") || t.getName().startsWith("Zzz_default")) {
                t.removeEntry(p.getName());
            }
        }

        Team team = sb.getTeam(teamId);
        if (team == null) team = sb.registerNewTeam(teamId);

        if (rank != null) {
            // Префикс должен быть Component, чтобы поддерживать градиент
            team.prefix(Msg.parse(rank.gradientName() + " "));
        } else {
            team.prefix(Component.empty());
        }
        team.addEntry(p.getName());

        // Также показываем над головой
        p.setPlayerListName(null); // оставляем имя по умолчанию; team prefix добавит префикс автоматически
    }
}
