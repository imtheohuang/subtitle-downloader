package priv.theo.subtitle;

import com.google.gson.annotations.SerializedName;

import java.util.Objects;

/**
 * Ext    string  文件扩展名
 * Link   string  文件下载链接
 */
public class FileInfo {
    @SerializedName("Ext")
    private String ext;
    @SerializedName("Link")
    private String link;

    public FileInfo() {
    }

    public String getExt() {
        return ext;
    }

    public void setExt(String ext) {
        this.ext = ext;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    @Override
    public String toString() {
        return "FileInfo{" +
                "ext='" + ext + '\'' +
                ", link='" + link + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FileInfo fileInfo = (FileInfo) o;
        return Objects.equals(ext, fileInfo.ext) &&
                Objects.equals(link, fileInfo.link);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ext, link);
    }
}
