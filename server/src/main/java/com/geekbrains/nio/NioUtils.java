package com.geekbrains.nio;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.List;

import static java.nio.file.StandardWatchEventKinds.*;

@Slf4j
public class NioUtils {
    @SneakyThrows
    public static void main(String[] args) {
        Path path = Paths.get("server", "root");
        System.out.println(Files.exists(path));
        System.out.println(Files.size(path));

        Path copy = Paths.get("server", "root", "new.txt");
//        System.out.println(path.resolve(copy));
        System.out.println(path.getParent().resolve("copy.txt"));

        System.out.println(path.toAbsolutePath());


//        Контроль изменений в папке и запись в log
        WatchService watchService = FileSystems.getDefault()
                .newWatchService();

        new Thread(()-> {
            while (true){
                WatchKey key;
                try {
                    key = watchService.take();
                    if(key.isValid()){
                        List<WatchEvent<?>> event = key.pollEvents();
                        for(WatchEvent<?> event1 : event){
                            log.debug("kind{}, context{}", event1.kind(), event1.context());
                        }
                        key.reset();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        path.register(watchService, ENTRY_MODIFY, ENTRY_CREATE, ENTRY_DELETE);

        // Дописать в файле сообщение
        Files.write(copy, "My message".getBytes(StandardCharsets.UTF_8),
        //Сохраняет все что было написано в файле и позволяет дописывать в конце
                StandardOpenOption.APPEND
        );

        //Копия файла в новый
        Files.copy(copy,Paths.get("server", "root", "f1.txt"),
                // позволяет копировать файл с существующим названием f1.txt
                StandardCopyOption.REPLACE_EXISTING
        );

        //Вывод всего что находится в папке
        Files.walk(path).forEach(System.out::println);
    }
}
