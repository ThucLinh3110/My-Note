package com.example.mynote.models;

public class SettingItem {
    public static final int TYPE_PROFILE_CARD = 0;
    public static final int TYPE_MENU_CARD = 1;
    public static final int TYPE_SECTION_TITLE = 2;
    public static final int TYPE_ACCOUNT_CARD = 3;
    public static final int TYPE_ADD_BUTTON = 4;

    private int type;
    private String title;
    private String subtitle;
    private User account1;
    private User account2;

    public SettingItem(int type, String title, String subtitle) {
        this.type = type;
        this.title = title;
        this.subtitle = subtitle;
    }

    public SettingItem(int type, User account1, User account2) {
        this.type = type;
        this.account1 = account1;
        this.account2 = account2;
    }

    public int getType() {
        return type;
    }

    public String getTitle() {
        return title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public User getAccount1() {
        return account1;
    }

    public User getAccount2() {
        return account2;
    }
}