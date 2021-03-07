package priv.theo.service;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import priv.theo.subtitle.dto.ShooterSubtitleFileDTO;
import priv.theo.subtitle.dto.VideoSubtitleDTO;
import priv.theo.subtitle.dto.ShooterSubtitleDTO;
import priv.theo.utils.SystemUtils;

import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * Shooter subtitle api client
 */
@Slf4j
@Component
public class ShooterSubtitleService {

    private static final String FILE_HASH = "filehash";
    private static final String PATH_INFO = "pathinfo";
    private static final String FORMAT = "format";
    private static final String LANG = "lang";
    private static final String FORMAT_JSON = "json";
    private static final String LANG_CHN = "chn";
    private static final String EQUAL_SIGN = "=";
    private static final int SUCCESS_CODE = 200;

    @Value("${shooter.serverUrl}")
    private String serviceUrl;

    private static PoolingHttpClientConnectionManager poolingHttpClientConnectionManager;

    @Autowired
    private SystemUtils systemUtils;

    public ShooterSubtitleService() {
        initPool();
    }

    public static void initPool() {

        poolingHttpClientConnectionManager = new PoolingHttpClientConnectionManager(60000, TimeUnit.SECONDS);
        poolingHttpClientConnectionManager.setMaxTotal(1000);
        poolingHttpClientConnectionManager.setDefaultMaxPerRoute(100);
    }

    private HttpClient getHttpClient() {
        return HttpClients.createMinimal(poolingHttpClientConnectionManager);
    }

    private RequestConfig getDefaultRequestConfig() {
        return RequestConfig.custom()
                .setConnectTimeout(35000).setConnectionRequestTimeout(35000).setSocketTimeout(60000).build();
    }

    @Async
    public void downloadFirstSubtitle(VideoSubtitleDTO videoSubtitleDTO) {
        if (Objects.isNull(videoSubtitleDTO)
                || Objects.isNull(videoSubtitleDTO.getShooterSubtitleDTOs())
                || Objects.isNull(videoSubtitleDTO.getShooterSubtitleDTOs()[0].getFiles())
                || Objects.isNull(videoSubtitleDTO.getShooterSubtitleDTOs()[0].getFiles()[0])) {
            log.info("downloadSubtitle(): Do not download subtitle. sub info is null!");
            return;
        }
        log.info("downloadFirstSubtitle(): download subtitle for video {}", videoSubtitleDTO.getFilePath());
        ShooterSubtitleFileDTO[] shooterSubtitleFileDTOS = videoSubtitleDTO.getShooterSubtitleDTOs()[0].getFiles();
        try {
            downloadSubtitle(shooterSubtitleFileDTOS[0].getLink(), generateStoragePath(videoSubtitleDTO.getFilePath()));
        } catch (IOException e) {
            log.warn("download failed. video:{}", videoSubtitleDTO.getFilePath());
            e.printStackTrace();
        }
    }

    private String generateStoragePath(String filePath) {
        String osName = System.getProperty("os.name");
        String delimiter = osName.toLowerCase().startsWith("window") ? "\\" : "/";
        return filePath.substring(0, filePath.lastIndexOf(delimiter));
    }

    public boolean downloadSubtitle(String url, String storagePath) throws IOException {
        log.info("downloadSubtitle(): url={}, storagePath={}", url, storagePath);
        boolean result = false;
        if (Objects.isNull(url) || url.isEmpty()) {
            log.warn("downloadSubtitle(): parameter is null");
        }
        HttpClient httpClient = getHttpClient();
        HttpGet httpGet = new HttpGet(url);
        httpGet.setConfig(getDefaultRequestConfig());
        HttpResponse response = httpClient.execute(httpGet);
        if (SUCCESS_CODE == response.getStatusLine().getStatusCode() && Objects.nonNull(response.getEntity())) {

            // Content-Disposition: attachment; filename=Young.Sheldon.S01E02.720p.HDTV.X264-DIMENSION.ass
            // todo validate npe
            String filename = response.getFirstHeader("Content-Disposition").getElements()[0].getParameterByName("filename").getValue();

            filename = filename.contains(systemUtils.getSystemDelimiter()) ? filename.substring(filename.lastIndexOf(systemUtils.getSystemDelimiter()) + 1) : filename;
            File storageFile = new File(storagePath, filename);
            response.getEntity().writeTo(new BufferedOutputStream(new FileOutputStream(storageFile)));
            log.info("downloadSubtitle(): storage file {}", storageFile.getAbsolutePath());
            result = true;
        }
        return result;

    }

    public ShooterSubtitleDTO[] searchSubtitle(VideoSubtitleDTO videoSubtitleDTO) throws IOException {

        log.info("searchSubtitle(): search subtitle for video {}", videoSubtitleDTO.getFilePath());
        HttpClient httpClient = getHttpClient();


        HttpPost httpPost = new HttpPost(generateUrl(videoSubtitleDTO));
        httpPost.setConfig(getDefaultRequestConfig());


        HttpResponse response = httpClient.execute(httpPost);

        String result = EntityUtils.toString(response.getEntity());

        // 回收
        EntityUtils.consume(response.getEntity());
        if (Objects.isNull(result) || result.isEmpty() || !result.startsWith("[")) {
            log.warn("sendRequest(): response error!");
            return null;
        }
        Gson gson = new Gson();
        ShooterSubtitleDTO[] subInfos = gson.fromJson(result, ShooterSubtitleDTO[].class);
        log.info("sendRequest(): response:{}", subInfos);
        return subInfos;
    }



    private String generateUrl(VideoSubtitleDTO videoSubtitleDTO) throws IOException {
        StringBuilder stringBuilder;
        stringBuilder = new StringBuilder(serviceUrl);
        stringBuilder.append("?").append(FILE_HASH).append(EQUAL_SIGN).append(videoSubtitleDTO.getFileHash());
        stringBuilder.append("&").append(FORMAT).append(EQUAL_SIGN).append(FORMAT_JSON);
        stringBuilder.append("&").append(PATH_INFO).append(EQUAL_SIGN).append(URLEncoder.encode(videoSubtitleDTO.getFilePath(), StandardCharsets.UTF_8.name()));
        stringBuilder.append("&").append(LANG).append(EQUAL_SIGN).append(LANG_CHN);

        return stringBuilder.toString();
    }

    /**
     * compute the file hash
     * refer https://docs.google.com/document/d/1ufdzy6jbornkXxsD-OGl3kgWa4P9WO5NZb6_QYZiGI0/preview
     *
     * @param file File
     * @return the special hash
     * @throws IOException
     */
    public String computeFileHash(File file) throws IOException {

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
