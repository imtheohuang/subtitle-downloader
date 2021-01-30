package priv.theo.subtitle;

import java.io.File;

public class RequestInfoDTO {

    private String filePath;
    private String fileHash;
    private File sourceFile;
    private SubInfo[] responseSubInfos;

    public RequestInfoDTO() {
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public SubInfo[] getResponseSubInfos() {
        return responseSubInfos;
    }

    public void setResponseSubInfos(SubInfo[] responseSubInfos) {
        this.responseSubInfos = responseSubInfos;
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
