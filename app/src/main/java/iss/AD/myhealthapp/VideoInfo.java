package iss.AD.myhealthapp;

public class VideoInfo {
    private String imageUrl;
    private String description;
    private String videoUrl;

    // 构造函数
    public VideoInfo(String imageUrl, String description, String videoUrl) {
        this.imageUrl = imageUrl;
        this.description = description;
        this.videoUrl = videoUrl;
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

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }
}

