package iss.AD.myhealthapp.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class User {
    private Integer userId;
    private List<GroupHub> publishedGroupHubs;
    private Set<GroupHub> belongsToGroupHubs;
    public User() {
        publishedGroupHubs = new ArrayList<>();
        belongsToGroupHubs = new HashSet<>();
    }
    // Getters and setters for all the fields
    public Integer getUserId() {
        return userId;
    }
    public void setUserId(Integer userId) {
        this.userId = userId;
    }
    public List<GroupHub> getPublishedGroupHubs() {
        return publishedGroupHubs;
    }
    public void setPublishedGroupHubs(List<GroupHub> publishedGroupHubs) {
        this.publishedGroupHubs = publishedGroupHubs;
    }
}
