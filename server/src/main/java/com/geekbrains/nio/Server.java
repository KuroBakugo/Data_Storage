package com.geekbrains.nio;

import lombok.extern.slf4j.Slf4j;
import org.omg.PortableInterceptor.ServerRequestInfo;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
public class Server {

    private ServerSocketChannel serverSocketChannel;
    private Selector selector;
    private ByteBuffer buffer;
    private static Path ROOT = Paths.get("server", "root");

    public Server() throws IOException {
        buffer = ByteBuffer.allocate(256);
        serverSocketChannel = ServerSocketChannel.open();
        selector = Selector.open();
        serverSocketChannel.bind((new InetSocketAddress(8189)));
        log.debug("Server Started");
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

        while ((serverSocketChannel.isOpen())) {
            selector.select();

            Set<SelectionKey> selectionKeys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = selectionKeys.iterator();
            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();
                if (key.isAcceptable()) {
                    handleAccept(key);
                }
                if (key.isReadable()) {
                    handleRead(key);
                }
                iterator.remove();
            }
        }

    }

    private void handleRead(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        buffer.clear();
        int read = 0;
        StringBuilder msg = new StringBuilder();
        while (true) {
            if (read == -1) {
                channel.close();
                return;
            }
            read = channel.read(buffer);

            if (read == 0) {
                break;
            }
            buffer.flip();
            while (buffer.hasRemaining()) {
                msg.append((char) buffer.get());
            }
            buffer.clear();

        }
        String massage = msg.toString().trim();
        log.debug("received: {}", massage);
        if(massage.equals("ls")){
            channel.write(ByteBuffer.wrap(getFilesInfo().getBytes(StandardCharsets.UTF_8)));
        }else if(massage.startsWith("cat")){
            String fileName = massage.split(" ")[1];
            channel.write(ByteBuffer.wrap(getFileDataString(fileName).getBytes(StandardCharsets.UTF_8)));
        }else {
            channel.write(ByteBuffer.wrap(("Wrong  " + massage).getBytes(StandardCharsets.UTF_8)));
        }
    }

    private void handleAccept(SelectionKey key) throws IOException {
        SocketChannel channel = serverSocketChannel.accept();
        channel.configureBlocking(false);
        channel.register(selector, SelectionKey.OP_READ);
        log.debug("Client accepted");
    }

    private String getFileDataString(String fileName) throws IOException {
        if(Files.isDirectory(ROOT.resolve(fileName))){
            return "[ERROR] Command Cat cannot be applied " + fileName + "\n";
        }else{
            return new String(Files.readAllBytes(ROOT.resolve(fileName)) + "\n");
        }
    }

    private String getFilesInfo() throws IOException {
        return Files.list(ROOT)
                .map(this::resolveFileType)
                .collect(Collectors.joining("\n")) + "\n";
    }

    private String resolveFileType(Path path){
        if(Files.isDirectory(path)){
            return String.format("%s\t%s" + path.getFileName().toString(), "[DIR]");
        }else {
            return String.format("%s\t%s" + path.getFileName().toString(), "[File]");
        }
    }

    public static void main(String[] args) throws IOException {
        new Server();
    }
}
