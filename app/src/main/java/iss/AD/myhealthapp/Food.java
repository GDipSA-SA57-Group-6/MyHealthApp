package iss.AD.myhealthapp;

import android.os.Parcel;
import android.os.Parcelable;

public class Food implements Parcelable {

    private int id;
    private String name;
    private String quantity_description;

    private int protein;
    private int cal;
    private int fat;
    private int carb;
    private String userId;
    private int type;

    public Food() {}

    public Food(int id, String name, String quantity_description, int protein, int cal,
                int fat, int carb, String userId, int type) {
        this.id = id;
        this.name = name;
        this.quantity_description = quantity_description;
        this.protein = protein;
        this.cal = cal;
        this.fat = fat;
        this.carb = carb;
        this.userId = userId;
        this.type = type;
    }

    protected Food(Parcel in) {
        id = in.readInt();
        name = in.readString();
        quantity_description = in.readString();
        this.protein = in.readInt();
        this.cal = in.readInt();
        this.fat = in.readInt();
        this.carb = in.readInt();
        this.userId = in.readString();
        this.type = in.readInt();
    }

    public static final Creator<Food> CREATOR = new Creator<Food>() {
        @Override
        public Food createFromParcel(Parcel in) {
            return new Food(in);
        }

        @Override
        public Food[] newArray(int size) {
            return new Food[size];
        }
    };

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(name);
        dest.writeString(quantity_description);
        dest.writeInt(protein);
        dest.writeInt(cal);
        dest.writeInt(fat);
        dest.writeInt(carb);
        dest.writeString(userId);
        dest.writeInt(type);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getQuantityDescription() {
        return quantity_description;
    }

    public void setQuantityDescription(String quantity_description) {
        this.quantity_description = quantity_description;
    }

    @Override
    public String toString() {
        return "Food [id=" + id + ", name=" + name + ", quantity_description=" + quantity_description + "]";
    }

    public int getProtein() {
        return protein;
    }

    public void setProtein(int protein) {
        this.protein = protein;
    }

    public int getCal() {
        return cal;
    }

    public void setCal(int cal) {
        this.cal = cal;
    }

    public int getFat() {
        return fat;
    }

    public void setFat(int fat) {
        this.fat = fat;
    }

    public int getCab() {
        return carb;
    }

    public void setCab(int cab) {
        this.carb = cab;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}


/*
public class Food implements Parcelable {

    private int id;
    private String name;
    private String quantity_description;

    private int protein;
    private int cal;
    private int fat;
    private int cab;
    private String userId;
    private int type;

    public Food() {}

    public Food(int id, String name, String quantity_description, int protein, int cal,
                int fat, int cab, String userId, int type) {
        this.id = id;
        this.name = name;
        this.quantity_description = quantity_description;
        this.protein = protein;
        this.cal = cal;
        this.fat = fat;
        this.cab = cab;
        this.userId = userId;
        this.type = type;
    }

    protected Food(Parcel in) {
        id = in.readInt();
        name = in.readString();
        quantity_description = in.readString();
        this.protein = in.readInt();
        this.cal = in.readInt();
        this.fat = in.readInt();
        this.cab = in.readInt();
        this.userId = in.readString();
        this.type = in.readInt();
    }

    public static final Creator<Food> CREATOR = new Creator<Food>() {
        @Override
        public Food createFromParcel(Parcel in) {
            return new Food(in);
        }

        @Override
        public Food[] newArray(int size) {
            return new Food[size];
        }
    };

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(name);
        dest.writeString(quantity_description);
        dest.writeInt(protein);
        dest.writeInt(cal);
        dest.writeInt(fat);
        dest.writeInt(cab);
        dest.writeString(userId);
        dest.writeInt(type);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getQuantityDescription() {
        return quantity_description;
    }

    public void setQuantityDescription(String quantity_description) {
        this.quantity_description = quantity_description;
    }

    @Override
    public String toString() {
        return "Food [id=" + id + ", name=" + name + ", quantity_description=" + quantity_description + "]";
    }

    public int getProtein() {
        return protein;
    }

    public void setProtein(int protein) {
        this.protein = protein;
    }

    public int getCal() {
        return cal;
    }

    public void setCal(int cal) {
        this.cal = cal;
    }

    public int getFat() {
        return fat;
    }

    public void setFat(int fat) {
        this.fat = fat;
    }

    public int getCab() {
        return cab;
    }

    public void setCab(int cab) {
        this.cab = cab;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}

 */
