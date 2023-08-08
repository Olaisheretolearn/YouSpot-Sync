public class SongInfo {
    private String songName;
    private String artistName;
    private String youtubeVideoId;
    private String spotifyUri;

    // Constructor, getters, and setters
    public  SongInfo(String songName, String artistName, String youtubeVideoId , String spotifyUri){
        this.artistName = artistName;
        this.songName = songName;
        this.spotifyUri = spotifyUri;
        this.youtubeVideoId = youtubeVideoId;

    }
    // ...

    public String getSongName() {
        return songName;
    }

    public String getArtistName() {
        return artistName;
    }

    public String getSpotifyUri() {
        return spotifyUri;
    }

    public void setSpotifyUri(String spotifyUri) {
        this.spotifyUri = spotifyUri;
    }

    public String getYoutubeVideoId() {
        return youtubeVideoId;
    }
}
