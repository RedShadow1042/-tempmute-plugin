package me.shadow.tempmute;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;

public final class Tempmute extends JavaPlugin implements Listener {
    private Map<Player, Long> mutedPlayers = new HashMap<>();

    @Override
    public void onEnable() {
        // Register the event listener
        Bukkit.getPluginManager().registerEvents(this, this);

        getLogger().info("Plugin has started!");

        // Start the task to check and unmute players
        getServer().getScheduler().runTaskTimer(this, this::checkAndUnmutePlayers, 20, 20);
    }

    private long parseTimeArgument(String timeArgument) {
        try {
            if (timeArgument.endsWith("s")) {
                return Long.parseLong(timeArgument.substring(0, timeArgument.length() - 1));
            } else if (timeArgument.endsWith("m")) {
                return Long.parseLong(timeArgument.substring(0, timeArgument.length() - 1)) * 60;
            } else if (timeArgument.endsWith("h")) {
                return Long.parseLong(timeArgument.substring(0, timeArgument.length() - 1)) * 3600;
            } else {
                return Long.parseLong(timeArgument);
            }
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private String buildReasonArgument(String[] args, int startIndex) {
        StringBuilder reason = new StringBuilder();
        for (int i = startIndex; i < args.length; i++) {
            reason.append(args[i]).append(" ");
        }
        return reason.toString().trim();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (command.getName().equalsIgnoreCase("tempmute")) {
            if (sender instanceof Player) {

                String playerName = args[0];
                Player p = (Player) sender;
                Player target = Bukkit.getServer().getPlayerExact(playerName);

                if (target == null) {
                    p.sendMessage("Player not found");
                } else if (args.length < 3) {
                    p.sendMessage("Usage: /tempmute <player> <time> <reason>");
                } else {
                    long muteDuration = parseTimeArgument(args[1]);
                    if (muteDuration <= 0) {
                        p.sendMessage("Invalid mute duration.");
                        return true;
                    }

                    String reason = buildReasonArgument(args, 2);
                    mutePlayer(target, muteDuration);
                    target.sendMessage(ChatColor.AQUA + "You have been tempmuted by: " + ChatColor.RED + p.getName() + ChatColor.AQUA + " for: " + ChatColor.RED + muteDuration + " seconds. Reason: " + ChatColor.RED + reason);
                    p.sendMessage("Muted " + target.getName() + " for " + muteDuration + " seconds. Reason: " + reason);
                }
                return true; // Return true here to indicate the command was handled

            } else {
                sender.sendMessage("This command can only be executed by a player.");
                return false; // Return false here if the command was not handled
            }
        }

        return false; // Return false here if the command was not handled
    }

    private void mutePlayer(Player player, long muteDuration) {
        mutedPlayers.put(player, System.currentTimeMillis() + (muteDuration * 1000));
    }

    private void unmutePlayer(Player player) {
        mutedPlayers.remove(player);
    }

    private boolean isPlayerMuted(Player player) {
        return mutedPlayers.containsKey(player);
    }

    private void checkAndUnmutePlayers() {
        long currentTime = System.currentTimeMillis();

        for (Player player : mutedPlayers.keySet()) {
            long unmuteTime = mutedPlayers.get(player);
            if (unmuteTime <= currentTime) {
                unmutePlayer(player);
                player.sendMessage(ChatColor.GREEN + "You have been unmuted.");
            }
        }
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        if (isPlayerMuted(player)) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "You are currently muted and cannot chat.");
        }
    }
}
