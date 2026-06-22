package com.example;

import java.io.File;

public class Song {

    private final String title;
    private final String filePath;

    // Original — used when adding local files
    public Song(File file) {
        this.filePath = file.toURI().toString();
        String name = file.getName();
        this.title = name.endsWith(".mp3") ? name.substring(0, name.length() - 4) : name;
    }

    // New — used when loading songs back from Firebase (HTTPS URL instead of local file)
    public Song(String title, String url) {
        this.title = title;
        this.filePath = url;
    }

    public String getTitle() {
        return title;
    }

    public String getFilePath() {
        return filePath;
    }

    @Override
    public String toString() {
        return title;
    }
}