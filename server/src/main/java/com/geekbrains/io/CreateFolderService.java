package com.geekbrains.io;

import java.io.File;

public class CreateFolderService {
    private static final String APP_NAME = "F:\\Учеба\\Liberty IDEA\\Data Warehouse Writing Course\\Data_Storage\\server";

    public void createServerDir(String dirName){
        File dir = new File(APP_NAME + dirName);
        if(!dir.exists()){
            dir.mkdir();
        }
    }
}
