package org.rustlib.rustboard;

import static org.rustlib.rustboard.MessageActions.MESSAGE_ACTION_KEY;
import static org.rustlib.utils.FileUtils.clearDir;
import static org.rustlib.utils.FileUtils.externalStorage;

import com.qualcomm.robotcore.util.RobotLog;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.rustlib.core.RobotControllerActivity;
import org.rustlib.rustboard.Rustboard.RustboardException;
import org.rustlib.utils.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;

public class RustboardServer extends WebSocketServer {
    public static final int port = 5801;
    static final File RUSTBOARD_STORAGE_DIR = new File(externalStorage, "Rustboard");
    private static final File RUSTBOARD_METADATA_FILE = new File(RUSTBOARD_STORAGE_DIR, "rustboard_metadata.json");
    static final File OLD_STORED_RUSTBOARD_DIR = new File(RUSTBOARD_STORAGE_DIR, "rustboards_previous");
    static final File NEW_STORED_RUSTBOARD_DIR = new File(RUSTBOARD_STORAGE_DIR, "rustboards_latest");
    private static RustboardServer instance = null;
    private static boolean debugMode = false;
    private Rustboard activeRustboard;
    private final JsonObject rustboardMetaData;
    private final HashSet<String> storedRustboardIds = new HashSet<>();
    private final HashMap<String, Rustboard> loadedRustboards = new HashMap<>();
    private static final int rustboardAutoSavePeriod = 10000;
    private static final int pingClientPeriod = 4000;
    private static final String pingClientMessage = "{\"action\": \"ping\"}";
    private static final ClientUpdater clientUpdater = new ClientUpdater();
    private final ScheduledExecutorService executorService = new ScheduledThreadPoolExecutor(3);
    private final Set<WebSocket> connections = Collections.synchronizedSet(new HashSet<>());

    ClientUpdater getClientUpdater() {
        return clientUpdater;
    }

    private void autoSave() {
        if (!RobotControllerActivity.opModeRunning()) {
            saveLayouts();
            executorService.schedule(this::autoSave, rustboardAutoSavePeriod, TimeUnit.MILLISECONDS);
        }
    }

    public Rustboard getActiveRustboard() {
        if (activeRustboard == null) {
            activeRustboard = Rustboard.emptyRustboard(null);
        }
        return activeRustboard;
    }

    private RustboardServer(int port) throws UnknownHostException {
        super(new InetSocketAddress(port));
        setReuseAddr(true);
        FileUtils.makeDirIfMissing(RUSTBOARD_STORAGE_DIR);
        JsonObject defaultMetaData = Json.createObjectBuilder().add("rustboards", Json.createArrayBuilder().build()).build();
        rustboardMetaData = FileUtils.safeLoadJsonObject(RUSTBOARD_METADATA_FILE, defaultMetaData);
        if (rustboardMetaData.containsKey("rustboards")) {
            JsonArray dataArray = rustboardMetaData.getJsonArray("rustboards");
            for (JsonValue value : dataArray) {
                JsonObject data = (JsonObject) value;
                String uuid = data.getString("uuid");
                storedRustboardIds.add(uuid);
                if (data.getBoolean("active")) {
                    try {
                        activeRustboard = Rustboard.load(uuid);
                    } catch (Rustboard.NoSuchRustboardException e) {
                        log(e);
                    }
                }
            }
        }
        autoSave();
        RobotControllerActivity.onOpModeStop(this::autoSave);
        executorService.scheduleAtFixedRate(clientUpdater, 0, 50, TimeUnit.MILLISECONDS);
        executorService.scheduleAtFixedRate(() -> {
            connections.forEach((connection) -> {
                if (connection.isOpen())
                    try {
                        connection.send(pingClientMessage);
                    } catch (RuntimeException e) {
                        log(e);
                    }
            });
        }, pingClientPeriod, pingClientPeriod, TimeUnit.MILLISECONDS);
    }

    static void messageActiveRustboard(JsonObject json) {
        WebSocket connection = getInstance().activeRustboard.getConnection();
        if (connection != null && connection.isOpen())
            connection.send(json.toString());
    }

    public static void enableDebugMode() { // TODO: make a good way to enable and disable debug mode
        debugMode = false;
    }

    public static void disableDebugMode() {
        debugMode = true;
    }

    public static boolean inDebugMode() {
        return debugMode;
    }

    public static void log(Object value) { // TODO: create simpler logger

    }

    private static void clearStorage() throws IOException {
        clearDir(RUSTBOARD_STORAGE_DIR, true);
    }

    public static RustboardServer getInstance() {
        if (instance == null) {
            try {
                instance = new RustboardServer(port);
                RobotLog.v("dashboard server started");
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
        }
        return instance;
    }

    @Override
    public void start() {
        if (!debugMode) {
            try {
                super.start();
            } catch (IllegalStateException e) {
                log(e);
            }
        }
    }

    @Override
    public void stop() {
        saveLayouts();
        try {
            super.stop();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onStart() {
        setConnectionLostTimeout(3);
    }

    public static boolean isActiveRustboard() {
        return getInstance().activeRustboard != null && getInstance().activeRustboard.isConnected();
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        connections.add(conn);
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        connections.remove(conn);
        if (activeRustboard.getConnection() == conn) {
            activeRustboard.onDisconnect();
        }
        for (Rustboard rustboard : loadedRustboards.values()) {
            if (rustboard.isConnected()) {
                activeRustboard = rustboard;
                break;
            }
        }
        log("client " + conn.getRemoteSocketAddress().toString() + " disconnected from the robot.");
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public void onMessage(WebSocket conn, String message) {
        JsonObject messageJson = FileUtils.readJsonString(message);
        try {
            switch (messageJson.getString(MESSAGE_ACTION_KEY)) {
                case MessageActions.CLIENT_DETAILS:
                    Time.calibrateUTCTime(messageJson.getJsonNumber("utc_time").longValue());
                    String uuid = messageJson.getString("uuid");
                    JsonArray clientNodes = messageJson.getJsonArray(RustboardNode.NODE_ARRAY_KEY);
                    Rustboard rustboard;
                    if (loadedRustboards.containsKey(uuid)) {
                        rustboard = loadedRustboards.get(uuid).mergeWithClientRustboard(clientNodes);
                    } else if (storedRustboardIds.contains(uuid)) {
                        try {
                            rustboard = Rustboard.load(uuid).mergeWithClientRustboard(clientNodes);
                        } catch (Rustboard.NoSuchRustboardException e) {
                            rustboard = new Rustboard(uuid, clientNodes);
                        }
                    } else {
                        rustboard = new Rustboard(uuid, clientNodes);
                    }
                    rustboard.setConnection(conn);
                    loadedRustboards.put(uuid, rustboard);
                    if (isActiveRustboard() && !rustboard.getUuid().equals(activeRustboard.getUuid())) {
                        rustboard.notifyClient("Rustboard queued because another Rustboard is connected", NoticeType.NEUTRAL, 5000);
                    } else {
                        activeRustboard = rustboard;
                        JsonObjectBuilder builder = Json.createObjectBuilder();
                        builder.add("action", "set_active");
                        rustboard.getConnection().send(builder.build().toString());
                    }

                    break;
                case MessageActions.EXCEPTION:
                    throw new RustboardException(messageJson.getString("exception_message"));
                default:
                    if (activeRustboard != null) {
                        activeRustboard.onMessage(messageJson);
                    }
            }
        } catch (Exception e) {
            Rustboard.notifyAllClients("Robot received an invalid websocket message");
            warnClientConsoles(e);
            log(e);
            throw new RuntimeException(e.getMessage() + "\n" + message); // TODO: remove after debugging
        }
    }

    public void saveLayouts() {
        FileUtils.makeDirIfMissing(OLD_STORED_RUSTBOARD_DIR);
        FileUtils.makeDirIfMissing(NEW_STORED_RUSTBOARD_DIR);
        JsonObjectBuilder metadataBuilder = Json.createObjectBuilder(rustboardMetaData);
        JsonArrayBuilder rustboardArrayBuilder = Json.createArrayBuilder(rustboardMetaData.getJsonArray("rustboards"));
        metadataBuilder.add("rustboards", rustboardArrayBuilder);
        loadedRustboards.forEach((uuid, rustboard) -> {
                    JsonObjectBuilder rustboardDescriptor = Json.createObjectBuilder();
                    rustboardDescriptor.add("uuid", uuid);
                    rustboardDescriptor.add("active", rustboard == activeRustboard);
                    rustboardArrayBuilder.add(rustboardDescriptor);
                    rustboard.save(new File(OLD_STORED_RUSTBOARD_DIR, uuid + ".json"));
                }
        );
        try {
            FileUtils.writeJson(RUSTBOARD_METADATA_FILE, metadataBuilder.build());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onError(WebSocket conn, Exception e) {
        log(e);
    }

    public Set<WebSocket> getConnectedSockets() {
        return connections;
    }

    public void threadSafeBroadcast(String message) {
        for (WebSocket connection : connections) {
            connection.send(message);
        }
    }

    public static void logToClientConsoles(String info) {
        JsonObjectBuilder builder = Json.createObjectBuilder();
        builder.add("action", MessageActions.CONSOLE_LOG);
        builder.add("info", info);
        getInstance().threadSafeBroadcast(builder.build().toString());
    }

    public static void warnClientConsoles(String info) {
        JsonObjectBuilder builder = Json.createObjectBuilder();
        builder.add("action", MessageActions.CONSOLE_WARN);
        builder.add("info", info);
        getInstance().threadSafeBroadcast(builder.build().toString());
    }

    public static void warnClientConsoles(Exception e) {
        warnClientConsoles(e.getMessage());
    }
}