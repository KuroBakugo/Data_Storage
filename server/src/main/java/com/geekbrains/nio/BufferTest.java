package com.geekbrains.nio;

import java.nio.ByteBuffer;

public class BufferTest {

    public static void main(String[] args) {

        ByteBuffer buffer = ByteBuffer.allocate(5);
        buffer.put((byte) 'a');
        buffer.put((byte) 'b');
        buffer.put((byte) 'c');
        //возвращает только заполненые ячейки массива
        buffer.flip();
        while ((buffer.hasRemaining())){
            System.out.println((char) buffer.get());
        }
        //возвращает чтение с 0 позиции
        buffer.rewind();
        while ((buffer.hasRemaining())){
            System.out.println((char) buffer.get());
        }

        buffer.clear();
    }
}
