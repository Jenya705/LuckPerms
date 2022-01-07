package me.lucko.luckperms.minestom.inject;

import me.lucko.luckperms.minestom.LPMinestomPlugin;
import me.lucko.luckperms.minestom.context.MinestomContextManager;
import net.minestom.server.entity.Player;
import net.minestom.server.network.PlayerProvider;
import net.minestom.server.network.player.PlayerConnection;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * @author Jenya705
 */
public class LPMinestomPlayerProvider implements PlayerProvider {

    private final LPMinestomPlugin plugin;

    public LPMinestomPlayerProvider(LPMinestomPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull Player createPlayer(@NotNull UUID uuid, @NotNull String username, @NotNull PlayerConnection connection) {
        return new LPMinestomPlayer(uuid, username, connection, (MinestomContextManager) plugin.getContextManager(), null);
    }
}
