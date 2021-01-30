package priv.theo.subtitle;

import com.google.gson.annotations.SerializedName;

import java.util.Arrays;
import java.util.Objects;

/**
 * shooter subtitle info
 * Desc   string 备注信息
 * Delay  int32  字幕相对于视频的延迟时间，单位是毫秒
 * Files  []Fileinfo  包含文件信息的Array。 注：一个字幕可能会包含多个字幕文件，例如：idx+sub格式
 */
public class SubInfo {
    @SerializedName("Desc")
    private String desc;
    @SerializedName("Delay")
    private int delay;
    @SerializedName("Files")
    private FileInfo[] files;

    public SubInfo() {
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public int getDelay() {
        return delay;
    }

    public void setDelay(int delay) {
        this.delay = delay;
    }

    public FileInfo[] getFiles() {
        return files;
    }

    public void setFiles(FileInfo[] files) {
        this.files = files;
    }

    @Override
    public String toString() {
        return "SubInfo{" +
                "desc='" + desc + '\'' +
                ", delay=" + delay +
                ", files=" + Arrays.toString(files) +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SubInfo subInfo = (SubInfo) o;
        return delay == subInfo.delay &&
                Objects.equals(desc, subInfo.desc) &&
                Arrays.equals(files, subInfo.files);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(desc, delay);
        result = 31 * result + Arrays.hashCode(files);
        return result;
    }
}
