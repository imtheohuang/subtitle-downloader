package priv.theo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;

public class FileReadWriteTest {
    private static final Logger log = LoggerFactory.getLogger(FileReadWriteTest.class);
    public static void testFileWrite() throws IOException {
        File file = new File("/Users/theo/Desktop/file_test");
        RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");
        byte[] bytes = new byte[8192];
        for (int i = 0; i < 8192; i++) {
            bytes[i] = (byte) i;
        }
        randomAccessFile.write(bytes);
        randomAccessFile.close();
    }

    public static void testFileRead() throws IOException {
        File file = new File("/Users/theo/Desktop/file_test");
        RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");

        int[] offset = new int[4];
        offset[0] = 3;
        offset[1] = 12;
        offset[2] = 4097;
        offset[3] = 5120;
        byte[] block = new byte[10];
//        byte[][] block = new byte[4][10];
        for (int i = 0; i < 4; i++) {
            randomAccessFile.seek(offset[i]);
            randomAccessFile.read(block, 0, 10);

            log.info(Arrays.toString(block));
        }
    }
}
