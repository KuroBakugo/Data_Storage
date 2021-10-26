package com.geekbrains.io;

import lombok.extern.slf4j.Slf4j;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;

@Slf4j
public class Handler implements Runnable {

    private static final int BUFFER_SIZE = 256;
    private static final byte[] buffer = new byte[BUFFER_SIZE];
    private static final String ROOT_DIR = "server/root";
    private final Socket socket;

    public Handler(Socket socket) {
        this.socket = socket;
    }

    public Socket getSocket() {
        return socket;
    }

    @Override
    public void run() {
        try (DataOutputStream os = new DataOutputStream(socket.getOutputStream());
             DataInputStream is = new DataInputStream(socket.getInputStream())) {

            while (true) {
                String s = is.readUTF();
                log.debug("Received: {}", s);
                long size = is.readLong();
                int read;
                try (OutputStream fos = Files.newOutputStream(Paths.get(ROOT_DIR, s))) {
                    for (int i = 0; i < (size + BUFFER_SIZE - 1) / BUFFER_SIZE; i++) {
                        read = is.read(buffer);
                        fos.write(buffer, 0, read);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                os.writeUTF("ok");
            }
        } catch (Exception e) {
            log.error("stacktrace: ", e);
        }
    }
}
