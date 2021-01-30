package priv.theo.subtitle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import priv.theo.service.ShooterSubClient;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class Client {
    private static final Logger log = LoggerFactory.getLogger(Client.class);

    private static String videoSuffixStr = "mp4,m4v,mov,mkv,avi,flv,rm,rmvb";
    private static Set<String> videoSuffix = new HashSet<String>(Arrays.asList(videoSuffixStr.split(",")));

    public static void main(String[] args) {
        String path = "/Volumes/N1/Share/Videos/TV";
        searchAndDown(path);
    }

    private static void searchAndDownloadSubtitleForOne() {
        String filePath = "/Users/theo/Desktop/Young.Sheldon.S01E02.720p.HDTV.X264-DIMENSION.mkv";
        RequestInfoDTO requestInfoDTO = new RequestInfoDTO();
        requestInfoDTO.setSourceFile(new File(filePath));
        requestInfoDTO.setFilePath(filePath);
        try {
            requestInfoDTO.setFileHash(ShooterUtils.computeFileHash(requestInfoDTO.getSourceFile()));
        } catch (IOException e) {
            log.warn("Compute file hash error. Do not send request to service to search subtitle. File path={}", filePath);
            return;
        }
        ShooterSubClient shooterSubClient = new ShooterSubClient();
        try {
            SubInfo[] subInfos = shooterSubClient.searchSubtitle(requestInfoDTO);
            requestInfoDTO.setResponseSubInfos(subInfos);

            shooterSubClient.downloadFirstSubtitle(requestInfoDTO);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void searchAndDown(String path) {
        log.info("search and down load subtitle for dir {}", path);
        List<String> videoList = new ArrayList<>();
        findAllVideo(path, videoList);

        if (videoList.isEmpty()) {
            log.warn("path {} do not has video.", path);
            return;
        }
        List<RequestInfoDTO> hasSubtitleToDownloadList = new ArrayList<>();
        ShooterSubClient shooterSubClient = new ShooterSubClient();

        log.info("video list: {}", videoList);
        for (String video : videoList) {
            RequestInfoDTO requestInfoDTO = new RequestInfoDTO();
            requestInfoDTO.setSourceFile(new File(video));
            requestInfoDTO.setFilePath(video);
            try {
                requestInfoDTO.setFileHash(ShooterUtils.computeFileHash(requestInfoDTO.getSourceFile()));
            } catch (IOException e) {
                log.warn("Compute file hash error. Do not send request to service to search subtitle. File path={}", video);
                e.printStackTrace();
                continue;
            }
            try {
                SubInfo[] subInfos;
                subInfos = shooterSubClient.searchSubtitle(requestInfoDTO);
                requestInfoDTO.setResponseSubInfos(subInfos);
                hasSubtitleToDownloadList.add(requestInfoDTO);
            } catch (IOException e) {
                log.info("Search failed, file can not download: {}", video);
                e.printStackTrace();
            }
        }

        if (hasSubtitleToDownloadList.isEmpty()) {
            log.warn("path {} do not need to download subtitle.", path);
        }

        hasSubtitleToDownloadList.forEach(requestInfoDTO -> {
            try {
                shooterSubClient.downloadFirstSubtitle(requestInfoDTO);
            } catch (IOException e) {
                log.warn("download failed. video:{}", requestInfoDTO.getFilePath());
                e.printStackTrace();
            }
        });
        log.info("search and download completed for dir {}", path);
    }

    private static void findAllVideo(String path, List<String> videoList) {
        File file = new File(path);

        if (!file.exists()) {
            return;
        }
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (Objects.isNull(files)) {
                return;
            }
            for (File value : files) {
                findAllVideo(value.getAbsolutePath(), videoList);
            }

        }
        if (!path.contains(".")) {
            return;
        }
        String suffix = path.substring(path.lastIndexOf(".") + 1, path.length());
        if (videoSuffix.contains(suffix)) {
            videoList.add(file.getAbsolutePath());
        }
    }

}
