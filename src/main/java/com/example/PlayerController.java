package com.example;

import javafx.collections.ObservableList;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

public class PlayerController {

    private MediaPlayer mediaPlayer;
    private ObservableList<Song> playlist;
    private int currentIndex = -1;

    private Runnable onSongEnd;
    private java.util.function.Consumer<Duration> onTimeUpdate;
    private java.util.function.Consumer<Duration> onDurationReady;
    private java.util.function.Consumer<Integer> onSongChanged;

    public PlayerController(ObservableList<Song> playlist) {
        this.playlist = playlist;
    }

    public void playSong(int index) {
        if (index < 0 || index >= playlist.size()) return;

        stopCurrent();

        currentIndex = index;
        Song song = playlist.get(index);

        Media media = new Media(song.getFilePath());
        mediaPlayer = new MediaPlayer(media);

        mediaPlayer.setOnReady(() -> {
            if (onDurationReady != null) {
                onDurationReady.accept(media.getDuration());
            }
        });

        mediaPlayer.currentTimeProperty().addListener((obs, oldTime, newTime) -> {
            if (onTimeUpdate != null) {
                onTimeUpdate.accept(newTime);
            }
        });

        mediaPlayer.setOnEndOfMedia(() -> {
            if (onSongEnd != null) onSongEnd.run();
        });

        mediaPlayer.play();

        if (onSongChanged != null) onSongChanged.accept(currentIndex);
    }

    public void play() {
        if (mediaPlayer != null) mediaPlayer.play();
        else if (!playlist.isEmpty()) playSong(0);
    }

    public void pause() {
        if (mediaPlayer != null) mediaPlayer.pause();
    }

    public void stop() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
        }
    }

    public void next() {
        if (playlist.isEmpty()) return;
        int next = (currentIndex + 1) % playlist.size();
        playSong(next);
    }

    public void previous() {
        if (playlist.isEmpty()) return;
        int prev = (currentIndex - 1 + playlist.size()) % playlist.size();
        playSong(prev);
    }

    public void seek(double seconds) {
        if (mediaPlayer != null) {
            mediaPlayer.seek(Duration.seconds(seconds));
        }
    }

    public void setVolume(double volume) {
        if (mediaPlayer != null) mediaPlayer.setVolume(volume);
    }

    public MediaPlayer.Status getStatus() {
        if (mediaPlayer == null) return null;
        return mediaPlayer.getStatus();
    }

    public int getCurrentIndex() {
        return currentIndex;
    }

    private void stopCurrent() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.dispose();
            mediaPlayer = null;
        }
    }

    // Callback setters
    public void setOnSongEnd(Runnable onSongEnd) { this.onSongEnd = onSongEnd; }
    public void setOnTimeUpdate(java.util.function.Consumer<Duration> cb) { this.onTimeUpdate = cb; }
    public void setOnDurationReady(java.util.function.Consumer<Duration> cb) { this.onDurationReady = cb; }
    public void setOnSongChanged(java.util.function.Consumer<Integer> cb) { this.onSongChanged = cb; }
}