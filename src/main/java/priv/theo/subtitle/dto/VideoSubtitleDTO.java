package priv.theo.subtitle.dto;

import java.io.File;

public class VideoSubtitleDTO {

    private String filePath;
    private String fileHash;
    private File sourceFile;
    private ShooterSubtitleDTO[] shooterSubtitleDTOs;

    public VideoSubtitleDTO() {
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public ShooterSubtitleDTO[] getShooterSubtitleDTOs() {
        return shooterSubtitleDTOs;
    }

    public void setShooterSubtitleDTOs(ShooterSubtitleDTO[] shooterSubtitleDTOS) {
        this.shooterSubtitleDTOs = shooterSubtitleDTOS;
    }

    public String getFileHash() {
        return fileHash;
    }

    public void setFileHash(String fileHash) {
        this.fileHash = fileHash;
    }

    public File getSourceFile() {
        return sourceFile;
    }

    public void setSourceFile(File sourceFile) {
        this.sourceFile = sourceFile;
    }
}
