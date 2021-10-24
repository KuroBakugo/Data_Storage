package com.geekbrains.io;

import java.net.ServerSocket;
import java.net.Socket;

//@Slf4j
public class Server {
    public static void main(String[] args) {

        CreateFolderService service = new CreateFolderService();
        service.createServerDir("root");

        try(ServerSocket server = new ServerSocket(8189)){
//            log.debug("Server started...");

            while (true){
                Socket socket = server.accept();
//                log.debug("Client accepted...");
                Handler handler = new Handler(socket);
                new Thread(handler).start();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
