package net.minecraft.server.v1_12_R1;

import com.google.common.collect.Lists;
import com.google.common.io.Files;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListenableFutureTask;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.base64.Base64;
import io.netty.util.ResourceLeakDetector;
import java.awt.GraphicsEnvironment;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.Proxy;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import javax.annotation.Nullable;
import javax.imageio.ImageIO;

import net.minecraft.server.v1_12_R1.ServerPing.ServerPingPlayerSample;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.World.Environment;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.RemoteConsoleCommandSender;
import org.bukkit.craftbukkit.Main;
import org.bukkit.craftbukkit.libs.jline.console.ConsoleReader;
import org.bukkit.craftbukkit.libs.joptsimple.OptionSet;
import org.bukkit.craftbukkit.v1_12_R1.CraftServer;
import org.bukkit.craftbukkit.v1_12_R1.SpigotTimings;
import org.bukkit.craftbukkit.v1_12_R1.chunkio.ChunkIOExecutor;
import org.bukkit.craftbukkit.v1_12_R1.scoreboard.CraftScoreboardManager;
import org.bukkit.craftbukkit.v1_12_R1.util.ServerShutdownThread;
import org.bukkit.event.world.WorldInitEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.PluginLoadOrder;
import org.spigotmc.CustomTimingsHandler;
import org.spigotmc.SlackActivityAccountant;
import org.spigotmc.WatchdogThread;

public abstract class MinecraftServer implements ICommandListener, Runnable, IAsyncTaskHandler, IMojangStatistics {

    public static final Logger LOGGER = LogManager.getLogger();
    public static final File a = new File("usercache.json");
    public Convertable convertable;
    private final MojangStatisticsGenerator m = new MojangStatisticsGenerator("server", this, aw());
    public File universe;
    private final List<ITickable> o = Lists.newArrayList();
    public final ICommandHandler b;
    public final MethodProfiler methodProfiler = new MethodProfiler();
    private ServerConnection p; // Spigot
    private final ServerPing q = new ServerPing();
    private final Random r = new Random();
    public final DataConverterManager dataConverterManager;
    private String serverIp;
    private int u = -1;
    public WorldServer[] worldServer;
    private PlayerList v;
    private boolean isRunning = true;
    private boolean isStopped;
    private int ticks;
    protected final Proxy e;
    public String f;
    public int g;
    private boolean onlineMode;
    private boolean A;
    private boolean spawnAnimals;
    private boolean spawnNPCs;
    private boolean pvpMode;
    private boolean allowFlight;
    private String motd;
    private int G;
    private int H;
    public final long[] h = new long[100];
    public long[][] i;
    private KeyPair I;
    private String J;
    private String K;
    private boolean demoMode;
    private boolean N;
    private String O = "";
    private String P = "";
    private boolean Q;
    private long R;
    private String S;
    private boolean T;
    private boolean U;
    private final YggdrasilAuthenticationService V;
    private final MinecraftSessionService W;
    private final GameProfileRepository X;
    private final UserCache Y;
    private long Z;
    protected final Queue<FutureTask<?>> j = new java.util.concurrent.ConcurrentLinkedQueue<FutureTask<?>>(); // Spigot, PAIL: Rename
    private Thread serverThread;
    private long ab = aw();

    //CraftBukkitstart
    public List<WorldServer> worlds = new ArrayList();
    public CraftServer server;
    public OptionSet options;
    public ConsoleCommandSender console;
    public RemoteConsoleCommandSender remoteConsole;
    public ConsoleReader reader;
    public static int currentTick = (int) (System.currentTimeMillis() / 50);
    public final Thread primaryThread;
    public java.util.Queue<Runnable> processQueue = new java.util.concurrent.ConcurrentLinkedQueue<Runnable>();
    public int autosavePeriod;
    // CraftBukkit end
    // Spigot start
    public static final int TPS = 20;
    public static final int TICK_TIME = 1000000000 / TPS;
    private static final int SAMPLE_INTERVAL = 100;
    public final double[] recentTps = new double[3];
    public final SlackActivityAccountant slackActivityAccountant = new SlackActivityAccountant();
    // Spigot end

    public MinecraftServer(OptionSet options, Proxy proxy, DataConverterManager dataconvertermanager, YggdrasilAuthenticationService yggdrasilauthenticationservice, MinecraftSessionService minecraftsessionservice, GameProfileRepository gameprofilerepository, UserCache usercache) {
        ResourceLeakDetector.setEnabled(false);// Spigot - disable
        this.e = proxy;
        this.V = yggdrasilauthenticationservice;
        this.W = minecraftsessionservice;
        this.X = gameprofilerepository;
        this.Y = usercache;
        // this.universe = file; // CraftBukkit
        // this.p = new ServerConnection(this); // Spigot
        this.b = this.i();
        // this.convertable = new WorldLoaderServer(file); // CraftBukkit - moved to DedicatedServer.init
        this.dataConverterManager = dataconvertermanager;
        // CraftBukkit start
        this.options = options;
        // Try to see if we're actually running in a terminal, disable jline if not
        if (System.console() == null && System.getProperty("org.bukkit.craftbukkit.libs.jline.terminal") == null) {
            System.setProperty("org.bukkit.craftbukkit.libs.jline.terminal", "org.bukkit.craftbukkit.libs.jline.UnsupportedTerminal");
            Main.useJline = false;
        }

        try {
            this.reader = new ConsoleReader(System.in, System.out);
            this.reader.setExpandEvents(false);// Avoid parsing exceptions for uncommonly used event designators
        } catch (Throwable e) {
            try {
                System.setProperty("org.bukkit.craftbukkit.libs.jline.terminal", "org.bukkit.craftbukkit.libs.jline.UnsupportedTerminal");
                System.setProperty("user.language", "ru"); // en
                Main.useJline = false;
                this.reader = new ConsoleReader(System.in, System.out);
                this.reader.setExpandEvents(false);
            } catch (IOException ex) {
                LOGGER.warn((String)null, ex);
            }
        }

        Runtime.getRuntime().addShutdownHook(new ServerShutdownThread(this));
        this.serverThread = this.primaryThread = new Thread(this, "Server thread"); // Moved from main
    }

    public abstract PropertyManager getPropertyManager();
    // CraftBukkit end

    protected CommandDispatcher i() {
        return new CommandDispatcher(this);
    }

    public abstract boolean init() throws IOException;

    protected void a(String s) {
        if (this.getConvertable().isConvertable(s)) {
            LOGGER.info("Converting map!");
            this.b("menu.convertingLevel");
            this.getConvertable().convert(s, new IProgressUpdate() {
                private long b = System.currentTimeMillis();

                public void a(String s) {
                }

                public void a(int i) {
                    if (System.currentTimeMillis() - this.b >= 1000L) {
                        this.b = System.currentTimeMillis();
                        MinecraftServer.LOGGER.info("Converting... {}%", i);
                    }

                }

                public void c(String s) { }
            });
        }

    }

    protected synchronized void b(String s) {
        this.S = s;
    }

    public void a(String s, String s1, long i, WorldType worldtype, String s2) {
        this.a(s);
        this.b("menu.loadingLevel");
        this.worldServer = new WorldServer[3];
        /* CraftBukkit start - Remove ticktime arrays and worldsettings
        this.i = new long[this.worldServer.length][100];
        IDataManager idatamanager = this.convertable.a(s, true);

        this.a(this.S(), idatamanager);
        WorldData worlddata = idatamanager.getWorldData();
        WorldSettings worldsettings;

        if (worlddata == null) {
            if (this.V()) {
                worldsettings = DemoWorldServer.a;
            } else {
                worldsettings = new WorldSettings(i, this.getGamemode(), this.getGenerateStructures(), this.isHardcore(), worldtype);
                worldsettings.setGeneratorSettings(s2);
                if (this.N) {
                    worldsettings.a();
                }
            }

            worlddata = new WorldData(worldsettings, s1);
        } else {
            worlddata.a(s1);
            worldsettings = new WorldSettings(worlddata);
        }
        */
        int worldCount = 3;

        for(int j = 0; j < worldCount; ++j) {
            WorldServer world;
            byte dimension = 0;
            if (j == 1) {
                if (!this.getAllowNether()) {
                    continue;
                }

                dimension = -1;
            }

            if (j == 2) {
                if (!this.server.getAllowEnd()) {
                    continue;
                }

                dimension = 1;
            }

            String worldType = Environment.getEnvironment(dimension).toString().toLowerCase();
            String name = dimension == 0 ? s : s + "_" + worldType;
            ChunkGenerator gen = this.server.getGenerator(name);
            WorldSettings worldsettings = new WorldSettings(i, this.getGamemode(), this.getGenerateStructures(), this.isHardcore(), worldtype);
            worldsettings.setGeneratorSettings(s2);
            if (j == 0) {
                IDataManager idatamanager = new ServerNBTManager(this.server.getWorldContainer(), s1, true, this.dataConverterManager);
                WorldData worlddata = idatamanager.getWorldData();
                if (worlddata == null) {
                    worlddata = new WorldData(worldsettings, s1);
                }

                worlddata.checkName(s1);// CraftBukkit - Migration did not rewrite the level.dat; This forces 1.8 to take the last loaded world as respawn (in this case the end)
                if (this.V()) {
                    world = (WorldServer)(new DemoWorldServer(this, idatamanager, worlddata, dimension, this.methodProfiler)).b();
                } else {
                    world = (WorldServer)(new WorldServer(this, idatamanager, worlddata, dimension, this.methodProfiler, Environment.getEnvironment(dimension), gen)).b();
                }

                world.a(worldsettings);
                this.server.scoreboardManager = new CraftScoreboardManager(this, world.getScoreboard());
            } else {
                String dim = "DIM" + dimension;

                File newWorld = new File(new File(name), dim);
                File oldWorld = new File(new File(s), dim);

                if ((!newWorld.isDirectory()) && (oldWorld.isDirectory())) {
                    LOGGER.info("---- Migration of old " + worldType + " folder required ----");
                    LOGGER.info("Unfortunately due to the way that Minecraft implemented multiworld support in 1.6, Bukkit requires that you move your " + worldType + " folder to a new location in order to operate correctly.");
                    LOGGER.info("We will move this folder for you, but it will mean that you need to move it back should you wish to stop using Bukkit in the future.");
                    LOGGER.info("Attempting to move " + oldWorld + " to " + newWorld + "...");

                    if (newWorld.exists()) {
                        LOGGER.warn("A file or folder already exists at " + newWorld + "!");
                        LOGGER.info("---- Migration of old " + worldType + " folder failed ----");
                    } else if (newWorld.getParentFile().mkdirs()) {
                        if (oldWorld.renameTo(newWorld)) {
                            LOGGER.info("Success! To restore " + worldType + " in the future, simply move " + newWorld + " to " + oldWorld);
                            // Migrate world data too.
                            try {
                                Files.copy(new File(new File(s), "level.dat"), new File(new File(name), "level.dat"));
                                FileUtils.copyDirectory(new File(new File(s), "data"), new File(new File(name), "data"));
                            } catch (IOException exception) {
                                LOGGER.warn("Unable to migrate world data.");
                            }
                            LOGGER.info("---- Migration of old " + worldType + " folder complete ----");
                        } else {
                            LOGGER.warn("Could not move folder " + oldWorld + " to " + newWorld + "!");
                            LOGGER.info("---- Migration of old " + worldType + " folder failed ----");
                        }
                    } else {
                        LOGGER.warn("Could not create path for " + newWorld + "!");
                        LOGGER.info("---- Migration of old " + worldType + " folder failed ----");
                    }
                }

                IDataManager idatamanager = new ServerNBTManager(this.server.getWorldContainer(), name, true, this.dataConverterManager);
                // world =, b0 to dimension, s1 to name, added Environment and gen
                WorldData worlddata = idatamanager.getWorldData();
                if (worlddata == null) {
                    worlddata = new WorldData(worldsettings, name);
                }

                worlddata.checkName(name); // CraftBukkit - Migration did not rewrite the level.dat; This forces 1.8 to take the last loaded world as respawn (in this case the end)
                world = (WorldServer) new SecondaryWorldServer(this, idatamanager, dimension, this.worlds.get(0), this.methodProfiler, worlddata, Environment.getEnvironment(dimension), gen).b();
            }

            this.server.getPluginManager().callEvent(new WorldInitEvent(world.getWorld()));
            world.addIWorldAccess(new WorldManager(this, world));
            if (!this.R()) {
                world.getWorldData().setGameType(this.getGamemode());
            }

            worlds.add(world);
            getPlayerList().setPlayerFileData(worlds.toArray(new WorldServer[worlds.size()]));
        }
        // CraftBukkit end
        this.v.setPlayerFileData(this.worldServer);
        this.a(this.getDifficulty());
        this.l();
    }

    protected void l() {
        int i = 0;
        this.b("menu.generatingTerrain");

        // CraftBukkit start - fire WorldLoadEvent and handle whether or not to keep the spawn in memory
        for(int m = 0; m < worlds.size(); ++m) {
            WorldServer worldserver = this.worlds.get(m);
            LOGGER.info("Подготовка начального региона к уровню " + m + " (Seed мира: " + worldserver.getSeed() + ")");

            if (!worldserver.getWorld().getKeepSpawnInMemory()) {
                continue;
            }

            BlockPosition blockposition = worldserver.getSpawn();
            long j = aw();
            i = 0;

            for (int k = -192; k <= 192 && this.isRunning(); k += 16) {
                for (int l = -192; l <= 192 && this.isRunning(); l += 16) {
                    long i1 = aw();

                    if (i1 - j > 1000L) {
                        this.a_("Preparing spawn area", i * 100 / 625);
                        j = i1;
                    }

                    ++i;
                    worldserver.getChunkProviderServer().getChunkAt(blockposition.getX() + k >> 4, blockposition.getZ() + l >> 4);
                }
            }
        }
        for (WorldServer world : this.worlds) this.server.getPluginManager().callEvent(new WorldLoadEvent(world.getWorld()));
        this.t();
    }

    protected void a(String s, IDataManager idatamanager) {
        File file = new File(idatamanager.getDirectory(), "resources.zip");

        if (file.isFile()) {
            try {
                this.setResourcePack("level://" + URLEncoder.encode(s, StandardCharsets.UTF_8.toString()) + "/" + "resources.zip", "");
            } catch (UnsupportedEncodingException unsupportedencodingexception) {
                LOGGER.warn("Something went wrong url encoding {}", s);
            }
        }

    }

    public abstract boolean getGenerateStructures();

    public abstract EnumGamemode getGamemode();

    public abstract EnumDifficulty getDifficulty();

    public abstract boolean isHardcore();

    public abstract int q();

    public abstract boolean r();

    public abstract boolean s();

    protected void a_(String s, int i) {
        this.f = s;
        this.g = i;
        LOGGER.info("{}: {}%", s, i);
    }

    protected void t() {
        this.f = null;
        this.g = 0;
        this.server.enablePlugins(PluginLoadOrder.POSTWORLD); // CraftBukkit
    }

    protected void saveChunks(boolean flag) {
        WorldServer[] aworldserver = this.worldServer;
        int i = aworldserver.length; // usage???

        // CraftBukkit start
        for(int j = 0; j < this.worlds.size(); ++j) {
            WorldServer worldserver = this.worlds.get(j);
            // CraftBukkit end

            if (worldserver != null) {
                if (!flag) LOGGER.info("Сохранение чанка для уровня \'{}\'/{}", worldserver.getWorldData().getName(), worldserver.worldProvider.getDimensionManager().b());

                try {
                    worldserver.save(true, (IProgressUpdate) null);
                    worldserver.saveLevel(); // CraftBukkit
                } catch (ExceptionWorldConflict ex) {
                    LOGGER.warn(ex.getMessage());
                }
            }
        }

    }

    // CraftBukkit start
    private boolean hasStopped = false;
    private final Object stopLock = new Object();
    // CraftBukkit end

    public void stop() throws ExceptionWorldConflict { // CraftBukkit - added throws
        // CraftBukkit start - prevent double stopping on multiple threads
        synchronized(stopLock) {
            if (hasStopped) return;
            hasStopped = true;
        }
        // CraftBukkit end
        net.minecraft.server.v1_12_R1.MinecraftServer.LOGGER.info("Остановка сервера... Остановка плагинов...");
        // CraftBukkit start
        if (this.server != null) {
            this.server.disablePlugins();
        }
        // CraftBukkit end
        if (this.an() != null) {
            this.an().b();
        }

        if (this.v != null) {
            net.minecraft.server.v1_12_R1.MinecraftServer.LOGGER.info("Сохранение данных игроков...");
            this.v.savePlayers();
            this.v.u();
            try { Thread.sleep(100); } catch (InterruptedException ex) {} // CraftBukkit - SPIGOT-625 - give server at least a chance to send packets
        }

        if (this.worldServer != null) {
            net.minecraft.server.v1_12_R1.MinecraftServer.LOGGER.info("Сохранение миров...");
            WorldServer[] aworldserver = this.worldServer;
            int i = aworldserver.length;

            int j;
            WorldServer worldserver;

            for (j = 0; j < i; ++j) {
                worldserver = aworldserver[j];
                if (worldserver != null) {
                    worldserver.savingDisabled = false;
                }
            }

            this.saveChunks(false);
            aworldserver = this.worldServer;
            i = aworldserver.length;

            /* CraftBukkit start - Handled in saveChunks
            for (j = 0; j < i; ++j) {
                worldserver = aworldserver[j];
                if (worldserver != null) {
                    worldserver.saveLevel();
                }
            }
            // CraftBukkit end */
        }

        if (this.m.d()) {
            this.m.e();
        }

        // Spigot start
        if (org.spigotmc.SpigotConfig.saveUserCacheOnStopOnly) {
            LOGGER.info("Сохранение файла usercache.json");
            this.Y.c();
        }
        // Spigot end
    }

    public String getServerIp() {
        return this.serverIp;
    }

    public void c(String s) {
        this.serverIp = s;
    }

    public boolean isRunning() {
        return this.isRunning;
    }

    public void safeShutdown() {
        this.isRunning = false;
    }

    // Spigot Start
    private static double calcTps(double avg, double exp, double tps)
    {
        return ( avg * exp ) + ( tps * ( 1 - exp ) );
    }
    // Spigot End

    public void run() {
        try {
            if (this.init()) {
                this.ab = aw();
                long i = 0L;

                this.q.setMOTD(new ChatComponentText(this.motd));
                this.q.setServerInfo(new ServerPing.ServerData("1.12.2", 340));
                this.a(this.q);

                // Spigot start
                Arrays.fill( recentTps, 20 );
                long lastTick = System.nanoTime(), catchupTime = 0, curTime, wait, tickSection = lastTick;
                while (this.isRunning) {
                    curTime = System.nanoTime();
                    wait = TICK_TIME - (curTime - lastTick) - catchupTime;
                    if (wait > 0) {
                        Thread.sleep(wait / 1000000);
                        catchupTime = 0;
                        continue;
                    } else {
                        catchupTime = Math.min(1000000000, Math.abs(wait));
                    }

                    if ( net.minecraft.server.v1_12_R1.MinecraftServer.currentTick++ % SAMPLE_INTERVAL == 0 )
                    {
                        double currentTps = 1E9 / ( curTime - tickSection ) * SAMPLE_INTERVAL;
                        recentTps[0] = calcTps( recentTps[0], 0.92, currentTps ); // 1/exp(5sec/1min)
                        recentTps[1] = calcTps( recentTps[1], 0.9835, currentTps ); // 1/exp(5sec/5min)
                        recentTps[2] = calcTps( recentTps[2], 0.9945, currentTps ); // 1/exp(5sec/15min)
                        tickSection = curTime;
                    }
                    lastTick = curTime;

                    this.C();
                    this.Q = true;
                }
                // Spigot end
            } else {
                this.a((CrashReport) null);
            }
        } catch (Throwable e) {
            LOGGER.error("Encountered an unexpected exception", e);
            // Spigot Start
            if (e.getCause() != null) {
                LOGGER.error("\tCause of unexpected exception was", e.getCause());
            }
            // Spigot End
            CrashReport crashreport = null;

            if (e instanceof ReportedException) {
                crashreport = this.b(((ReportedException)e).a());
            } else {
                crashreport = this.b(new CrashReport("Exception in server tick loop", e));
            }

            File file = new File(new File(this.A(), "crash-reports"), "crash-" + (new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss")).format(new Date()) + "-server.txt");

            if (crashreport.a(file)) {
                LOGGER.error("This crash report has been saved to: {}", file.getAbsolutePath());
            } else {
                LOGGER.error("We were unable to save this crash report to disk.");
            }

            this.a(crashreport);
        } finally {
            try {
                WatchdogThread.doStop();
                this.isStopped = true;
                this.stop();
            } catch (Throwable e) {
                LOGGER.error("Exception stopping the server", e);
            } finally {
                // CraftBukkit start - Restore terminal to original settings
                try {
                    reader.getTerminal().restore();
                } catch (Exception ignored) {}
                // CraftBukkit end
                this.B();
            }

        }

    }

    public void a(ServerPing serverping) {
        File file = this.d("server-icon.png");

        if (!file.exists()) {
            file = this.getConvertable().b(this.S(), "icon.png");
        }

        if (file.isFile()) {
            ByteBuf bytebuf = Unpooled.buffer();

            try {
                BufferedImage bufferedimage = ImageIO.read(file);
                Validate.validState(bufferedimage.getWidth() == 64, "Must be 64 pixels wide", new Object[0]); // don't remove new Object[0]
                Validate.validState(bufferedimage.getHeight() == 64, "Must be 64 pixels high", new Object[0]); // don't remove new Object[0]
                ImageIO.write(bufferedimage, "PNG", new ByteBufOutputStream(bytebuf));
                ByteBuf bytebuf1 = Base64.encode(bytebuf);

                serverping.setFavicon("data:image/png;base64," + bytebuf1.toString(StandardCharsets.UTF_8));
            } catch (Exception ex) {
                LOGGER.error("Couldn\'t load server icon", ex);
            } finally {
                bytebuf.release();
            }
        }

    }

    public File A() {
        return new File(".");
    }

    protected void a(CrashReport crashreport) {}

    public void B() {}

    protected void C() throws ExceptionWorldConflict { // CraftBukkit - added throws
        SpigotTimings.serverTickTimer.startTiming(); // Spigot
        this.slackActivityAccountant.tickStarted(); // Spigot
        long i = System.nanoTime();

        ++this.ticks;
        if (this.T) {
            this.T = false;
            this.methodProfiler.a = true;
            this.methodProfiler.a();
        }

        this.methodProfiler.a("root");
        this.D();
        if (i - this.Z >= 5000000000L) {
            this.Z = i;
            this.q.setPlayerSample(new ServerPingPlayerSample(this.I(), this.H()));
            GameProfile[] agameprofile = new GameProfile[Math.min(this.H(), 12)];
            int j = MathHelper.nextInt(this.r, 0, this.H() - agameprofile.length);

            for(int k = 0; k < agameprofile.length; ++k) {
                agameprofile[k] = ((EntityPlayer) this.v.v().get(j + k)).getProfile();
            }

            Collections.shuffle(Arrays.asList(agameprofile));
            this.q.b().a(agameprofile);
        }

        if (this.autosavePeriod > 0 && this.ticks % this.autosavePeriod == 0) {// CraftBukkit
            SpigotTimings.worldSaveTimer.startTiming(); // Spigot
            this.methodProfiler.a("save");
            this.v.savePlayers();
            // Spigot Start
            // We replace this with saving each individual world as this.saveChunks(...) is broken,
            // and causes the main thread to sleep for random amounts of time depending on chunk activity
            // Also pass flag to only save modified chunks
            this.server.playerCommandState = true;
            for (World world : this.worlds) world.getWorld().save(false);
            this.server.playerCommandState = false;
            // this.saveChunks(true);
            // Spigot End
            this.methodProfiler.b();
            SpigotTimings.worldSaveTimer.stopTiming();
        }

        this.methodProfiler.a("tallying");
        // Spigot start
        long tickNanos;
        this.h[this.ticks % 100] = tickNanos = System.nanoTime() - i;
        // Spigot end
        this.methodProfiler.b();
        this.methodProfiler.a("snooper");
        if (this.getSnooperEnabled() && !this.m.d() && this.ticks > 100) {  // Spigot
            this.m.a();
        }

        if (this.getSnooperEnabled() && this.ticks % 6000 == 0) { // Spigot
            this.m.b();
        }

        this.methodProfiler.b();
        this.methodProfiler.b();
        WatchdogThread.tick(); // Spigot
        this.slackActivityAccountant.tickEnded(tickNanos); // Spigot
        SpigotTimings.serverTickTimer.stopTiming(); // Spigot
        CustomTimingsHandler.tick(); // Spigot
    }

    public void D() {
        SpigotTimings.schedulerTimer.startTiming(); // Spigot
        this.server.getScheduler().mainThreadHeartbeat(this.ticks); // CraftBukkit
        SpigotTimings.schedulerTimer.stopTiming(); // Spigot
        this.methodProfiler.a("jobs");
        Queue queue = this.j; //usage???

        // Spigot start
        FutureTask<?> entry;
        int count = this.j.size();
        while (count-- > 0 && (entry = this.j.poll()) != null) {
            SystemUtils.a(entry, LOGGER);
        }
        // Spigot end

        this.methodProfiler.c("levels");

        // CraftBukkit start
        // Run tasks that are waiting on processing
        SpigotTimings.processQueueTimer.startTiming(); // Spigot
        while(!processQueue.isEmpty()) {
            (processQueue.remove()).run();
        }
        SpigotTimings.processQueueTimer.stopTiming(); // Spigot

        SpigotTimings.chunkIOTickTimer.startTiming(); // Spigot
        ChunkIOExecutor.tick();
        SpigotTimings.chunkIOTickTimer.stopTiming(); // Spigot

        SpigotTimings.timeUpdateTimer.startTiming(); // Spigot
        // Send time updates to everyone, it will get the right time from the world the player is in.
        int i;
        if (this.ticks % 20 == 0) {
            for(i = 0; i < this.getPlayerList().players.size(); ++i) {
                EntityPlayer entityplayer = (EntityPlayer) this.getPlayerList().players.get(i);
                entityplayer.playerConnection.sendPacket(new PacketPlayOutUpdateTime(entityplayer.world.getTime(), entityplayer.getPlayerTime(), entityplayer.world.getGameRules().getBoolean("doDaylightCycle"))); // Add support for per player time
            }
        }
        SpigotTimings.timeUpdateTimer.stopTiming(); // Spigot

        for(i = 0; i < this.worlds.size(); ++i) { // CraftBukkit
            long j = System.nanoTime(); // usage ???

            // if (i == 0 || this.getAllowNether()) {
            WorldServer worldserver = this.worlds.get(i);

            this.methodProfiler.a(() -> {
                return worldserver.getWorldData().getName();
            });
                /* Drop global time updates
                if (this.ticks % 20 == 0) {
                    this.methodProfiler.a("timeSync");
                    this.v.a((Packet) (new PacketPlayOutUpdateTime(worldserver.getTime(), worldserver.getDayTime(), worldserver.getGameRules().getBoolean("doDaylightCycle"))), worldserver.worldProvider.getDimensionManager().getDimensionID());
                    this.methodProfiler.b();
                }
                // CraftBukkit end */

            this.methodProfiler.a("tick");

            CrashReport crashreport;
            try {
                worldserver.timings.doTick.startTiming(); // Spigot
                worldserver.doTick();
                worldserver.timings.doTick.stopTiming(); // Spigot
            } catch (Throwable ex) {
                // Spigot Start
                try {
                    crashreport = CrashReport.a(ex, "Exception ticking world");
                } catch (Throwable t) {
                    throw new RuntimeException("Error generating crash report", t);
                }
                // Spigot End
                worldserver.a(crashreport);
                throw new ReportedException(crashreport);
            }

            try {
                worldserver.timings.tickEntities.startTiming(); // Spigot
                worldserver.tickEntities();
                worldserver.timings.tickEntities.stopTiming(); // Spigot
            } catch (Throwable ex) {
                // Spigot Start
                try {
                    crashreport = CrashReport.a(ex, "Exception ticking world entities");
                } catch (Throwable t) {
                    throw new RuntimeException("Error generating crash report", t);
                }
                // Spigot End
                worldserver.a(crashreport);
                throw new ReportedException(crashreport);
            }

            this.methodProfiler.b();
            this.methodProfiler.a("tracker");
            worldserver.timings.tracker.startTiming(); // Spigot
            worldserver.getTracker().updatePlayers();
            worldserver.timings.tracker.stopTiming(); // Spigot
            this.methodProfiler.b();
            this.methodProfiler.b();
            // } // CraftBukkit

            // this.i[i][this.ticks % 100] = System.nanoTime() - j; // CraftBukkit
        }

        this.methodProfiler.c("connection");
        SpigotTimings.connectionTimer.startTiming(); // Spigot
        this.an().c();
        SpigotTimings.connectionTimer.stopTiming(); // Spigot
        this.methodProfiler.c("players");
        SpigotTimings.playerListTimer.startTiming(); // Spigot
        this.v.tick();
        SpigotTimings.playerListTimer.stopTiming(); // Spigot
        this.methodProfiler.c("commandFunctions");
        SpigotTimings.commandFunctionsTimer.startTiming(); // Spigot
        this.aL().e();
        SpigotTimings.commandFunctionsTimer.stopTiming();// Spigot
        this.methodProfiler.c("tickables");

        SpigotTimings.tickablesTimer.startTiming(); // Spigot
        for (i = 0; i < this.o.size(); ++i) {
            ((ITickable) this.o.get(i)).e();
        }
        SpigotTimings.tickablesTimer.stopTiming(); // Spigot

        this.methodProfiler.b();
    }

    public boolean getAllowNether() {
        return false;
    }

    public void a(ITickable itickable) {
        this.o.add(itickable);
    }

    public static void main(OptionSet options) { // CraftBukkit - replaces main(String[] astring)
        DispenserRegistry.c();

        try {
            /* CraftBukkit start - Replace everything
            boolean flag = true;
            String s = null;
            String s1 = ".";
            String s2 = null;
            boolean flag1 = false;
            boolean flag2 = false;
            int i = -1;

            for (int j = 0; j < astring.length; ++j) {
                String s3 = astring[j];
                String s4 = j == astring.length - 1 ? null : astring[j + 1];
                boolean flag3 = false;

                if (!"nogui".equals(s3) && !"--nogui".equals(s3)) {
                    if ("--port".equals(s3) && s4 != null) {
                        flag3 = true;

                        try {
                            i = Integer.parseInt(s4);
                        } catch (NumberFormatException numberformatexception) {
                            ;
                        }
                    } else if ("--singleplayer".equals(s3) && s4 != null) {
                        flag3 = true;
                        s = s4;
                    } else if ("--universe".equals(s3) && s4 != null) {
                        flag3 = true;
                        s1 = s4;
                    } else if ("--world".equals(s3) && s4 != null) {
                        flag3 = true;
                        s2 = s4;
                    } else if ("--demo".equals(s3)) {
                        flag1 = true;
                    } else if ("--bonusChest".equals(s3)) {
                        flag2 = true;
                    }
                } else {
                    flag = false;
                }

                if (flag3) {
                    ++j;
                }
            }
            */ // CraftBukkit end

            String s1 = "."; // PAIL?
            YggdrasilAuthenticationService yggdrasilauthenticationservice = new YggdrasilAuthenticationService(Proxy.NO_PROXY, UUID.randomUUID().toString());
            MinecraftSessionService minecraftsessionservice = yggdrasilauthenticationservice.createMinecraftSessionService();
            GameProfileRepository gameprofilerepository = yggdrasilauthenticationservice.createProfileRepository();
            UserCache usercache = new UserCache(gameprofilerepository, new File(s1, a.getName()));
            final DedicatedServer dedicatedserver = new DedicatedServer(options, DataConverterRegistry.a(), yggdrasilauthenticationservice, minecraftsessionservice, gameprofilerepository, usercache);

            /* CraftBukkit start
            if (s != null) {
                dedicatedserver.i(s);
            }

            if (s2 != null) {
                dedicatedserver.setWorld(s2);
            }

            if (i >= 0) {
                dedicatedserver.setPort(i);
            }

            if (flag1) {
                dedicatedserver.b(true);
            }

            if (flag2) {
                dedicatedserver.c(true);
            }

            if (flag && !GraphicsEnvironment.isHeadless()) {
                dedicatedserver.aR();
            }

            dedicatedserver.F();
            Runtime.getRuntime().addShutdownHook(new Thread("Server Shutdown Thread") {
                public void run() {
                    dedicatedserver.stop();
                }
            });
            */

            if (options.has("port")) {
                int port = (Integer)options.valueOf("port");
                if (port > 0) {
                    dedicatedserver.setPort(port);
                }
            }

            if (options.has("universe")) {
                dedicatedserver.universe = (File) options.valueOf("universe");
            }

            if (options.has("world")) {
                dedicatedserver.setWorld((String) options.valueOf("world"));
            }

            dedicatedserver.primaryThread.start();
            // CraftBukkit end
        } catch (Exception ex) {
            LOGGER.fatal("Failed to start the minecraft server", ex);
        }

    }

    public void F() {
        /* CraftBukkit start - prevent abuse
        this.serverThread = new Thread(this, "Server thread");
        this.serverThread.start();
        // CraftBukkit end */
    }

    public File d(String s) {
        return new File(this.A(), s);
    }

    public void info(String s) {
        LOGGER.info(s);
    }

    public void warning(String s) {
        LOGGER.warn(s);
    }

    public WorldServer getWorldServer(int i) {
        // CraftBukkit start
        for (WorldServer world : worlds) {
            if (world.dimension == i) {
                return world;
            }
        }
        return worlds.get(0);
        // CraftBukkit end
    }

    public String getVersion() {
        return "1.12.2";
    }

    public int H() {
        return this.v.getPlayerCount();
    }

    public int I() {
        return this.v.getMaxPlayers();
    }

    public String[] getPlayers() {
        return this.v.f();
    }

    public GameProfile[] K() {
        return this.v.g();
    }

    public boolean isDebugging() {
        return this.getPropertyManager().getBoolean("debug", false); // CraftBukkit - don't hardcode
    }

    public void g(String s) {
        LOGGER.error(s);
    }

    public void h(String s) {
        if (this.isDebugging()) {
            LOGGER.info(s);
        }

    }

    public String getServerModName() {
        return "Spigot"; // Spigot - Spigot > // CraftBukkit - cb > vanilla!
    }

    public CrashReport b(CrashReport crashreport) {
        crashreport.g().a("Profiler Position", new CrashReportCallable() {
            public String a() throws Exception {
                return MinecraftServer.this.methodProfiler.a ? MinecraftServer.this.methodProfiler.c() : "N/A (disabled)";
            }

            public Object call() throws Exception {
                return this.a();
            }
        });
        if (this.v != null) {
            crashreport.g().a("Player Count", new CrashReportCallable() {
                public String a() {
                    return MinecraftServer.this.v.getPlayerCount() + " / " + MinecraftServer.this.v.getMaxPlayers() + "; " + MinecraftServer.this.v.v();
                }

                public Object call() throws Exception {
                    return this.a();
                }
            });
        }

        return crashreport;
    }

    public List<String> tabCompleteCommand(ICommandListener icommandlistener, String s, @Nullable BlockPosition blockposition, boolean flag) {
        /* CraftBukkit start - Allow tab-completion of Bukkit commands
        ArrayList arraylist = Lists.newArrayList();
        boolean flag1 = s.startsWith("/");

        if (flag1) {
            s = s.substring(1);
        }

        if (!flag1 && !flag) {
            String[] astring = s.split(" ", -1);
            String s1 = astring[astring.length - 1];
            String[] astring1 = this.v.f();
            int i = astring1.length;

            for (int j = 0; j < i; ++j) {
                String s2 = astring1[j];

                if (CommandAbstract.a(s1, s2)) {
                    arraylist.add(s2);
                }
            }

            return arraylist;
        } else {
            boolean flag2 = !s.contains(" ");
            List list = this.b.a(icommandlistener, s, blockposition);

            if (!list.isEmpty()) {
                Iterator iterator = list.iterator();

                while (iterator.hasNext()) {
                    String s3 = (String) iterator.next();

                    if (flag2 && !flag) {
                        arraylist.add("/" + s3);
                    } else {
                        arraylist.add(s3);
                    }
                }
            }

            return arraylist;
        }
        */
        return this.server.tabComplete(icommandlistener, s, blockposition, flag);
        // CraftBukkit end
    }

    public boolean M() {
        return true; // CraftBukkit
    }

    public String getName() {
        return "Server";
    }

    public void sendMessage(IChatBaseComponent ichatbasecomponent) {
        LOGGER.info(ichatbasecomponent.toPlainText());
    }

    public boolean a(int i, String s) {
        return true;
    }

    public ICommandHandler getCommandHandler() {
        return this.b;
    }

    public KeyPair O() {
        return this.I;
    }

    public int P() {
        return this.u;
    }

    public void setPort(int i) {
        this.u = i;
    }

    public String Q() {
        return this.J;
    }

    public void i(String s) {
        this.J = s;
    }

    public boolean R() {
        return this.J != null;
    }

    public String S() {
        return this.K;
    }

    public void setWorld(String s) {
        this.K = s;
    }

    public void a(KeyPair keypair) {
        this.I = keypair;
    }

    public void a(EnumDifficulty enumdifficulty) {
        // CraftBukkit start
        // WorldServer[] aworldserver = this.worldServer;
        int i = this.worlds.size();

        for(int j = 0; j < i; ++j) {
            WorldServer worldserver = this.worlds.get(j);
            // CraftBukkit end

            if (worldserver != null) {
                if (worldserver.getWorldData().isHardcore()) {
                    worldserver.getWorldData().setDifficulty(EnumDifficulty.HARD);
                    worldserver.setSpawnFlags(true, true);
                } else if (this.R()) {
                    worldserver.getWorldData().setDifficulty(enumdifficulty);
                    worldserver.setSpawnFlags(worldserver.getDifficulty() != EnumDifficulty.PEACEFUL, true);
                } else {
                    worldserver.getWorldData().setDifficulty(enumdifficulty);
                    worldserver.setSpawnFlags(this.getSpawnMonsters(), this.spawnAnimals);
                }
            }
        }

    }

    public boolean getSpawnMonsters() {
        return true;
    }

    public boolean V() {
        return this.demoMode;
    }

    public void b(boolean flag) {
        this.demoMode = flag;
    }

    public void c(boolean flag) {
        this.N = flag;
    }

    public Convertable getConvertable() {
        return this.convertable;
    }

    public String getResourcePack() {
        return this.O;
    }

    public String getResourcePackHash() {
        return this.P;
    }

    public void setResourcePack(String s, String s1) {
        this.O = s;
        this.P = s1;
    }

    public void a(MojangStatisticsGenerator mojangstatisticsgenerator) {
        mojangstatisticsgenerator.a("whitelist_enabled", false);
        mojangstatisticsgenerator.a("whitelist_count", 0);
        if (this.v != null) {
            mojangstatisticsgenerator.a("players_current", this.H());
            mojangstatisticsgenerator.a("players_max", this.I());
            mojangstatisticsgenerator.a("players_seen", this.v.getSeenPlayers().length);
        }

        mojangstatisticsgenerator.a("uses_auth", this.onlineMode);
        mojangstatisticsgenerator.a("gui_state", this.ap() ? "enabled" : "disabled");
        mojangstatisticsgenerator.a("run_time", (aw() - mojangstatisticsgenerator.g()) / 60L * 1000L);
        mojangstatisticsgenerator.a("avg_tick_ms", (int)(MathHelper.a(this.h) * 1.0E-6D));
        int i = 0;

        if (this.worldServer != null) {
            // CraftBukkit start
            for(int j = 0; j < this.worlds.size(); ++j) {
                WorldServer worldserver = this.worlds.get(j);
                if (worldserver != null) {
                    // CraftBukkit end
                    WorldData worlddata = worldserver.getWorldData();

                    mojangstatisticsgenerator.a("world[" + i + "][dimension]", worldserver.worldProvider.getDimensionManager().getDimensionID());
                    mojangstatisticsgenerator.a("world[" + i + "][mode]", worlddata.getGameType());
                    mojangstatisticsgenerator.a("world[" + i + "][difficulty]", worldserver.getDifficulty());
                    mojangstatisticsgenerator.a("world[" + i + "][hardcore]", worlddata.isHardcore());
                    mojangstatisticsgenerator.a("world[" + i + "][generator_name]", worlddata.getType().name());
                    mojangstatisticsgenerator.a("world[" + i + "][generator_version]", worlddata.getType().getVersion());
                    mojangstatisticsgenerator.a("world[" + i + "][height]", this.G);
                    mojangstatisticsgenerator.a("world[" + i + "][chunks_loaded]", worldserver.getChunkProviderServer().g());
                    ++i;
                }
            }
        }

        mojangstatisticsgenerator.a("worlds", i);
    }

    public void b(MojangStatisticsGenerator mojangstatisticsgenerator) {
        mojangstatisticsgenerator.b("singleplayer", this.R());
        mojangstatisticsgenerator.b("server_brand", this.getServerModName());
        mojangstatisticsgenerator.b("gui_supported", GraphicsEnvironment.isHeadless() ? "headless" : "supported");
        mojangstatisticsgenerator.b("dedicated", this.aa());
    }

    public boolean getSnooperEnabled() {
        return true;
    }

    public abstract boolean aa();

    public boolean getOnlineMode() {
        return this.server.getOnlineMode(); // CraftBukkit
    }

    public void setOnlineMode(boolean flag) {
        this.onlineMode = flag;
    }

    public boolean ac() {
        return this.A;
    }

    public void e(boolean flag) {
        this.A = flag;
    }

    public boolean getSpawnAnimals() {
        return this.spawnAnimals;
    }

    public void setSpawnAnimals(boolean flag) {
        this.spawnAnimals = flag;
    }

    public boolean getSpawnNPCs() {
        return this.spawnNPCs;
    }

    public abstract boolean af();

    public void setSpawnNPCs(boolean flag) {
        this.spawnNPCs = flag;
    }

    public boolean getPVP() {
        return this.pvpMode;
    }

    public void setPVP(boolean flag) {
        this.pvpMode = flag;
    }

    public boolean getAllowFlight() {
        return this.allowFlight;
    }

    public void setAllowFlight(boolean flag) {
        this.allowFlight = flag;
    }

    public abstract boolean getEnableCommandBlock();

    public String getMotd() {
        return this.motd;
    }

    public void setMotd(String s) {
        this.motd = s;
    }

    public int getMaxBuildHeight() {
        return this.G;
    }

    public void c(int i) {
        this.G = i;
    }

    public boolean isStopped() {
        return this.isStopped;
    }

    public PlayerList getPlayerList() {
        return this.v;
    }

    public void a(PlayerList playerlist) {
        this.v = playerlist;
    }

    public void setGamemode(EnumGamemode enumgamemode) {
        // CraftBukkit start
        for (int i = 0; i < this.worlds.size(); ++i) {
            worlds.get(i).getWorldData().setGameType(enumgamemode);
        }

    }

    // Spigot Start
    public ServerConnection getServerConnection() {
        return this.p;
    }
    // Spigot End
    public ServerConnection an() {
        return this.p == null ? this.p = new ServerConnection(this) : this.p; // Spigot
    }

    public boolean ap() {
        return false;
    }

    public abstract String a(EnumGamemode enumgamemode, boolean flag);

    public int aq() {
        return this.ticks;
    }

    public void ar() {
        this.T = true;
    }

    public World getWorld() {
        return this.worlds.get(0); // CraftBukkit
    }

    public int getSpawnProtection() {
        return 16;
    }

    public boolean a(World world, BlockPosition blockposition, EntityHuman entityhuman) {
        return false;
    }

    public void setForceGamemode(boolean flag) {
        this.U = flag;
    }

    public boolean getForceGamemode() {
        return this.U;
    }

    public Proxy av() {
        return this.e;
    }

    public static long aw() {
        return System.currentTimeMillis();
    }

    public int getIdleTimeout() {
        return this.H;
    }

    public void setIdleTimeout(int i) {
        this.H = i;
    }

    public MinecraftSessionService az() {
        return this.W;
    }

    public GameProfileRepository getGameProfileRepository() {
        return this.X;
    }

    public UserCache getUserCache() {
        return this.Y;
    }

    public ServerPing getServerPing() {
        return this.q;
    }

    public void aD() {
        this.Z = 0L;
    }

    @Nullable
    public Entity a(UUID uuid) {
        WorldServer[] aworldserver = this.worldServer;
        int i = aworldserver.length;

        // CraftBukkit start
        for (int j = 0; j < worlds.size(); ++j) {
            WorldServer worldserver = worlds.get(j);
            // CraftBukkit end

            if (worldserver != null) {
                Entity entity = worldserver.getEntity(uuid);

                if (entity != null) {
                    return entity;
                }
            }
        }

        return null;
    }

    public boolean getSendCommandFeedback() {
        return worlds.get(0).getGameRules().getBoolean("sendCommandFeedback");
    }

    public MinecraftServer C_() {
        return this;
    }

    public int aE() {
        return 29999984;
    }

    public <V> ListenableFuture<V> a(Callable<V> callable) {
        Validate.notNull(callable);
        if (!this.isMainThread()) { // CraftBukkit && !this.isStopped()) {
            ListenableFutureTask listenablefuturetask = ListenableFutureTask.create(callable);
            Queue queue = this.j;

            // Spigot start
            this.j.add(listenablefuturetask);
            return listenablefuturetask;
            // Spigot end
        } else {
            try {
                return Futures.immediateFuture(callable.call());
            } catch (Exception exception) {
                return Futures.immediateFailedCheckedFuture(exception);
            }
        }
    }

    public ListenableFuture<Object> postToMainThread(Runnable runnable) {
        Validate.notNull(runnable);
        return this.a(Executors.callable(runnable));
    }

    public boolean isMainThread() {
        return Thread.currentThread() == this.serverThread;
    }

    public int aG() {
        return 256;
    }

    public long aH() {
        return this.ab;
    }

    public Thread aI() {
        return this.serverThread;
    }

    public int a(@Nullable WorldServer worldserver) {
        return worldserver != null ? worldserver.getGameRules().c("spawnRadius") : 10;
    }

    public AdvancementDataWorld getAdvancementData() {
        return this.worlds.get(0).z(); // CraftBukkit
    }

    public CustomFunctionData aL() {
        return this.worlds.get(0).A(); // CraftBukkit
    }

    public void reload() {
        if (this.isMainThread()) {
            this.getPlayerList().savePlayers();
            this.worlds.get(0).getLootTableRegistry().reload(); // CraftBukkit
            this.getAdvancementData().reload();
            this.aL().f();
            this.getPlayerList().reload();
        } else {
            this.postToMainThread(this::reload);
        }

    }

    // CraftBukkit start
    /** @deprecated */
    @Deprecated
    public static MinecraftServer getServer() {
        return Bukkit.getServer() instanceof CraftServer ? ((CraftServer) Bukkit.getServer()).getServer() : null;
    }
    // CraftBukkit end
}
