package iss.AD.myhealthapp;

public class AdvInfo {
    private String advurl;
    private String description;


    // 构造函数
    public AdvInfo(String advUrl, String description) {
        this.advurl = advUrl;
        this.description = description;
    }

    // Getter和Setter
    public String getAdvUrl() {
        return advurl;
    }

    public void setAdvUrl(String advUrl) {
        this.advurl = advUrl;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

}