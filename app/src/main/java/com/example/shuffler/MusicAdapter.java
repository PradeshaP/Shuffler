package com.example.shuffler;

import android.content.Context;
import android.media.MediaPlayer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class MusicAdapter extends RecyclerView.Adapter<MusicAdapter.MusicViewHolder> {

    private Context context;
    private List<File> musicFiles;
    private MediaPlayer mediaPlayer;
    private Button playPauseButton;
    private boolean isPlaying = false;

    public MusicAdapter(Context context, List<File> musicFiles, Button playPauseButton) {
        this.context = context;
        this.musicFiles = musicFiles;
        this.playPauseButton = playPauseButton;
        this.mediaPlayer = new MediaPlayer();
    }

    @NonNull
    @Override
    public MusicViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.item_music, parent, false);
        return new MusicViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MusicViewHolder holder, int position) {
        File currentFile = musicFiles.get(position);
        holder.fileNameTextView.setText(currentFile.getName());

        holder.itemView.setOnClickListener(v -> {
            playAudio(currentFile);
        });

        playPauseButton.setOnClickListener(v -> {
            if (isPlaying) {
                mediaPlayer.pause();
                playPauseButton.setText("Play");
                isPlaying = false;
            } else {
                mediaPlayer.start();
                playPauseButton.setText("Pause");
                isPlaying = true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return musicFiles.size();
    }

    private void playAudio(File file) {
        try {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
                mediaPlayer.reset();
            }

            mediaPlayer.setDataSource(file.getPath());
            mediaPlayer.prepareAsync();
            mediaPlayer.setOnPreparedListener(mp -> {
                mediaPlayer.start();
                isPlaying = true;
                playPauseButton.setText("Pause");
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static class MusicViewHolder extends RecyclerView.ViewHolder {

        TextView fileNameTextView;

        public MusicViewHolder(View itemView) {
            super(itemView);
            fileNameTextView = itemView.findViewById(R.id.music_file_name);
        }
    }
}
