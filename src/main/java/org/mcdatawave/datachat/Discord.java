package org.mcdatawave.datachat;

import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.ReadyEvent;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.concurrent.TimeUnit;

public class Discord extends ListenerAdapter implements Listener {

    private DataChat plugin;
    private JDA jda;

    Discord(DataChat main){
        this.plugin = main;
        startBot();
        plugin.getServer().getPluginManager().registerEvents(this,plugin);
        jda.addEventListener(this);
    }

    private TextChannel getGeneral() {
        return jda.getTextChannelById(plugin.getConfig().getString("General"));
    }

    private TextChannel getConsole() {
        return jda.getTextChannelById(plugin.getConfig().getString("Console"));
    }

    private String getInvite() {
        return plugin.getConfig().getString("DiscInvite");
    }

    // Starts the Discord Bot
    private void startBot() {
        try {
            jda = new JDABuilder(AccountType.BOT).setToken(plugin.getConfig().getString("BotToken")).buildAsync();
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to log into discord bot!");
        }
    }

    // startup msg
    @Override
    public void onReady(ReadyEvent event){
        getGeneral().sendMessage("**Server has started**").queue();
    }

    // shutdown msg
    void shutdownMessage(){
        getGeneral().sendMessage("**Server has shut down**").complete();
    }

    // Sends messages from minecraft to Discord
    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        String message = event.getMessage();
        if (message.toLowerCase().contains("@here") || message.toLowerCase().contains("@everyone")) return;
        getGeneral().sendMessage("**" + event.getPlayer().getName() + ":** " + message).queue();
    }

    // join game msg
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        getGeneral().sendMessage("**" + event.getPlayer().getName() + " joined the game**").queue();
    }

    // left game msg
    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        getGeneral().sendMessage("**" + event.getPlayer().getName() + " left the game**").queue();
    }

    // Sends messages from disc to minecraft
    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {

        if (event.getAuthor().isBot() || event.getAuthor().isFake() || event.isWebhookMessage()) return;

        String message = event.getMessage().getContentRaw();
        String user = event.getAuthor().getName();

        TextComponent b0 = new TextComponent("[");
        b0.setColor(ChatColor.DARK_PURPLE);
        TextComponent b1 = new TextComponent("]");
        b1.setColor(ChatColor.DARK_PURPLE);

        TextComponent disc = new TextComponent("Discord");
        disc.setColor(ChatColor.LIGHT_PURPLE);
        disc.setClickEvent(new ClickEvent(
                ClickEvent.Action.OPEN_URL,getInvite())
        );
        disc.setHoverEvent(new HoverEvent(
                HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Join our discord!").create())
        );

        // General chat messages
        if (event.getChannel().equals(getGeneral())){
            TextComponent msg = new TextComponent(" " + user + ": " + message);
            for (Player p : Bukkit.getOnlinePlayers()) {
                p.spigot().sendMessage(b0, disc, b1, msg);
            }
        }

        // Console messages
        if (event.getChannel().equals(getConsole())) {
            Bukkit.getScheduler().callSyncMethod(plugin,
                    () -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), message)
            );
        }

        // list command
        if (message.equals("/list")) {

            getGeneral().deleteMessageById(event.getMessageId()).queueAfter(5, TimeUnit.SECONDS);

            StringBuilder onlineList = new StringBuilder();

            for (Player p : Bukkit.getOnlinePlayers()) {
                onlineList.append(p.getName());
                onlineList.append(", ");
            }

            if (Bukkit.getOnlinePlayers().isEmpty()){
                getGeneral().sendMessage("**No one is currently online**").queue(
                        message1 -> message1.delete().queueAfter(5, TimeUnit.SECONDS));
            }else {
                getGeneral().sendMessage("Players online - **" + onlineList + "**").queue(
                        message1 -> message1.delete().queueAfter(5, TimeUnit.SECONDS));
            }
        }
    }
}
