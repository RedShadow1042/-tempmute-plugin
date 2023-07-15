package me.shadow.tempmute;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;

public class UnmuteCommand implements CommandExecutor {
    private Map<UUID, Long> mutedPlayers;
    private Connection connection;

    public UnmuteCommand(Map<UUID, Long> mutedPlayers, Connection connection) {
        this.mutedPlayers = mutedPlayers;
        this.connection = connection;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("unmute")) {
            if (args.length == 1) {
                Player target = Bukkit.getPlayerExact(args[0]);

                if (target != null) {
                    unmutePlayer(target);
                    sender.sendMessage("Unmuted " + target.getName() + ".");
                } else {
                    sender.sendMessage("Player not found.");
                }
            } else {
                sender.sendMessage("Usage: /unmute <player>");
            }
            return true;
        }

        return false;
    }

    private void unmutePlayer(Player player) {
        UUID playerUUID = player.getUniqueId();
        mutedPlayers.remove(playerUUID);
        removeMutedPlayer(playerUUID);
    }

    private void removeMutedPlayer(UUID uuid) {
        try (PreparedStatement statement = connection.prepareStatement("DELETE FROM muted_players WHERE player_uuid = ?")) {
            statement.setString(1, uuid.toString());
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
