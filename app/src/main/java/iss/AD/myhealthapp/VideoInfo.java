package iss.AD.myhealthapp;

import android.os.Parcel;
import android.os.Parcelable;

public class VideoInfo implements Parcelable {
    private String imageUrl;
    private String description;
    private String vidUrl;

    // 构造函数
    public VideoInfo(String imageUrl, String description, String vidUrl) {
        this.imageUrl = imageUrl;
        this.description = description;
        this.vidUrl = vidUrl;
    }

    protected VideoInfo(Parcel in) {
        imageUrl = in.readString();
        description = in.readString();
        vidUrl = in.readString();
    }

    // Getter和Setter
    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getVidUrl() {
        return vidUrl;
    }

    public void setVidUrl(String videoUrl) {
        this.vidUrl = videoUrl;
    }

    // Parcelable接口实现
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(imageUrl);
        dest.writeString(description);
        dest.writeString(vidUrl);
    }

    // 创建CREATOR对象
    public static final Creator<VideoInfo> CREATOR = new Creator<VideoInfo>() {
        @Override
        public VideoInfo createFromParcel(Parcel in) {
            return new VideoInfo(in);
        }

        @Override
        public VideoInfo[] newArray(int size) {
            return new VideoInfo[size];
        }
    };
}

