package com.arcfour.musicmute;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("musicmute")
public interface MusicMuteConfig extends Config {
	@ConfigItem(
			keyName = "mutedTracks",
			name = "Muted Tracks",
			description = "Comma-separated list of muted music tracks"
	)
	default String mutedTracks() {
		return "";
	}

	@ConfigItem(
			keyName = "mutedTracks",
			name = "",
			description = ""
	)
	void mutedTracks(String key);
}

