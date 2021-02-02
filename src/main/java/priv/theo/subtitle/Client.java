package priv.theo.subtitle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import priv.theo.service.ShooterSubtitleService;
import priv.theo.subtitle.dto.ShooterSubtitleDTO;
import priv.theo.subtitle.dto.VideoSubtitleDTO;

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
        VideoSubtitleDTO videoSubtitleDTO = new VideoSubtitleDTO();
        videoSubtitleDTO.setSourceFile(new File(filePath));
        videoSubtitleDTO.setFilePath(filePath);
        try {
            videoSubtitleDTO.setFileHash(ShooterUtils.computeFileHash(videoSubtitleDTO.getSourceFile()));
        } catch (IOException e) {
            log.warn("Compute file hash error. Do not send request to service to search subtitle. File path={}", filePath);
            return;
        }
        ShooterSubtitleService shooterSubtitleService = new ShooterSubtitleService();
        try {
            ShooterSubtitleDTO[] subInfos = shooterSubtitleService.searchSubtitle(videoSubtitleDTO);
            videoSubtitleDTO.setShooterSubtitleDTOs(subInfos);

            shooterSubtitleService.downloadFirstSubtitle(videoSubtitleDTO);
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
        List<VideoSubtitleDTO> hasSubtitleToDownloadList = new ArrayList<>();
        ShooterSubtitleService shooterSubtitleService = new ShooterSubtitleService();

        log.info("video list: {}", videoList);
        for (String video : videoList) {
            VideoSubtitleDTO videoSubtitleDTO = new VideoSubtitleDTO();
            videoSubtitleDTO.setSourceFile(new File(video));
            videoSubtitleDTO.setFilePath(video);
            try {
                videoSubtitleDTO.setFileHash(ShooterUtils.computeFileHash(videoSubtitleDTO.getSourceFile()));
            } catch (IOException e) {
                log.warn("Compute file hash error. Do not send request to service to search subtitle. File path={}", video);
                e.printStackTrace();
                continue;
            }
            try {
                ShooterSubtitleDTO[] subInfos;
                subInfos = shooterSubtitleService.searchSubtitle(videoSubtitleDTO);
                videoSubtitleDTO.setShooterSubtitleDTOs(subInfos);
                hasSubtitleToDownloadList.add(videoSubtitleDTO);
            } catch (IOException e) {
                log.info("Search failed, file can not download: {}", video);
                e.printStackTrace();
            }
        }

        if (hasSubtitleToDownloadList.isEmpty()) {
            log.warn("path {} do not need to download subtitle.", path);
        }

        hasSubtitleToDownloadList.forEach(videoSubtitleDTO -> {
            try {
                shooterSubtitleService.downloadFirstSubtitle(videoSubtitleDTO);
            } catch (IOException e) {
                log.warn("download failed. video:{}", videoSubtitleDTO.getFilePath());
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
