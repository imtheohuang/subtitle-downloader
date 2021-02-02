package priv.theo.subtitle;

import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URLEncoder;
import java.util.Arrays;


public class ShooterUtils {
    private static final Logger log = LoggerFactory.getLogger(ShooterUtils.class);

    /**
     * compute the file hash
     * refer https://docs.google.com/document/d/1ufdzy6jbornkXxsD-OGl3kgWa4P9WO5NZb6_QYZiGI0/preview
     *
     * @param file File
     * @return the special hash
     * @throws IOException
     */
    public static String computeFileHash(File file) throws IOException {

        RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r");

        long fileLength = randomAccessFile.length();
        long[] offset = new long[4];
        if (fileLength < 8192) {
            log.warn("computeFileHash(): a video file less than 8k? impossible!");
        }
        offset[3] = fileLength - 8192;
        offset[2] = Math.floorDiv(fileLength, 3);
        offset[1] = Math.floorDiv(fileLength, 3) * 2;
        offset[0] = 4096;
        byte[] block = new byte[4096];
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            randomAccessFile.seek(offset[i]);
            randomAccessFile.read(block, 0, 4096);
            sb.append(DigestUtils.md5Hex(block)).append(";");
        }

        randomAccessFile.close();
        String substring = sb.substring(0, sb.length() - 1);
        String encode = URLEncoder.encode(substring);
        log.info("computeFileHash(): file={}, hash={}", file.getName(), encode);
        return encode;
    }
}
