package com.arcfour.musicmute;

import javax.inject.Inject;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.api.MenuAction;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.util.Text;

import java.util.HashSet;
import java.util.Set;

@Slf4j
@PluginDescriptor(
		name = "Music Mute",
		description = "Allows you to automatically mute specific music tracks",
		tags = {"sound"}
)

public class MusicMutePlugin extends Plugin {
	@Inject
	private Client client;

	@Inject
	private MusicMuteConfig config;

	private Set<String> mutedTracks = new HashSet<>();
	private int prevVolume = -1;
	private String lastTrackName = "";
	private int WIDGET_GROUP_ID = 239;
	private int WIDGET_CHILD_ID = 9;
	private static final String MUTE_OPTION = "Mute";
	private static final String UNMUTE_OPTION = "Unmute";
	private static final int MUSIC_LIST_GROUP_ID = WidgetInfo.MUSIC_TRACK_LIST.getGroupId();

	private boolean isDeveloperMode;

	@Override
	protected void startUp() throws Exception {
		log.debug("Music Mute plugin initializing");
		loadMutedTracks();
		isDeveloperMode = Boolean.getBoolean("runelite.developer.mode");
		if(isDeveloperMode) {
			log.debug("Music Mute plugin: developer mode enabled");
		}
	}

	@Override
	protected void shutDown() throws Exception {
		log.debug("Music Mute plugin shutting down");
		mutedTracks.clear();
		prevVolume = -1;
		lastTrackName = "";
	}

	@Subscribe
	public void onGameTick(GameTick event) {
		if(client.getGameState() != GameState.LOGGED_IN) { return; }

		Widget musicTrackWidget = client.getWidget(WIDGET_GROUP_ID, WIDGET_CHILD_ID);
		if(musicTrackWidget != null) {
			String currentTrack = musicTrackWidget.getText();

			if(!currentTrack.equals(lastTrackName)) {
				lastTrackName = currentTrack;
				if(isDeveloperMode) {
					log.debug("Music track changed to: {}", currentTrack);
				}
			}

			boolean shouldMute = mutedTracks.contains(currentTrack);
			int currentVolume = client.getMusicVolume();

			if(shouldMute && currentVolume != 0) {
				if(prevVolume == -1) {
					prevVolume = currentVolume;
					log.debug("Saved previous music volume: {}", prevVolume);
				}
				client.setMusicVolume(0);
				log.debug("Muted track: {}", currentTrack);
			} else if(!shouldMute && currentVolume == 0 && prevVolume != -1) {
				client.setMusicVolume(prevVolume);
				log.debug("Unmuted music, prevVolume: {}", prevVolume);
				prevVolume = -1;
			}
		}
	}

	@Subscribe
	public void onMenuEntryAdded(MenuEntryAdded event) {
		final int groupId = WidgetInfo.TO_GROUP(event.getActionParam1());

		if (groupId == MUSIC_LIST_GROUP_ID && (event.getOption().equals("Play") || event.getOption().equals("Unlock"))) {
			String trackName = Text.removeTags(event.getTarget());
			log.debug("Menu entry added for track: {}", trackName);

			// Insert "Mute" or "Unmute" menu item based on the current track state
			client.createMenuEntry(-1)
					.setOption(mutedTracks.contains(trackName) ? UNMUTE_OPTION : MUTE_OPTION)
					.setTarget(event.getTarget())
					.setType(MenuAction.RUNELITE)
					.onClick(e -> {
						if (mutedTracks.contains(trackName)) {
							unmuteTrack(trackName);
							log.debug("Unmuted track: {}", trackName);
						} else {
							muteTrack(trackName);
							log.debug("Muted track: {}", trackName);
						}
					});
		}
	}

	private void muteTrack(String trackName) {
		mutedTracks.add(trackName);
		saveMutedTracks();
	}

	private void unmuteTrack(String trackName) {
		mutedTracks.remove(trackName);
		saveMutedTracks();
	}

	private void loadMutedTracks() {
		String[] tracks = config.mutedTracks().split(",");
		for (String track : tracks) {
			if (!track.isEmpty()) {
				mutedTracks.add(track);
				log.debug("Loaded muted track: {}", track);
			}
		}
	}

	private void saveMutedTracks() {
		config.mutedTracks(String.join(",", mutedTracks));
		log.debug("Saved muted tracks: {}", mutedTracks);
	}

	@Provides
	MusicMuteConfig provideConfig(ConfigManager configManager) {
		return configManager.getConfig(MusicMuteConfig.class);
	}
}

