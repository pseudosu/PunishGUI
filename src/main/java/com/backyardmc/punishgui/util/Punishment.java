package com.backyardmc.punishgui.util;

import java.lang.reflect.Field;

public class Punishment {

    private int id;
    private String name;
    private String firstOffense;
    private String secondOffense;
    private String thirdOffense;
    private String fourthOffense;
    private final int maxWarningLevel;

    public Punishment(int id, String name, String firstOffense, String secondOffense, String thirdOffense, String fourthOffense, int maxWarningLevel) {
        this.id = id;
        this.name = name;
        this.firstOffense = firstOffense;
        this.secondOffense = secondOffense;
        this.thirdOffense = thirdOffense;
        this.fourthOffense = fourthOffense;
        this.maxWarningLevel = maxWarningLevel;
    }

    public Punishment(int id, String name, String firstOffense, String secondOffense, String thirdOffense, int maxWarningLevel) {
        this.id = id;
        this.name = name;
        this.firstOffense = firstOffense;
        this.secondOffense = secondOffense;
        this.thirdOffense = thirdOffense;
        this.maxWarningLevel = maxWarningLevel;
    }

    public Punishment(int id, String name, String firstOffense, String secondOffense, int maxWarningLevel) {

        this.id = id;
        this.name = name;
        this.firstOffense = firstOffense;
        this.secondOffense = secondOffense;
        this.maxWarningLevel = maxWarningLevel;
    }

    public Punishment(int id, String name, String firstOffense, int maxWarningLevel) {
        this.id = id;
        this.name = name;
        this.firstOffense = firstOffense;
        this.maxWarningLevel = maxWarningLevel;
    }

    public int getId() {
        return id;
    }

    public String getPunishmentFromWarningLevel(int warningLevel) {
        switch (warningLevel) {
            case 1:
                return getFirstOffense();
            case 2:
                return getSecondOffense();
            case 3:
                return getThirdOffense();
            case 4:
                return getFourthOffense();
            default:
                return null;
        }
    }

    public String getName() {
        return name;
    }

    public String getFirstOffense() {
        return firstOffense;
    }

    public String getSecondOffense() {
        return secondOffense;
    }

    public String getThirdOffense() {
        return thirdOffense;
    }

    public String getFourthOffense() {
        return fourthOffense;
    }

    public String toString() {
        StringBuilder result = new StringBuilder();
        String newLine = System.getProperty("line.separator");

        result.append(this.getClass().getName());
        result.append(" Object {");
        result.append(newLine);

        //determine fields declared in this class only (no fields of superclass)
        Field[] fields = this.getClass().getDeclaredFields();

        //print field names paired with their values
        for (Field field : fields) {
            result.append("  ");
            try {
                result.append(field.getName());
                result.append(": ");
                //requires access to private field:
                result.append(field.get(this));
            } catch (IllegalAccessException ex) {
                ex.printStackTrace();
            }
            result.append(newLine);
        }
        result.append("}");

        return result.toString();
    }

    public int getMaxWarningLevel() {
        return maxWarningLevel;
    }

    public boolean isTimeBased(int maxWarningLevel) {
        if (getPunishmentFromWarningLevel(maxWarningLevel).equalsIgnoreCase("kick"))
            return false;
        return getPunishmentFromWarningLevel(maxWarningLevel).split(" ").length > 1;
    }

    public String getPunishmentType(int warningLevel) {
        if (!isTimeBased(warningLevel)) {
            return getPunishmentFromWarningLevel(warningLevel);
        } else {
            return getPunishmentFromWarningLevel(warningLevel).split(" ")[2];
        }
    }

    public int getPunishmentTime(int warningLevel) {
        return Integer.parseInt(getPunishmentFromWarningLevel(warningLevel).split(" ")[0]);
    }

    public String getPunishmentTimeIncrement(int warningLevel) {
        return getPunishmentFromWarningLevel(warningLevel).split(" ")[1];
    }
}
