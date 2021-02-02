package priv.theo.service;

import com.google.gson.Gson;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import priv.theo.subtitle.dto.ShooterSubtitleFileDTO;
import priv.theo.subtitle.dto.VideoSubtitleDTO;
import priv.theo.subtitle.dto.ShooterSubtitleDTO;

import java.io.*;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * Shooter subtitle api client
 */
public class ShooterSubtitleService {
    private static final Logger log = LoggerFactory.getLogger(ShooterSubtitleService.class);

    private static final String SERVICE_URL = "https://www.shooter.cn/api/subapi.php";
    private static final String FILE_HASH = "filehash";
    private static final String PATH_INFO = "pathinfo";
    private static final String FORMAT = "format";
    private static final String LANG = "lang";
    private static final String FORMAT_JSON = "json";
    private static final String LANG_CHN = "chn";
    private static final String EQUAL_SIGN = "=";
    private static final int SUCCESS_CODE = 200;

    private static PoolingHttpClientConnectionManager poolingHttpClientConnectionManager;

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

    public void downloadFirstSubtitle(VideoSubtitleDTO videoSubtitleDTO) throws IOException {
        if (Objects.isNull(videoSubtitleDTO)
                || Objects.isNull(videoSubtitleDTO.getShooterSubtitleDTOs())
                || Objects.isNull(videoSubtitleDTO.getShooterSubtitleDTOs()[0].getFiles())
                || Objects.isNull(videoSubtitleDTO.getShooterSubtitleDTOs()[0].getFiles()[0])) {
            log.info("downloadSubtitle(): Do not download subtitle. sub info is null!");
            return;
        }
        log.info("downloadFirstSubtitle(): download subtitle for video {}", videoSubtitleDTO.getFilePath());
        ShooterSubtitleFileDTO[] shooterSubtitleFileDTOS = videoSubtitleDTO.getShooterSubtitleDTOs()[0].getFiles();
        downloadSubtitle(shooterSubtitleFileDTOS[0].getLink(), generateStoragePath(videoSubtitleDTO.getFilePath()));
    }

    private String generateStoragePath(String filePath) {
        return filePath.substring(0, filePath.lastIndexOf("/"));
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
        stringBuilder = new StringBuilder(SERVICE_URL);
        stringBuilder.append("?").append(FILE_HASH).append(EQUAL_SIGN).append(videoSubtitleDTO.getFileHash());
        stringBuilder.append("&").append(FORMAT).append(EQUAL_SIGN).append(FORMAT_JSON);
        stringBuilder.append("&").append(PATH_INFO).append(EQUAL_SIGN).append(videoSubtitleDTO.getFilePath());
        stringBuilder.append("&").append(LANG).append(EQUAL_SIGN).append(LANG_CHN);

        return stringBuilder.toString();
    }



}
