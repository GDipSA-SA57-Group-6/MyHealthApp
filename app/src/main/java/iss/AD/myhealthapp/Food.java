package iss.AD.myhealthapp;

import android.os.Parcel;
import android.os.Parcelable;

public class Food implements Parcelable {

    private int id;
    private String name;
    private String quantity_description;

    public Food() {}

    public Food(int id, String name, String quantity_description) {
        this.id = id;
        this.name = name;
        this.quantity_description = quantity_description;
    }

    protected Food(Parcel in) {
        id = in.readInt();
        name = in.readString();
        quantity_description = in.readString();
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
}
