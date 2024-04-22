package com.sample.core.pojos;

public class NavigationItem {
    private String title;
    private String name;
    private String path;

    public NavigationItem(String title, String name, String path) {
        this.title = title;
        this.name = name;
        this.path = path;
    }

    public String getTitle() {
        return title;
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }
}