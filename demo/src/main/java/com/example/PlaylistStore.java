package com.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PlaylistStore {

    private static final File SAVE_FILE = new File(
        System.getProperty("user.home"), ".mp3player_playlist.json"
    );

    private final ObjectMapper mapper = new ObjectMapper()
        .enable(SerializationFeature.INDENT_OUTPUT);

    /** Saves the current playlist to ~/.mp3player_playlist.json */
    public void save(List<Song> songs) {
        try {
            List<String> paths = new ArrayList<>();
            for (Song s : songs) paths.add(s.getFilePath());
            mapper.writeValue(SAVE_FILE, paths);
        } catch (IOException e) {
            System.err.println("Failed to save playlist: " + e.getMessage());
        }
    }

    /** Loads songs from ~/.mp3player_playlist.json, skipping any missing files */
    public List<Song> load() {
        if (!SAVE_FILE.exists()) return new ArrayList<>();
        try {
            String[] paths = mapper.readValue(SAVE_FILE, String[].class);
            List<Song> songs = new ArrayList<>();
            for (String path : paths) {
                File f = new File(path);
                if (f.exists()) songs.add(new Song(f));
                else System.err.println("Skipping missing file: " + path);
            }
            return songs;
        } catch (IOException e) {
            System.err.println("Failed to load playlist: " + e.getMessage());
            return new ArrayList<>();
        }
    }
}