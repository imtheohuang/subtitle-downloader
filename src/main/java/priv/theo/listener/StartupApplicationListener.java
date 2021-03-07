package priv.theo.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import priv.theo.service.SubtitleDownloaderService;

/**
 * @author Theo
 * @date 2021/3/6
 * @description
 */
@Slf4j
@Component
public class StartupApplicationListener implements ApplicationListener<ApplicationReadyEvent> {

    @Value("${config.targetPath}")
    private String path;

    @Value("${shooter.serverUrl}")
    private String serviceUrl;

    @Autowired
    SubtitleDownloaderService subtitleDownloaderService;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent applicationReadyEvent) {


        subtitleDownloaderService.searchAndDown(path);

    }
}
