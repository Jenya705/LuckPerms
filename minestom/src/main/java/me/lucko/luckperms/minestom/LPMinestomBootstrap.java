/*
 * This file is part of LuckPerms, licensed under the MIT License.
 *
 *  Copyright (c) lucko (Luck) <luck@lucko.me>
 *  Copyright (c) contributors
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */

package me.lucko.luckperms.minestom;

import java.util.EnumSet;
import me.lucko.luckperms.common.dependencies.Dependency;
import me.lucko.luckperms.common.plugin.bootstrap.LuckPermsBootstrap;
import me.lucko.luckperms.common.plugin.classpath.ClassPathAppender;
import me.lucko.luckperms.common.plugin.logging.PluginLogger;
import me.lucko.luckperms.common.plugin.logging.Slf4jPluginLogger;
import me.lucko.luckperms.common.plugin.scheduler.SchedulerAdapter;
import me.lucko.luckperms.minestom.inject.LPMinestomPlayerProvider;
import net.luckperms.api.platform.Platform;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.extensions.Extension;
import org.jetbrains.annotations.NotNull;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

public class LPMinestomBootstrap extends Extension implements LuckPermsBootstrap {
    private final Slf4jPluginLogger logger = new Slf4jPluginLogger(LoggerFactory.getLogger("luckperms"));

    // Latches for enable and load
    private final CountDownLatch loadLatch = new CountDownLatch(1);
    private final CountDownLatch enableLatch = new CountDownLatch(1);

    /**
     * The plugin instance
     */
    private final LPMinestomPlugin plugin;
    private final MinestomSchedulerAdapter schedulerAdapter;
    private final MinestomClassPathAppender classPathAppender;

    private final LPMinestomPlayerProvider playerProvider;

    private Instant startupTime;

    public LPMinestomBootstrap() {
        this.plugin = new LPMinestomPlugin(this);
        this.schedulerAdapter = new MinestomSchedulerAdapter();
        this.classPathAppender = new MinestomClassPathAppender(this);
        this.playerProvider = new LPMinestomPlayerProvider(this.plugin);
    }

    @Override
    public void preInitialize() {
        this.startupTime = Instant.now();
        MinecraftServer.getConnectionManager().setPlayerProvider(this.playerProvider);
        try {
            this.plugin.load();
            this.plugin.getDependencyManager().loadDependencies(
                    EnumSet.of(Dependency.CONFIGURATE_CORE, Dependency.CONFIGURATE_YAML));
        } finally {
            this.loadLatch.countDown();
        }
    }

    @Override
    public void initialize() {
        try {
            this.plugin.enable();
        } finally {
            this.enableLatch.countDown();
        }
    }

    @Override
    public void terminate() {
        this.plugin.disable();
    }

    @Override
    public PluginLogger getPluginLogger() { return logger; }

    @Override
    // Temporary
    public SchedulerAdapter getScheduler() { return schedulerAdapter; }

    @Override
    public ClassPathAppender getClassPathAppender() {
        return classPathAppender;
    }

    @Override
    public CountDownLatch getLoadLatch() { return this.loadLatch; }

    @Override
    public CountDownLatch getEnableLatch() { return this.enableLatch; }

    @Override
    public String getVersion() {
        return this.getOrigin().getVersion();
    }

    @Override
    public Instant getStartupTime() {
        return startupTime;
    }

    @Override
    public Platform.Type getType() {
        return Platform.Type.MINESTOM;
    }

    @Override
    public String getServerBrand() {
        return MinecraftServer.getBrandName();
    }

    @Override
    public String getServerVersion() {
        return MinecraftServer.VERSION_NAME;
    }

    @Override
    public @NotNull Path getDataDirectory() {
        return new File("./luckperms").toPath();
    }

    @Override
    public InputStream getResourceStream(String path) {
        return getClass().getClassLoader().getResourceAsStream(path);
    }

    @Override
    public Optional<Player> getPlayer(UUID uniqueId) {
        return Optional.ofNullable(MinecraftServer.getConnectionManager().getPlayer(uniqueId));
    }

    @Override
    public Optional<UUID> lookupUniqueId(String username) {
        Player player = MinecraftServer.getConnectionManager().findPlayer(username);

        if (player == null) return Optional.empty();

        return Optional.of(player.getUuid());
    }

    @Override
    public Optional<String> lookupUsername(UUID uniqueId) {
        Player player = MinecraftServer.getConnectionManager().getPlayer(uniqueId);

        if (player == null) return Optional.empty();

        return Optional.of(player.getUsername());
    }

    @Override
    public int getPlayerCount() {
        return MinecraftServer.getConnectionManager().getOnlinePlayers().size();
    }

    @Override
    public Collection<String> getPlayerList() {
        ArrayList<String> playerNames = new ArrayList<>();
        MinecraftServer.getConnectionManager().getOnlinePlayers().forEach(player -> playerNames.add(player.getUsername()));
        return playerNames;
    }

    @Override
    public Collection<UUID> getOnlinePlayers() {
        ArrayList<UUID> playerIds = new ArrayList<>();
        MinecraftServer.getConnectionManager().getOnlinePlayers().forEach(player -> playerIds.add(player.getUuid()));
        return playerIds;
    }

    @Override
    public boolean isPlayerOnline(UUID uniqueId) {
        return MinecraftServer.getConnectionManager().getPlayer(uniqueId) != null;
    }
}
