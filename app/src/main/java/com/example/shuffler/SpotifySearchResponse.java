package com.example.shuffler.api;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class SpotifySearchResponse {
    @SerializedName("tracks")
    private Tracks tracks;

    public Tracks getTracks() {
        return tracks;
    }

    public class Tracks {
        @SerializedName("items")
        private List<Track> items;

        public List<Track> getItems() {
            return items;
        }
    }

    public class Track {
        @SerializedName("name")
        private String name;

        @SerializedName("artists")
        private List<Artist> artists;

        @SerializedName("external_urls")
        private ExternalUrls externalUrls;

        public String getName() {
            return name;
        }

        public List<Artist> getArtists() {
            return artists;
        }

        public String getUrl() {
            return externalUrls.getSpotify(); // Assuming you want the Spotify link
        }
    }

    public class Artist {
        @SerializedName("name")
        private String name;

        public String getName() {
            return name;
        }
    }

    public class ExternalUrls {
        @SerializedName("spotify")
        private String spotify;

        public String getSpotify() {
            return spotify;
        }
    }
}