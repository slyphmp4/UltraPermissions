package me.techscode.compatibility;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/** A minimal Minecraft 26.3 client for the isolated test container's loopback interface. */
public final class OfflineProbeClient {
    private static final String HOST = "127.0.0.1";
    private static final int PORT = 25565;
    private static final int MAX_PACKET = 8 * 1024 * 1024;
    private static final Pattern PROTOCOL = Pattern.compile("\"protocol\"\\s*:\\s*(\\d+)");
    public static final String FIRST_PLAYER = "FoliaOwnerA";
    public static final String SECOND_PLAYER = "FoliaOwnerB";

    public static void main(String[] args) throws Exception {
        int protocol = waitForServer();
        var workers = Executors.newFixedThreadPool(2);
        try {
            var first = workers.submit(() -> { play(protocol, FIRST_PLAYER); return null; });
            var second = workers.submit(() -> { play(protocol, SECOND_PLAYER); return null; });
            first.get();
            second.get();
        } finally {
            workers.shutdownNow();
        }
    }

    private static int waitForServer() throws Exception {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(100);
        IOException last = null;
        while (System.nanoTime() < deadline) {
            try (Socket socket = connect()) {
                var in = new DataInputStream(socket.getInputStream());
                var out = new DataOutputStream(socket.getOutputStream());
                handshake(out, -1, 1);
                send(out, 0, body -> {});
                var frame = readFrame(in);
                if (readVarInt(frame) != 0) throw new IOException("Unexpected status packet");
                int size = readVarInt(frame);
                if (size < 0 || size > 65_536) throw new IOException("Invalid status size");
                var status = new String(frame.readNBytes(size), StandardCharsets.UTF_8);
                var matcher = PROTOCOL.matcher(status);
                if (matcher.find()) return Integer.parseInt(matcher.group(1));
                throw new IOException("Server did not report its protocol version");
            } catch (IOException unavailable) {
                last = unavailable;
                Thread.sleep(300);
            }
        }
        throw new IOException("The local Folia test server never became ready", last);
    }

    private static Socket connect() throws IOException {
        Socket socket = new Socket();
        try {
            socket.connect(new InetSocketAddress(HOST, PORT), 2000);
            socket.setSoTimeout(35_000);
            socket.setTcpNoDelay(true);
            return socket;
        } catch (IOException failure) {
            socket.close();
            throw failure;
        }
    }

    private static void play(int protocol, String name) throws IOException {
        try (Socket socket = connect()) {
            var in = new DataInputStream(socket.getInputStream());
            var out = new DataOutputStream(socket.getOutputStream());
            handshake(out, protocol, 2);
            UUID uuid = UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes(StandardCharsets.UTF_8));
            send(out, 0, body -> {
                writeString(body, name);
                body.writeLong(uuid.getMostSignificantBits());
                body.writeLong(uuid.getLeastSignificantBits());
            });

            int loginPacket = readVarInt(readFrame(in));
            if (loginPacket != 0x02) throw new IOException(name + " received unexpected login packet " + loginPacket);
            send(out, 0x03, body -> {});
            configure(in, out);
            System.out.println(name + " finished login and configuration");
            acknowledgePlay(in, out);
        }
    }

    private static void configure(DataInputStream in, DataOutputStream out) throws IOException {
        for (;;) {
            var frame = readFrame(in);
            switch (readVarInt(frame)) {
                case 0x02 -> throw new IOException("Disconnected during configuration");
                case 0x03 -> {
                    send(out, 0x03, body -> {});
                    return;
                }
                case 0x04 -> send(out, 0x04, body -> body.writeLong(frame.readLong()));
                case 0x05 -> send(out, 0x05, body -> body.writeInt(frame.readInt()));
                case 0x0F -> send(out, 0x07, body -> writeVarInt(body, 0));
                default -> {} // The client has no rendering or registry consumer.
            }
        }
    }

    private static void acknowledgePlay(DataInputStream in, DataOutputStream out) throws IOException {
        double x = 0, y = 0, z = 0;
        float yaw = 0, pitch = 0;
        try {
            for (;;) {
                var frame = readFrame(in);
                switch (readVarInt(frame)) {
                    case 0x20 -> { return; }
                    case 0x2D -> send(out, 0x1C, body -> body.writeLong(frame.readLong()));
                    case 0x49 -> {
                        int id = readVarInt(frame);
                        double nextX = frame.readDouble(), nextY = frame.readDouble(), nextZ = frame.readDouble();
                        frame.skipNBytes(3L * Double.BYTES); // Delta movement is irrelevant to a stationary client.
                        float nextYaw = frame.readFloat(), nextPitch = frame.readFloat();
                        int relative = frame.readInt();
                        x = nextX + ((relative & 1) == 0 ? 0 : x);
                        y = nextY + ((relative & 2) == 0 ? 0 : y);
                        z = nextZ + ((relative & 4) == 0 ? 0 : z);
                        yaw = nextYaw + ((relative & 8) == 0 ? 0 : yaw);
                        pitch = nextPitch + ((relative & 16) == 0 ? 0 : pitch);
                        double positionX = x, positionY = y, positionZ = z;
                        float rotationYaw = yaw, rotationPitch = pitch;
                        send(out, 0x00, body -> {
                            writeVarInt(body, id);
                            body.writeDouble(positionX);
                            body.writeDouble(positionY);
                            body.writeDouble(positionZ);
                            body.writeFloat(rotationYaw);
                            body.writeFloat(rotationPitch);
                        });
                    }
                    default -> {}
                }
            }
        } catch (EOFException stopped) {
            // The test server has shut down.
        }
    }

    private static void handshake(DataOutputStream out, int protocol, int intention) throws IOException {
        send(out, 0, body -> {
            writeVarInt(body, protocol);
            writeString(body, HOST);
            body.writeShort(PORT);
            writeVarInt(body, intention);
        });
    }

    private static DataInputStream readFrame(DataInputStream input) throws IOException {
        int length = readVarInt(input);
        if (length < 1 || length > MAX_PACKET) throw new IOException("Invalid packet length " + length);
        byte[] frame = input.readNBytes(length);
        if (frame.length != length) throw new EOFException("Incomplete packet frame");
        return new DataInputStream(new ByteArrayInputStream(frame));
    }

    private static void send(DataOutputStream output, int id, PacketBody payload) throws IOException {
        var bytes = new ByteArrayOutputStream();
        var frame = new DataOutputStream(bytes);
        writeVarInt(frame, id);
        payload.write(frame);
        writeVarInt(output, bytes.size());
        bytes.writeTo(output);
        output.flush();
    }

    private static void writeString(DataOutputStream out, String value) throws IOException {
        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        writeVarInt(out, bytes.length);
        out.write(bytes);
    }

    private static int readVarInt(DataInputStream in) throws IOException {
        int result = 0;
        for (int shift = 0; shift < 35; shift += 7) {
            int part = in.readUnsignedByte();
            result |= (part & 0x7F) << shift;
            if ((part & 0x80) == 0) return result;
        }
        throw new IOException("Invalid VarInt");
    }

    private static void writeVarInt(DataOutputStream out, int value) throws IOException {
        while ((value & ~0x7F) != 0) {
            out.writeByte((value & 0x7F) | 0x80);
            value >>>= 7;
        }
        out.writeByte(value);
    }

    @FunctionalInterface
    private interface PacketBody {
        void write(DataOutputStream body) throws IOException;
    }
}
