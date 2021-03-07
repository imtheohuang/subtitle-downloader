package priv.theo.utils;

import org.springframework.stereotype.Component;

/**
 * @author Theo
 * @date 2021/3/7
 */
@Component
public class SystemUtils {
    private static final String PROPERTY_OS_NAME = "os.name";
    private static final String OS_WINDOWS = "window";
    private static final String WINDOW_FILE_DELIMITER = "\\";
    private static final String Linux_FILE_DELIMITER = "/";

    public String getSystemDelimiter() {
        return System.getProperty(PROPERTY_OS_NAME).toLowerCase().startsWith(OS_WINDOWS) ? WINDOW_FILE_DELIMITER : Linux_FILE_DELIMITER;
    }
}
