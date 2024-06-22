package com.arcfour.musicmute;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class MusicMuteTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(MusicMutePlugin.class);
		RuneLite.main(args);
	}
}