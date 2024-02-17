package iss.AD.myhealthapp.model;

import java.util.HashSet;
import java.util.Set;

public class GroupHub {
    private Long id;
    private String name;
    private int quantity;
    private int initialQuantity;
    private int likes;
    /**
     * 暂时不使用
     */
    private boolean isDependOnQuantity;
    private double latitude;
    private double longitude;
    /**
     * 暂时不使用
     */
    private boolean isDependOnLocation;
    private String startTime;
    private String endTime;
    /**
     * 暂时不使用
     */
    private boolean isDependOnTime;
    private User publishedBy;
    private Set<User> hasUsers;
    public GroupHub() {
        hasUsers = new HashSet<>();
    }
    // ... other getters and setters
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public void incrementLikes() {
        this.likes++;
    }
    // toString method for debugging purposes
    public void setConfirmed(boolean b) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setConfirmed'");
    }
    public void setCancelled(boolean b) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setCancelled'");
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public int getQuantity() {
        return quantity;
    }
    public void setQuantity(int quantity) {
        this.initialQuantity = quantity;
        this.quantity = quantity;
    }
    public int getLikes() {
        return likes;
    }
    public void setLikes(int likes) {
        this.likes = likes;
    }
    public boolean isDependOnQuantity() {
        return isDependOnQuantity;
    }
    public void setDependOnQuantity(boolean dependOnQuantity) {
        isDependOnQuantity = dependOnQuantity;
    }
    public double getLatitude() {
        return latitude;
    }
    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }
    public double getLongitude() {
        return longitude;
    }
    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
    public boolean isDependOnLocation() {
        return isDependOnLocation;
    }
    public void setDependOnLocation(boolean dependOnLocation) {
        isDependOnLocation = dependOnLocation;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public boolean isDependOnTime() {
        return isDependOnTime;
    }

    public void setDependOnTime(boolean dependOnTime) {
        isDependOnTime = dependOnTime;
    }

    public User getPublishedBy() {
        return publishedBy;
    }

    public void setPublishedBy(User publishedBy) {
        this.publishedBy = publishedBy;
    }

    public Set<User> getHasUsers() {
        return hasUsers;
    }

    public void setHasUsers(Set<User> hasUsers) {
        this.hasUsers = hasUsers;
    }

    public int getInitialQuantity() {
        return initialQuantity;
    }

    public void setInitialQuantity(int initialQuantity) {
        this.initialQuantity = initialQuantity;
    }
}
