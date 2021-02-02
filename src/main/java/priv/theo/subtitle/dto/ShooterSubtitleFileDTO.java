package priv.theo.subtitle.dto;

import com.google.gson.annotations.SerializedName;

import java.util.Objects;

/**
 * Ext    string  文件扩展名
 * Link   string  文件下载链接
 */
public class ShooterSubtitleFileDTO {
    @SerializedName("Ext")
    private String ext;
    @SerializedName("Link")
    private String link;

    public ShooterSubtitleFileDTO() {
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
        ShooterSubtitleFileDTO shooterSubtitleFileDTO = (ShooterSubtitleFileDTO) o;
        return Objects.equals(ext, shooterSubtitleFileDTO.ext) &&
                Objects.equals(link, shooterSubtitleFileDTO.link);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ext, link);
    }
}
