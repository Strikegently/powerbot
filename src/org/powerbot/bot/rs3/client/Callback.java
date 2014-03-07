package org.powerbot.bot.rs3.client;

public interface Callback {
	public void updateRenderInfo(Render render);

	public void notifyMessage(int id, String sender, String message);

	public void notifyObjectDefinitionLoad(RSObjectDef def);

	public void updateCamera(RSInteractableLocation offset, RSInteractableLocation center);
}