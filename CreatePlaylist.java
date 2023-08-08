import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoListResponse;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class CreatePlaylist {
    private  final String spotifyToken;
    private  final String spotifyUserId;

    public CreatePlaylist(String spotifyToken, String spotifyUserId) {
        this.spotifyToken = spotifyToken;
        this.spotifyUserId = spotifyUserId;
    }


    public static void main(String[] args) {
        Secret secret = new Secret();
        String spotifyToken = secret.getSpotifyToken();
        String spotifyUserId = secret.getUserID();
        CreatePlaylist playlistCreator = new CreatePlaylist(spotifyToken, spotifyUserId);
        playlistCreator.addLikedVideosToPlaylist();
    }


    public void addLikedVideosToPlaylist() {
        try {
            List<VideoInfo> likedVideos = getLikedVideos();
            List<String> uris = new ArrayList<>();

            for (VideoInfo video : likedVideos) {
                String uri = getSpotifyURI(video.getSongName(), video.getArtist());
                if (uri != null) {
                    uris.add(uri);
                }
            }

            String playlistId = createPlaylist("Youtube Liked Vids");
            addSongsToPlaylist(playlistId, uris);
            System.out.println("Songs added to playlist successfully!");
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }


    private List<VideoInfo> getLikedVideos() throws IOException, JSONException {
        List<VideoInfo> videoInfoList = new ArrayList<>();

        // Make a GET request to the YouTube API
        String apiKey = "";
        String url = "https://www.googleapis.com/youtube/v3/videos?part=snippet&myRating=like&maxResults=50&key=" + apiKey;

        HttpURLConnection connection = null;
        BufferedReader reader = null;
        try {
            URL requestUrl = new URL(url);
            connection = (HttpURLConnection) requestUrl.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();

            // Read the response
            StringBuilder responseBuilder = new StringBuilder();
            reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                responseBuilder.append(line);
            }

            // Parse the response using org.json
            JSONObject json = new JSONObject(responseBuilder.toString());
            JSONArray items = json.getJSONArray("items");

            for (int i = 0; i < items.length(); i++) {
                JSONObject item = items.getJSONObject(i);
                String videoId = item.getString("id");
                JSONObject snippet = item.getJSONObject("snippet");
                String title = snippet.getString("title");

                // Extract song name and artist name from the video title
                String[] titleParts = title.split("-");
                String songName = titleParts[0].trim();
                String artistName = "";
                if (titleParts.length > 1) {
                    artistName = titleParts[1].trim();
                }

                String thumbnailUrl = snippet.getJSONObject("thumbnails").getJSONObject("default").getString("url");
                String videoUrl = "https://www.youtube.com/watch?v=" + videoId;

                // Create a VideoInfo object and add it to the list
                VideoInfo videoInfo = new VideoInfo(videoId, songName, artistName, title, thumbnailUrl, videoUrl);
                videoInfoList.add(videoInfo);
            }
        } finally {
            // Close the connection and reader
            if (connection != null) {
                connection.disconnect();
            }
            if (reader != null) {
                reader.close();
            }
        }

        return videoInfoList;
    }




    private String createPlaylist(String name) throws IOException, JSONException {
        String requestUrl = "https://api.spotify.com/v1/users/" + spotifyUserId + "/playlists";
        JSONObject requestBody = new JSONObject();
        requestBody.put("name", name);
        requestBody.put("public", true);

        HttpURLConnection connection = null;
        try {
            URL url = new URL(requestUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Authorization", "Bearer " + spotifyToken);
            connection.setDoOutput(true);

            String requestBodyString = requestBody.toString();
            try (OutputStream outputStream = connection.getOutputStream()) {
                byte[] input = requestBodyString.getBytes(StandardCharsets.UTF_8);
                outputStream.write(input, 0, input.length);
            }

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_CREATED) {
                StringBuilder response = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                }

                JSONObject responseJson = new JSONObject(response.toString());
                return responseJson.getString("id");
            } else {
                throw new IOException("Failed to create playlist. Response code: " + responseCode);
            }
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private void addSongsToPlaylist(String playlistId, List<String> uris) throws IOException {
        String requestUrl = "https://api.spotify.com/v1/playlists/" + playlistId + "/tracks";

        HttpURLConnection connection = null;
        try {
            URL url = new URL(requestUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Authorization", "Bearer " + spotifyToken);
            connection.setDoOutput(true);

            String requestPayload = new JSONObject().put("uris", uris).toString();
            try (OutputStream outputStream = connection.getOutputStream()) {
                byte[] input = requestPayload.getBytes(StandardCharsets.UTF_8);
                outputStream.write(input, 0, input.length);
            }

            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_CREATED) {
                throw new IOException("Failed to add songs to playlist. Response code: " + responseCode);
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private String getSpotifyURI(String songName, String artist) throws IOException, JSONException {
        String encodedSongName = URLEncoder.encode(songName, StandardCharsets.UTF_8);
        String encodedArtist = URLEncoder.encode(artist, StandardCharsets.UTF_8);

        String query = "https://api.spotify.com/v1/search?query=track%3A" + encodedSongName + "+artist%3A" + encodedArtist + "&type=track&offset=0&limit=1";

        HttpURLConnection connection = null;
        BufferedReader reader = null;
        try {
            URL url = new URL(query);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Authorization", "Bearer " + spotifyToken);

            // Read the response
            StringBuilder responseBuilder = new StringBuilder();
            reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                responseBuilder.append(line);
            }

            // Parse the response using org.json
            JSONObject json = new JSONObject(responseBuilder.toString());
            JSONArray tracks = json.getJSONObject("tracks").getJSONArray("items");


            if (tracks.length() > 0) {
                JSONObject track = tracks.getJSONObject(0);
                return track.getString("uri");
            }
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
            if (reader != null) {
                reader.close();
            }
        }

        return null;
    }

    private static class VideoInfo {
        private String videoId;
        private String songName;
        private String artist;
        private String title;
        private String thumbnailUrl;
        private String videoUrl;

        public VideoInfo(String videoId, String songName, String artist, String title, String thumbnailUrl, String videoUrl) {
            this.videoId = videoId;
            this.songName = songName;
            this.artist = artist;
            this.title = title;
            this.thumbnailUrl = thumbnailUrl;
            this.videoUrl = videoUrl;
        }

        public String getVideoId() {
            return videoId;
        }

        public String getSongName() {
            return songName;
        }

        public String getArtist() {
            return artist;
        }

        public String getTitle() {
            return title;
        }

        public String getThumbnailUrl() {
            return thumbnailUrl;
        }

        public String getVideoUrl() {
            return videoUrl;
        }
    }
}