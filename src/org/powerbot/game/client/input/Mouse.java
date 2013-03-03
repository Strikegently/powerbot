package org.powerbot.game.client.input;

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

public abstract class Mouse extends Focus implements MouseListener, MouseMotionListener, MouseWheelListener {
	private int clientX;
	private int clientY;
	private int clientPressX = -1;
	private int clientPressY = -1;
	private long clientPressTime = -1;
	private boolean clientPresent;
	private boolean clientPressed;

	public abstract void _mouseClicked(MouseEvent e);

	public abstract void _mouseDragged(MouseEvent e);

	public abstract void _mouseEntered(MouseEvent e);

	public abstract void _mouseExited(MouseEvent e);

	public abstract void _mouseMoved(MouseEvent e);

	public abstract void _mousePressed(MouseEvent e);

	public abstract void _mouseReleased(MouseEvent e);

	public abstract void _mouseWheelMoved(MouseWheelEvent e);

	public int getX() {
		if (clientX == -1) {
			return 0;
		}
		return clientX;
	}

	public int getY() {
		if (clientY == -1) {
			return 0;
		}
		return clientY;
	}

	public Point getLocation() {
		return new Point(clientX == -1 ? 0 : clientX, clientY == -1 ? 0 : clientY);
	}

	public int getPressX() {
		return clientPressX;
	}

	public int getPressY() {
		return clientPressY;
	}

	public long getPressTime() {
		return clientPressTime;
	}

	public boolean isPressed() {
		return clientPressed;
	}

	public boolean isPresent() {
		return clientPresent;
	}

	public final void mouseClicked(final MouseEvent e) {
		clientX = e.getX();
		clientY = e.getY();
		_mouseClicked(e);
		e.consume();
	}

	public final void mouseDragged(final MouseEvent e) {
		clientX = e.getX();
		clientY = e.getY();
		_mouseDragged(e);
		e.consume();
	}

	public final void mouseEntered(final MouseEvent e) {
		clientPresent = true;
		clientX = e.getX();
		clientY = e.getY();
		_mouseEntered(e);
		e.consume();
	}

	public final void mouseExited(final MouseEvent e) {
		clientPresent = false;
		clientX = e.getX();
		clientY = e.getY();
		_mouseExited(e);
		e.consume();
	}

	public final void mouseMoved(final MouseEvent e) {
		clientX = e.getX();
		clientY = e.getY();
		_mouseMoved(e);
		e.consume();
	}

	public final void mousePressed(final MouseEvent e) {
		clientPressed = true;
		clientX = e.getX();
		clientY = e.getY();
		clientPressX = e.getX();
		clientPressY = e.getY();
		clientPressTime = System.currentTimeMillis();
		_mousePressed(e);
		e.consume();
	}

	public final void mouseReleased(final MouseEvent e) {
		clientX = e.getX();
		clientY = e.getY();
		clientPressed = false;

		_mouseReleased(e);
		e.consume();
	}

	public void mouseWheelMoved(final MouseWheelEvent e) {
		try {
			_mouseWheelMoved(e);
		} catch (final AbstractMethodError ignored) {
		}
		e.consume();
	}

	@Deprecated
	public final void move(final Point point) {
		update(point.x, point.y);
	}

	@Deprecated
	public final void update(final int x, final int y) {
		clientX = x;
		clientY = y;
	}

	public final void sendEvent(final MouseEvent e) {
		if (e == null) return;
		try {
			clientX = e.getX();
			clientY = e.getY();
			if (e.getID() == MouseEvent.MOUSE_CLICKED) {
				_mouseClicked(e);
			} else if (e.getID() == MouseEvent.MOUSE_DRAGGED) {
				_mouseDragged(e);
			} else if (e.getID() == MouseEvent.MOUSE_ENTERED) {
				clientPresent = true;
				_mouseEntered(e);
			} else if (e.getID() == MouseEvent.MOUSE_EXITED) {
				clientPresent = false;
				_mouseExited(e);
			} else if (e.getID() == MouseEvent.MOUSE_MOVED) {
				_mouseMoved(e);
			} else if (e.getID() == MouseEvent.MOUSE_PRESSED) {
				clientPressX = e.getX();
				clientPressY = e.getY();
				clientPressTime = e.getWhen();
				clientPressed = true;
				_mousePressed(e);
			} else if (e.getID() == MouseEvent.MOUSE_RELEASED) {
				clientPressed = false;
				_mouseReleased(e);
			} else if (e.getID() == MouseEvent.MOUSE_WHEEL) {
				_mouseWheelMoved((MouseWheelEvent) e);
			} else {
				throw new InternalError(e.toString());
			}
		} catch (final Exception ignored) {
		}
	}
}
