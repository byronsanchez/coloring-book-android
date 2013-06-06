/*
 * MusicManager
 * Copyright (c) 2009 Robert Green
 * www.rbgrn.net
 * www.batterypoweredgames.com
 * 
 * Modified by Byron Sanchez (hackbytes.com)
 */

package net.globide.coloring_book_08;

import java.util.Collection;
import java.util.HashMap;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaPlayer;

/**
 * Manages whether or not music is playing based on user preferences.
 */

public class MusicManager {

    // Define a boolean for determining whether or not the user has enabled the
    // Media Player via preferences.
    private static boolean sIsEnabled = false;
    public static boolean sIsManualSound = false;

    // Define a flag for signaling previous music for music switches.
    private static final int MUSIC_PREVIOUS = -1;
    // Define integer ids for each music type.
    public static final int MUSIC_A = 0;

    // Define a hash map in which to store the media players corresponding to
    // each
    // song (useful for multiple song management).
    private static HashMap<Integer, MediaPlayer> sPlayers = new HashMap<Integer, MediaPlayer>();
    // Define current and previous music statuses as nil to start off with.
    private static int sCurrentMusic = -1;
    private static int sPreviousMusic = -1;

    // Define SharedPreferences persistent data storage properties.
    private static SharedPreferences sSharedPreferences;

    /**
     * Gets the Music Player Volume Preference.
     */
    public static float getMusicVolume() {
        // Get the preference as an int.
        int volumeInt = sSharedPreferences.getInt("sbSettingsMusicVolume", 100);

        // return the value as a float.
        return Float.valueOf(volumeInt);
    }

    /**
     * Gets the Music Player Status Preference - whether or not music is
     * enabled.
     */
    public static boolean getMusicStatus(Context context) {
        // Get the preference as a boolean.
        boolean volumeBool = sSharedPreferences.getBoolean(
                "tbSettingsMusicIsChecked", false);

        // return the boolean value
        return volumeBool;
    }

    /**
     * Sets the preferences for this static class. This MUST be set before
     * MusicManager can be used.
     */
    public static void setPreferences(SharedPreferences sp) {
        sSharedPreferences = sp;
    }

    /**
     * Starts playing the music.
     */
    public static void start(Context context, int music) {
        start(context, music, false);
    }

    /**
     * Starts playing the music.
     */
    public static void start(Context context, int music, boolean force) {
        // If the media player is NOT enabled, return. No music will play.
        // Start and stop of currently playing music should be handled in the
        // user
        // input activity, so functionality can be tailored to the activity.
        if (!sIsEnabled) {
            return;
        }

        // If a song is currently playing and a force hasn't been requested...
        if (!force && sCurrentMusic > -1) {
            // return and end the function. No need to start the new song.
            return;
        }
        // ???
        if (music == MUSIC_PREVIOUS) {
            music = sPreviousMusic;
        }
        // If the requested song is the currently playing song...
        if (music == sCurrentMusic) {
            // return, as the song is already playing.
            return;
        }
        // If a song is currently playing.
        if (sCurrentMusic != -1) {
            // Then since it got this far, a force must have been requested.

            // Set the previous music id to the currently playing music id.
            sPreviousMusic = sCurrentMusic;
            // Pause the currently playing music to get ready for a song change.
            pause();
        }

        // Set the current music id to the requested music id.
        sCurrentMusic = music;
        // Get the media players for the requested music from the HashMap.
        MediaPlayer mp = sPlayers.get(music);
        // If media players for this song exists...
        if (mp != null) {
            // If the song is currently not playing...
            if (!mp.isPlaying()) {
                // Start playing the song.
                mp.start();
            }
        }
        else {

            // Else, this is the first time this media player is being created
            // and
            // stored in the HashMap. Create each the media player for the
            // requested
            // song, attach it to its corresponding resource, and store the
            // player in
            // the Hash Map for easy access later.

            // Put if, else if cases here; one for each MUSIC_CONSTANT defined
            // above.
            // The code should be roughly the same for each case except for the
            // conditional check and the create method should references the
            // corresponding media resource for that soung
            if (music == MUSIC_A) {
                mp = MediaPlayer.create(context, R.raw.beat_delib_01);
            }
            else {
                // unsupported music number...
                // There's nothing to do with a song that doesn't exist. Return
                // the
                // method.
                return;
            }

            // Store the media player in the HashMap, keyed by it's id.
            sPlayers.put(music, mp);
            // Get the volume preference.
            float volume = getMusicVolume();
            // Set the music volume on this media player.
            mp.setVolume(volume, volume);

            // If the media player was successfully created...
            if (mp != null) {
                // Enable looping.
                mp.setLooping(true);
                // Start playing the song.
                mp.start();
            }
        }
    }

    /**
     * Pauses the music.
     */
    public static void pause() {
        // Get a Collection of all the media players in the HashMap.
        Collection<MediaPlayer> mps = sPlayers.values();

        // Iterate through the collection...
        for (MediaPlayer p : mps) {
            // If a media player is playing...
            if (p.isPlaying()) {
                // pause it.
                p.pause();
            }
        }

        // previousMusic should always be something valid, so check to make sure
        // that currentMusic is not signalling nil.
        if (sCurrentMusic != -1) {
            // Set the previous music id to be the current music id.
            sPreviousMusic = sCurrentMusic;
        }

        // Set the current music id flag to signal that there is no current
        // music
        // playing
        sCurrentMusic = -1;
    }

    /**
     * Updates the volume based on user preferences.
     */
    public static void updateVolume() {
        // Get the volume from preferences.
        float volume = getMusicVolume();
        // Get the media player collection from the HashMap.
        Collection<MediaPlayer> mps = sPlayers.values();
        // Set the volume for each media player.
        for (MediaPlayer p : mps) {
            p.setVolume(volume, volume);
        }
    }

    /**
     * Updates the player status based on user preferences.
     */
    public static void updateStatusFromPrefs(Context context) {
        // Get the volume from preferences.
        boolean status = getMusicStatus(context);
        sIsEnabled = status;
    }

    /**
     * Releases the media players when they are not needed.
     */
    public static void release() {
        // Get the media player collection from the HashMap.
        Collection<MediaPlayer> mps = sPlayers.values();
        for (MediaPlayer mp : mps) {
            // If a media player exists in this iteration...
            if (mp != null) {
                // If that media player is currently playing...
                if (mp.isPlaying()) {
                    // Stop the music...
                    mp.stop();
                }
                // Release the media player.
                mp.release();
            }
        }

        // Clear the collection.
        mps.clear();
        // If the current music id is not nil...
        if (sCurrentMusic != -1) {
            // Set the previous music id to the current music id.
            sPreviousMusic = sCurrentMusic;
        }

        // Set the current music id to signal nil.
        sCurrentMusic = -1;
    }
}
