package iss.AD.myhealthapp;

public class AdvInfo {
    private String imageUrl;
    private String description;


    // 构造函数
    public AdvInfo(String imageUrl, String description, String videoUrl) {
        this.imageUrl = imageUrl;
        this.description = description;
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

}