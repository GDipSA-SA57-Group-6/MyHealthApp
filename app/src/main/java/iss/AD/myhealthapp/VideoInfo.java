package iss.AD.myhealthapp;

public class VideoInfo {
    private String imageUrl;
    private String description;
    private String vidUrl;

    // 构造函数
    public VideoInfo(String imageUrl, String description, String vidUrl) {
        this.imageUrl = imageUrl;
        this.description = description;
        this.vidUrl = vidUrl;
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
}

