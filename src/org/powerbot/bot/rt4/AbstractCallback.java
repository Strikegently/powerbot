package org.powerbot.bot.rt4;

import org.powerbot.bot.EventDispatcher;
import org.powerbot.bot.rt4.client.Callback;
import org.powerbot.script.MessageEvent;

public class AbstractCallback implements Callback {
	private final EventDispatcher dispatcher;

	public AbstractCallback(final Bot bot) {
		dispatcher = bot.dispatcher;
	}

	@Override
	public void onMessage(final int id, final String sender, final String message) {
		System.out.println(String.format("[%d] %s: %s", id, sender, message));
		dispatcher.dispatch(new MessageEvent(id, sender, message));
	}
}