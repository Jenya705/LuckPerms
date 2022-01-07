package me.lucko.luckperms.minestom.inject;

import me.lucko.luckperms.common.calculator.result.TristateResult;
import me.lucko.luckperms.common.context.QueryOptionsCache;
import me.lucko.luckperms.common.model.User;
import me.lucko.luckperms.common.verbose.event.PermissionCheckEvent;
import me.lucko.luckperms.minestom.context.MinestomContextManager;
import net.luckperms.api.query.QueryOptions;
import net.luckperms.api.util.Tristate;
import net.minestom.server.entity.Player;
import net.minestom.server.network.player.PlayerConnection;
import net.minestom.server.permission.Permission;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * @author Jenya705
 */
public class LPMinestomPlayer extends Player {

    private final QueryOptionsCache<Player> queryOptionsSupplier;
    private User user;

    public LPMinestomPlayer(UUID uuid, String nickname, PlayerConnection connection, MinestomContextManager contextManager, User user) {
        super(uuid, nickname, connection);
        this.queryOptionsSupplier = new QueryOptionsCache<>(this, contextManager);
        this.user = user;
    }

    @Override
    public @Nullable Permission getPermission(@NotNull String permissionName) {
        if (permissionName == null) {
            throw new NullPointerException("permission");
        }
        if (user == null) {
            return null;
        }
        QueryOptions queryOptions = this.queryOptionsSupplier.getQueryOptions();
        TristateResult result = this.user.getCachedData().getPermissionData(queryOptions).checkPermission(permissionName, PermissionCheckEvent.Origin.PLATFORM_LOOKUP_CHECK);
        if (result.result() == Tristate.TRUE) {
            return new Permission(permissionName);
        }
        // null indicates false
        return null;
    }

    public void setUser(User user) {
        this.user = user;
    }

}
