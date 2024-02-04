package iss.AD.myhealthapp;

public class Exercise {
    private String name;
    private int caloriesBurntPer30Minutes;

    public Exercise(String name, int caloriesBurntPer30Minutes) {
        this.name = name;
        this.caloriesBurntPer30Minutes = caloriesBurntPer30Minutes;
    }

    public String getName() {
        return name;
    }

    public int getCaloriesBurntPer30Minutes() {
        return caloriesBurntPer30Minutes;
    }

    @Override
    public String toString() {
        return name;
    }
}