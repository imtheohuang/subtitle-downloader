package priv.theo.service;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import priv.theo.subtitle.dto.ShooterSubtitleDTO;
import priv.theo.subtitle.dto.VideoSubtitleDTO;

import java.io.File;
import java.io.IOException;
import java.util.*;

@Slf4j
@Component
public class SubtitleDownloaderService {
    @Value("${config.videoSuffix}")
    private Set<String> videoSuffix;
//
//    public static void main(String[] args) {
//        String path = "/Volumes/N1/Share/Videos/TV";
//        searchAndDown(path);
//    }

    @Autowired
    private ShooterSubtitleService shooterSubtitleService;

    @Autowired
    private ThreadPoolTaskExecutor asyncExecutor;

    public void searchAndDownloadSubtitleForOne() {
        String filePath = "/Users/theo/Desktop/Young.Sheldon.S01E02.720p.HDTV.X264-DIMENSION.mkv";
        VideoSubtitleDTO videoSubtitleDTO = new VideoSubtitleDTO();
        videoSubtitleDTO.setSourceFile(new File(filePath));
        videoSubtitleDTO.setFilePath(filePath);
        try {
            videoSubtitleDTO.setFileHash(shooterSubtitleService.computeFileHash(videoSubtitleDTO.getSourceFile()));
        } catch (IOException e) {
            log.warn("Compute file hash error. Do not send request to service to search subtitle. File path={}", filePath);
            return;
        }

        try {
            ShooterSubtitleDTO[] subInfos = shooterSubtitleService.searchSubtitle(videoSubtitleDTO);
            videoSubtitleDTO.setShooterSubtitleDTOs(subInfos);

            shooterSubtitleService.downloadFirstSubtitle(videoSubtitleDTO);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void searchAndDown(String path) {
        log.info("search and down load subtitle for dir {}", path);
        List<String> videoList = new ArrayList<>();
        findAllVideo(path, videoList);

        if (videoList.isEmpty()) {
            log.warn("path {} do not has video.", path);
            return;
        }
        List<VideoSubtitleDTO> hasSubtitleToDownloadList = new ArrayList<>();

        log.info("video list: {}", videoList);
        for (String video : videoList) {
            VideoSubtitleDTO videoSubtitleDTO = new VideoSubtitleDTO();
            videoSubtitleDTO.setSourceFile(new File(video));
            videoSubtitleDTO.setFilePath(video);
            try {
                videoSubtitleDTO.setFileHash(shooterSubtitleService.computeFileHash(videoSubtitleDTO.getSourceFile()));
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

        hasSubtitleToDownloadList.forEach(videoSubtitleDTO -> shooterSubtitleService.downloadFirstSubtitle(videoSubtitleDTO));
        log.info("search and download completed for dir {}", path);
    }

    private void findAllVideo(String path, List<String> videoList) {
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
        String suffix = path.substring(path.lastIndexOf(".") + 1);
        if (videoSuffix.contains(suffix)) {
            videoList.add(file.getAbsolutePath());
        }
    }

}
