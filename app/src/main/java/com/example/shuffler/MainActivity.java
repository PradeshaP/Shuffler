package com.example.shuffler;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.shuffler.api.RetrofitClientInstance;
import com.example.shuffler.api.SpotifyApi;
import com.example.shuffler.api.SpotifySearchResponse;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_PERMISSION = 1;
    private static final String PREFERENCE_NAME = "ShufflerPreferences";

    private MediaPlayer mediaPlayer;
    private List<File> audioFiles;
    private List<File> filteredAudioFiles;
    private Map<File, Integer> playFrequencyMap;
    private boolean isPlaying = false;
    private int currentIndex = -1;  // Start as -1 to ensure no automatic play

    private SharedPreferences sharedPreferences;
    private TextView currentlyPlayingTextView;
    private EditText searchBox;
    private ListView mp3ListView;
    private Button playPauseButton, nextButton, prevButton, shuffleButton;
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize variables
        mediaPlayer = new MediaPlayer();
        audioFiles = new ArrayList<>();
        filteredAudioFiles = new ArrayList<>();
        playFrequencyMap = new HashMap<>();
        sharedPreferences = getSharedPreferences(PREFERENCE_NAME, MODE_PRIVATE);

        // UI elements
        currentlyPlayingTextView = findViewById(R.id.currently_playing_textview);
        mp3ListView = findViewById(R.id.mp3_listview);
        playPauseButton = findViewById(R.id.play_pause_button);
        nextButton = findViewById(R.id.next_button);
        prevButton = findViewById(R.id.prev_button);
        searchBox = findViewById(R.id.search_box);
        shuffleButton = findViewById(R.id.shuffle_button);

        // Listeners
        shuffleButton.setOnClickListener(v -> shuffleAudioFilesByFrequency());
        playPauseButton.setOnClickListener(v -> togglePlayPause());
        nextButton.setOnClickListener(v -> nextAudio());
        prevButton.setOnClickListener(v -> prevAudio());
        mp3ListView.setOnItemClickListener((parent, view, position, id) -> {
            currentIndex = filteredAudioFiles.indexOf(audioFiles.get(position));
            prepareAndPlayAudio();
        });

        searchBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterSongs(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        checkAndLoadAudioFiles();
    }

    private void checkAndLoadAudioFiles() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            ContentResolver resolver = getContentResolver();
            Uri musicUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            String[] projection = {MediaStore.Audio.Media.DATA};

            Cursor cursor = resolver.query(musicUri, projection, null, null, null);
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    String path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
                    if (path.endsWith(".mp3")) {
                        File file = new File(path);
                        if (!audioFiles.contains(file)) {
                            audioFiles.add(file);
                        }
                    }
                }
                cursor.close();

                filteredAudioFiles.addAll(audioFiles);
                loadPlayFrequencies();
                updateListView();
            }
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_PERMISSION);
        }
    }

    private void shuffleAudioFilesByFrequency() {
        if (!audioFiles.isEmpty()) {
            Collections.sort(audioFiles, (file1, file2) -> {
                int freq1 = playFrequencyMap.getOrDefault(file1, 0);
                int freq2 = playFrequencyMap.getOrDefault(file2, 0);
                if (freq1 == freq2) {
                    return file1.getName().compareTo(file2.getName());
                }
                return Integer.compare(freq2, freq1); // Highest frequency first
            });

            filteredAudioFiles.clear();
            filteredAudioFiles.addAll(audioFiles);
            updateListView();
            currentIndex = -1;
            Toast.makeText(this, "Playlist shuffled by frequency!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "No audio files to shuffle!", Toast.LENGTH_SHORT).show();
        }
    }

    private void togglePlayPause() {
        if (currentIndex == -1) {
            currentIndex = 0;  // Set to the first index after shuffle
        }

        if (isPlaying) {
            mediaPlayer.pause();
        } else {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.start(); // Resume
            } else {
                prepareAndPlayAudio(); // Prepare and play if not playing
            }
        }

        isPlaying = !isPlaying;
        updatePlayPauseButton();
    }

    private void prepareAndPlayAudio() {
        if (!filteredAudioFiles.isEmpty() && currentIndex >= 0) {
            try {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                }
                mediaPlayer.reset();

                File currentFile = filteredAudioFiles.get(currentIndex);
                mediaPlayer.setDataSource(currentFile.getPath());

                mediaPlayer.setOnPreparedListener(mp -> {
                    mediaPlayer.start();
                    isPlaying = true;
                    currentlyPlayingTextView.setText("Playing: " + currentFile.getName());

                    // Update play frequency
                    int currentFrequency = playFrequencyMap.getOrDefault(currentFile, 0);
                    playFrequencyMap.put(currentFile, currentFrequency + 1);
                    savePlayFrequencies();
                    updatePlayPauseButton();
                });

                mediaPlayer.setOnCompletionListener(mp -> nextAudio());
                mediaPlayer.prepareAsync();
            } catch (IOException e) {
                Toast.makeText(this, "Error playing audio!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void nextAudio() {
        if (!filteredAudioFiles.isEmpty()) {
            currentIndex = (currentIndex + 1) % filteredAudioFiles.size();
            prepareAndPlayAudio();
        }
    }

    private void prevAudio() {
        if (!filteredAudioFiles.isEmpty()) {
            currentIndex = (currentIndex - 1 + filteredAudioFiles.size()) % filteredAudioFiles.size();
            prepareAndPlayAudio();
        }
    }

    private void filterSongs(String query) {
        filteredAudioFiles.clear();
        for (File file : audioFiles) {
            if (file.getName().toLowerCase().contains(query.toLowerCase())) {
                filteredAudioFiles.add(file);
            }
        }
        updateListView();
    }

    private void updateListView() {
        List<String> fileNames = new ArrayList<>();
        for (File file : filteredAudioFiles) {
            String fileNameWithCount = file.getName() + " (" + playFrequencyMap.getOrDefault(file, 0) + " plays)";
            fileNames.add(fileNameWithCount);
        }
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, fileNames);
        mp3ListView.setAdapter(adapter);
    }

    private void savePlayFrequencies() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        for (Map.Entry<File, Integer> entry : playFrequencyMap.entrySet()) {
            editor.putInt(entry.getKey().getPath(), entry.getValue());
        }
        editor.apply();
    }

    private void loadPlayFrequencies() {
        Map<String, ?> allEntries = sharedPreferences.getAll();
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            File file = new File(entry.getKey());
            playFrequencyMap.put(file, (Integer) entry.getValue());
        }
    }

    private void updatePlayPauseButton() {
        playPauseButton.setText(isPlaying ? "Pause" : "Play");
    }

    private void searchOnlineSongs(String query) {
        SpotifyApi spotifyApi = RetrofitClientInstance.getRetrofitInstance().create(SpotifyApi.class);
        Call<SpotifySearchResponse> call = spotifyApi.searchSongs(query, "track");
        call.enqueue(new Callback<SpotifySearchResponse>() {
            @Override
            public void onResponse(Call<SpotifySearchResponse> call, Response<SpotifySearchResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<SpotifySearchResponse.Track> tracks = response.body().getTracks().getItems();
                    displayOnlineSongs(tracks);
                } else {
                    Toast.makeText(MainActivity.this, "No results found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<SpotifySearchResponse> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayOnlineSongs(List<SpotifySearchResponse.Track> tracks) {
        List<String> songTitles = new ArrayList<>();
        for (SpotifySearchResponse.Track track : tracks) {
            String artistNames = "";
            for (SpotifySearchResponse.Artist artist : track.getArtists()) {
                artistNames += artist.getName() + ", ";
            }
            if (!artistNames.isEmpty()) {
                artistNames = artistNames.substring(0, artistNames.length() - 2); // Remove last comma
            }
            songTitles.add(track.getName() + " - " + artistNames);
        }

        ArrayAdapter<String> onlineAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, songTitles);
        mp3ListView.setAdapter(onlineAdapter);

        mp3ListView.setOnItemClickListener((parent, view, position, id) -> {
            SpotifySearchResponse.Track selectedTrack = tracks.get(position);
            openSongInExternalApp(selectedTrack);
        });
    }

    private void openSongInExternalApp(SpotifySearchResponse.Track track) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(track.getUrl()));
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                checkAndLoadAudioFiles();
            } else {
                Toast.makeText(this, "Permission denied!", Toast.LENGTH_SHORT).show();
            }
        }
    }
}