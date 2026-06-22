package com.example;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.util.List;

public class PlayerView {

    private final Stage stage;
    private final ObservableList<Song> playlist = FXCollections.observableArrayList();
    private final PlayerController controller;
    private final PlaylistStore store = new PlaylistStore();

    private Label nowPlayingLabel;
    private Label currentTimeLabel;
    private Label totalTimeLabel;
    private Slider progressSlider;
    private Slider volumeSlider;
    private Button playPauseBtn;
    private ListView<Song> playlistView;

    private boolean seeking = false;
    private Duration totalDuration = Duration.ZERO;

    public PlayerView(Stage stage) {
        this.stage = stage;
        this.controller = new PlayerController(playlist);

        // Load saved playlist on startup
        playlist.addAll(store.load());

        setupCallbacks();
    }

    private void setupCallbacks() {
        controller.setOnSongEnd(() -> controller.next());

        controller.setOnDurationReady(duration -> {
            totalDuration = duration;
            totalTimeLabel.setText(formatTime(duration));
            progressSlider.setMax(duration.toSeconds());
        });

        controller.setOnTimeUpdate(time -> {
            if (!seeking) {
                progressSlider.setValue(time.toSeconds());
                currentTimeLabel.setText(formatTime(time));
            }
        });

        controller.setOnSongChanged(index -> {
            playlistView.getSelectionModel().select(index);
            Song song = playlist.get(index);
            nowPlayingLabel.setText(song.getTitle());
            playPauseBtn.setText("⏸");
            progressSlider.setValue(0);
            currentTimeLabel.setText("0:00");
            totalTimeLabel.setText("0:00");
        });
    }

    public void show() {
        stage.setTitle("MP3 Player");
        stage.setMinWidth(480);
        stage.setMinHeight(580);

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #1a1a2e;");
        root.setPadding(new Insets(20));

        root.setTop(buildTopBar());
        root.setCenter(buildPlaylistView());
        root.setBottom(buildControls());

        Scene scene = new Scene(root, 480, 580);
        stage.setScene(scene);

        // Save playlist when the window is closed
        stage.setOnCloseRequest(e -> store.save(playlist));

        stage.show();
    }

    private VBox buildTopBar() {
        Label appTitle = new Label("♪ MP3 Player");
        appTitle.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        appTitle.setTextFill(Color.web("#e94560"));

        nowPlayingLabel = new Label("No song selected");
        nowPlayingLabel.setFont(Font.font("Arial", 14));
        nowPlayingLabel.setTextFill(Color.web("#a0a0c0"));
        nowPlayingLabel.setMaxWidth(Double.MAX_VALUE);
        nowPlayingLabel.setAlignment(Pos.CENTER);

        VBox top = new VBox(6, appTitle, nowPlayingLabel);
        top.setAlignment(Pos.CENTER);
        top.setPadding(new Insets(0, 0, 16, 0));
        return top;
    }

    private VBox buildPlaylistView() {
        playlistView = new ListView<>(playlist);
        playlistView.setPrefHeight(200);
        playlistView.setStyle(
            "-fx-background-color: #16213e;" +
            "-fx-border-color: #0f3460;" +
            "-fx-border-radius: 6;" +
            "-fx-background-radius: 6;" +
            "-fx-control-inner-background: #16213e;" +
            "-fx-text-fill: #c0c0e0;"
        );

        playlistView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Song item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("-fx-background-color: transparent;");
                } else {
                    setText("♪  " + item.getTitle());
                    setStyle(
                        "-fx-background-color: transparent;" +
                        "-fx-text-fill: #c0c0e0;" +
                        "-fx-font-size: 13px;"
                    );
                }
            }
        });

        playlistView.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                int idx = playlistView.getSelectionModel().getSelectedIndex();
                if (idx >= 0) controller.playSong(idx);
            }
        });

        Button addBtn = styledButton("+ Add Songs", "#0f3460", "#e94560");
        Button removeBtn = styledButton("Remove", "#0f3460", "#a0a0c0");

        addBtn.setOnAction(e -> addSongs());
        removeBtn.setOnAction(e -> removeSelected());

        HBox btnRow = new HBox(10, addBtn, removeBtn);
        btnRow.setPadding(new Insets(8, 0, 0, 0));

        VBox box = new VBox(8, playlistView, btnRow);
        VBox.setVgrow(playlistView, Priority.ALWAYS);
        return box;
    }

    private VBox buildControls() {
        progressSlider = new Slider(0, 1, 0);
        progressSlider.setMaxWidth(Double.MAX_VALUE);
        progressSlider.setStyle("-fx-accent: #e94560;");

        progressSlider.setOnMousePressed(e -> seeking = true);
        progressSlider.setOnMouseReleased(e -> {
            controller.seek(progressSlider.getValue());
            seeking = false;
        });

        currentTimeLabel = new Label("0:00");
        totalTimeLabel = new Label("0:00");
        styleTimeLabel(currentTimeLabel);
        styleTimeLabel(totalTimeLabel);

        HBox fullTimeRow = new HBox(currentTimeLabel, progressSlider, totalTimeLabel);
        HBox.setHgrow(progressSlider, Priority.ALWAYS);
        fullTimeRow.setSpacing(8);
        fullTimeRow.setAlignment(Pos.CENTER);

        Button prevBtn = styledButton("⏮", "#0f3460", "#c0c0e0");
        playPauseBtn   = styledButton("▶", "#e94560", "#ffffff");
        Button nextBtn = styledButton("⏭", "#0f3460", "#c0c0e0");
        Button stopBtn = styledButton("⏹", "#0f3460", "#c0c0e0");

        playPauseBtn.setMinWidth(60);

        prevBtn.setOnAction(e -> controller.previous());
        nextBtn.setOnAction(e -> controller.next());
        stopBtn.setOnAction(e -> {
            controller.stop();
            playPauseBtn.setText("▶");
            progressSlider.setValue(0);
            currentTimeLabel.setText("0:00");
        });
        playPauseBtn.setOnAction(e -> togglePlayPause());

        HBox btnRow = new HBox(12, prevBtn, playPauseBtn, nextBtn, stopBtn);
        btnRow.setAlignment(Pos.CENTER);

        Label volLabel = new Label("🔊");
        volLabel.setTextFill(Color.web("#a0a0c0"));
        volumeSlider = new Slider(0, 1, 0.8);
        volumeSlider.setPrefWidth(120);
        volumeSlider.setStyle("-fx-accent: #0f3460;");
        volumeSlider.valueProperty().addListener((obs, o, n) -> controller.setVolume(n.doubleValue()));

        HBox volRow = new HBox(8, volLabel, volumeSlider);
        volRow.setAlignment(Pos.CENTER_RIGHT);

        VBox controls = new VBox(10, fullTimeRow, btnRow, volRow);
        controls.setPadding(new Insets(16, 0, 0, 0));
        return controls;
    }

    private void togglePlayPause() {
        MediaPlayer.Status status = controller.getStatus();
        if (status == MediaPlayer.Status.PLAYING) {
            controller.pause();
            playPauseBtn.setText("▶");
        } else {
            if (playlist.isEmpty()) return;
            if (controller.getCurrentIndex() < 0) controller.playSong(0);
            else controller.play();
            playPauseBtn.setText("⏸");
        }
    }

    private void addSongs() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select MP3 Files");
        chooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("MP3 Files", "*.mp3")
        );
        List<File> files = chooser.showOpenMultipleDialog(stage);
        if (files != null) {
            for (File f : files) playlist.add(new Song(f));
            store.save(playlist); // save immediately after adding
        }
    }

    private void removeSelected() {
        int idx = playlistView.getSelectionModel().getSelectedIndex();
        if (idx >= 0) {
            playlist.remove(idx);
            store.save(playlist); // save immediately after removing
        }
    }

    private Button styledButton(String text, String bg, String fg) {
        Button btn = new Button(text);
        btn.setStyle(
            "-fx-background-color: " + bg + ";" +
            "-fx-text-fill: " + fg + ";" +
            "-fx-font-size: 14px;" +
            "-fx-padding: 6 14;" +
            "-fx-background-radius: 6;" +
            "-fx-cursor: hand;"
        );
        btn.setOnMouseEntered(e -> btn.setOpacity(0.8));
        btn.setOnMouseExited(e -> btn.setOpacity(1.0));
        return btn;
    }

    private void styleTimeLabel(Label label) {
        label.setTextFill(Color.web("#a0a0c0"));
        label.setFont(Font.font("Monospaced", 12));
        label.setMinWidth(36);
    }

    private String formatTime(Duration duration) {
        int seconds = (int) duration.toSeconds();
        return String.format("%d:%02d", seconds / 60, seconds % 60);
    }
}