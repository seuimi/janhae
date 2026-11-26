package kr.janhae;

import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

public final class PackHost {
    public static final UUID PACK_ID = UUID.fromString("6f0c1a2e-7b34-4a1e-9c55-3e0a9b7d26a1");

    private final JanhaePlugin plugin;
    private HttpServer server;
    private byte[] sha1 = new byte[0];
    private Path zip;

    public PackHost(JanhaePlugin plugin) {
        this.plugin = plugin;
    }

    public void start() {
        if (!plugin.getConfig().getBoolean("resource-pack.enabled", true)) {
            return;
        }
        zip = plugin.getDataFolder().toPath().resolve("janhae.zip");
        try {
            Files.createDirectories(zip.getParent());
            try (InputStream input = plugin.getResource("janhae-pack.zip")) {
                if (input == null) {
                    plugin.getLogger().warning("리소스팩 zip이 플러그인 안에 없습니다.");
                    return;
                }
                Files.copy(input, zip, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            }
            sha1 = sha1(Files.readAllBytes(zip));
        } catch (IOException exception) {
            plugin.getLogger().warning("리소스팩을 꺼내지 못했습니다: " + exception.getMessage());
            return;
        }
        String bind = plugin.getConfig().getString("resource-pack.bind", "0.0.0.0");
        int port = plugin.getConfig().getInt("resource-pack.port", 8765);
        try {
            server = HttpServer.create(new InetSocketAddress(bind, port), 0);
            server.createContext("/janhae.zip", exchange -> {
                byte[] data;
                try {
                    data = Files.readAllBytes(zip);
                } catch (IOException exception) {
                    exchange.sendResponseHeaders(404, -1);
                    exchange.close();
                    return;
                }
                exchange.getResponseHeaders().set("Content-Type", "application/zip");
                exchange.sendResponseHeaders(200, data.length);
                exchange.getResponseBody().write(data);
                exchange.close();
            });
            server.start();
            plugin.getLogger().info("리소스팩 주소: http://" + bind + ":" + port + "/janhae.zip");
        } catch (IOException exception) {
            plugin.getLogger().warning("리소스팩 포트를 열지 못했습니다: " + exception.getMessage());
        }
    }

    public void stop() {
        if (server != null) {
            server.stop(0);
        }
    }

    public void offer(Player player) {
        String base = plugin.getConfig().getString("resource-pack.public-base-url", "");
        if (base == null || base.isBlank()) {
            if (player.isOp()) {
                player.sendMessage(Component.text("리소스팩 파일은 준비됐습니다. config.yml 의 resource-pack.public-base-url 에 http://서버IP:" + plugin.getConfig().getInt("resource-pack.port", 8765) + " 를 넣으면 접속할 때 받아집니다.", net.kyori.adventure.text.format.NamedTextColor.YELLOW));
            }
            return;
        }
        if (sha1.length != 20) {
            return;
        }
        String url = base.replaceAll("/+$", "") + "/janhae.zip";
        String prompt = plugin.getConfig().getString("resource-pack.prompt", "잔해 리소스팩을 받아 주세요.");
        boolean required = plugin.getConfig().getBoolean("resource-pack.required", false);
        player.setResourcePack(PACK_ID, url, sha1, Component.text(prompt), required);
    }

    private static byte[] sha1(byte[] data) {
        try {
            return MessageDigest.getInstance("SHA-1").digest(data);
        } catch (NoSuchAlgorithmException exception) {
            return new byte[0];
        }
    }
}
