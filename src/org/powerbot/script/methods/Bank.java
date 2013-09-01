package org.powerbot.script.methods;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.powerbot.script.lang.Filter;
import org.powerbot.script.lang.ItemQuery;
import org.powerbot.script.util.Random;
import org.powerbot.script.util.Timer;
import org.powerbot.script.wrappers.Component;
import org.powerbot.script.wrappers.GameObject;
import org.powerbot.script.wrappers.Interactive;
import org.powerbot.script.wrappers.Item;
import org.powerbot.script.wrappers.Locatable;
import org.powerbot.script.wrappers.Npc;
import org.powerbot.script.wrappers.Tile;
import org.powerbot.script.wrappers.Widget;

import static org.powerbot.script.util.Constants.getInt;
import static org.powerbot.script.util.Constants.getIntA;
import static org.powerbot.script.util.Constants.getObj;

public class Bank extends ItemQuery<Item> {
	public static final int[] BANK_NPC_IDS = getIntA("bank.npc.ids");
	public static final int[] BANK_BOOTH_IDS = getIntA("bank.booth.ids");
	public static final int[] BANK_COUNTER_IDS = getIntA("bank.counter.ids");
	public static final int[] BANK_CHEST_IDS = getIntA("bank.chest.ids");
	public static final Tile[] UNREACHABLE_BANK_TILES = getObj("bank.unreachable.tiles", Tile[].class);

	private static final Filter<Interactive> UNREACHABLE_FILTER = new Filter<Interactive>() {
		@Override
		public boolean accept(Interactive interactive) {
			if (interactive instanceof Locatable) {
				Tile tile = ((Locatable) interactive).getLocation();
				for (Tile bad : UNREACHABLE_BANK_TILES) {
					if (tile.equals(bad)) {
						return false;
					}
				}
			}
			return true;
		}
	};

	public static final int WIDGET = getInt("bank.widget");
	public static final int COMPONENT_BUTTON_CLOSE = getInt("bank.component.button.close");
	public static final int COMPONENT_CONTAINER_ITEMS = getInt("bank.component.container.items");
	public static final int COMPONENT_BUTTON_WITHDRAW_MODE = getInt("bank.component.button.withdraw.mode");
	public static final int COMPONENT_BUTTON_DEPOSIT_INVENTORY = getInt("bank.component.button.deposit.inventory");
	public static final int COMPONENT_BUTTON_DEPOSIT_MONEY = getInt("bank.component.button.deposit.money");
	public static final int COMPONENT_BUTTON_DEPOSIT_EQUIPMENT = getInt("bank.component.button.deposit.equipment");
	public static final int COMPONENT_BUTTON_DEPOSIT_FAMILIAR = getInt("bank.component.button.deposit.familiar");
	public static final int COMPONENT_SCROLL_BAR = getInt("bank.component.scroll.bar");
	public static final int SETTING_BANK_STATE = getInt("bank.setting.bank.state");
	public static final int SETTING_WITHDRAW_MODE = getInt("bank.setting.withdraw.mode");

	public Bank(MethodContext factory) {
		super(factory);
	}

	private Interactive getBank() {
		Filter<Interactive> f = new Filter<Interactive>() {
			@Override
			public boolean accept(final Interactive interactive) {
				return interactive.isOnScreen();
			}
		};

		List<Interactive> interactives = new ArrayList<>();
		ctx.npcs.select().id(BANK_NPC_IDS).select(f).select(UNREACHABLE_FILTER).nearest().limit(1).addTo(interactives);
		List<GameObject> cache = new ArrayList<>();
		ctx.objects.select().addTo(cache);
		ctx.objects.id(BANK_BOOTH_IDS).select(f).select(UNREACHABLE_FILTER).nearest().limit(1).addTo(interactives);
		ctx.objects.select(cache).id(BANK_COUNTER_IDS).select(f).select(UNREACHABLE_FILTER).nearest().limit(1).addTo(interactives);
		ctx.objects.select(cache).id(BANK_CHEST_IDS).select(f).select(UNREACHABLE_FILTER).nearest().limit(1).addTo(interactives);

		if (interactives.isEmpty()) {
			return ctx.objects.getNil();
		}

		return interactives.get(Random.nextInt(0, interactives.size()));
	}

	public Locatable getNearest() {
		Locatable nearest = Tile.NIL;
		for (Npc npc : ctx.npcs.select().select(UNREACHABLE_FILTER).id(BANK_NPC_IDS).nearest().limit(1)) {
			nearest = npc;
		}
		Tile loc = ctx.players.local().getLocation();
		for (GameObject object : ctx.objects.select().select(UNREACHABLE_FILTER).
				id(BANK_BOOTH_IDS, BANK_COUNTER_IDS, BANK_CHEST_IDS).nearest().limit(1)) {
			if (loc.distanceTo(object) < loc.distanceTo(nearest)) {
				nearest = object;
			}
		}
		return nearest;
	}

	public boolean isPresent() {
		return getNearest() != Tile.NIL;
	}

	public boolean isOnScreen() {
		return getBank().isValid();
	}

	public boolean isOpen() {
		return ctx.widgets.get(WIDGET, COMPONENT_CONTAINER_ITEMS).isValid();
	}

	public boolean open() {
		if (isOpen()) {
			return true;
		}
		Interactive interactive = getBank();
		final int id;
		if (interactive.isValid()) {
			if (interactive instanceof Npc) {
				id = ((Npc) interactive).getId();
			} else if (interactive instanceof GameObject) {
				id = ((GameObject) interactive).getId();
			} else {
				id = -1;
			}
		} else {
			id = -1;
		}
		if (id == -1) {
			return false;
		}
		int index = -1;
		final int[][] ids = {BANK_NPC_IDS, BANK_BOOTH_IDS, BANK_CHEST_IDS, BANK_COUNTER_IDS};
		for (int i = 0; i < ids.length; i++) {
			Arrays.sort(ids[i]);
			if (Arrays.binarySearch(ids[i], id) >= 0) {
				index = i;
				break;
			}
		}
		if (index == -1) {
			return false;
		}
		final String[] actions = {"Bank", "Bank", null, "Bank"};
		final String[] options = {null, "Bank booth", null, "Counter"};
		if (actions[index] == null) {
			if (interactive.hover()) {
				sleep(50, 100);
			}
			actions[index] = ctx.menu.indexOf(Menu.filter("Open")) != -1 ? "Open" : ctx.menu.indexOf(Menu.filter("Use")) != -1 ? "Use" : null;
			if (actions[index] == null) {
				return false;
			}
		}
		if (interactive.interact(actions[index], options[index])) {
			final Widget bankPin = ctx.widgets.get(13);
			for (int i = 0; i < 20 && !isOpen() && !bankPin.isValid(); i++) {
				sleep(200, 300);
			}
		}
		return isOpen();
	}

	public boolean close(final boolean wait) {
		if (!isOpen()) {
			return true;
		}
		final Component c = ctx.widgets.get(WIDGET, COMPONENT_BUTTON_CLOSE);
		if (c == null) {
			return false;
		}
		if (c.isValid() && c.interact("Close")) {
			if (!wait) {
				return true;
			}
			final Timer t = new Timer(Random.nextInt(1000, 2000));
			while (t.isRunning() && isOpen()) {
				sleep(100);
			}
			return !isOpen();
		}
		return false;
	}

	public boolean close() {
		return close(true);
	}

	@Override
	protected List<Item> get() {
		final Component c = ctx.widgets.get(WIDGET, COMPONENT_CONTAINER_ITEMS);
		if (c == null || !c.isValid()) {
			return new ArrayList<>();
		}
		final Component[] components = c.getChildren();
		List<Item> items = new ArrayList<>(components.length);
		for (final Component i : components) {
			if (i.getItemId() != -1) {
				items.add(new Item(ctx, i));
			}
		}
		return items;
	}

	public Item getItemAt(final int index) {
		final Component c = ctx.widgets.get(WIDGET, COMPONENT_CONTAINER_ITEMS);
		if (c == null || !c.isValid()) {
			return null;
		}
		final Component i = c.getChild(index);
		if (i != null && i.getItemId() != -1) {
			return new Item(ctx, i);
		}
		return null;
	}

	public int indexOf(final int id) {
		final Component items = ctx.widgets.get(WIDGET, COMPONENT_CONTAINER_ITEMS);
		if (items == null || !items.isValid()) {
			return -1;
		}
		final Component[] comps = items.getChildren();
		for (int i = 0; i < comps.length; i++) {
			if (comps[i].getItemId() == id) {
				return i;
			}
		}
		return -1;
	}

	public int getCurrentTab() {
		return ((ctx.settings.get(SETTING_BANK_STATE) >>> 24) - 136) / 8;
	}

	public boolean setCurrentTab(final int index) {
		final Component c = ctx.widgets.get(WIDGET, 35 - (index * 2));
		if (c != null && c.isValid() && c.click(true)) {
			final Timer timer = new Timer(800);
			while (timer.isRunning() && getCurrentTab() != index) {
				sleep(15);
			}
			return getCurrentTab() == index;
		}
		return false;
	}

	public Item getTabItem(final int index) {
		final Component c = ctx.widgets.get(WIDGET, 82 - (index * 2));
		if (c != null && c.isValid()) {
			return new Item(ctx, c);
		}
		return null;
	}

	public boolean withdraw(int id, Amount amount) {
		return withdraw(id, amount.getValue());
	}

	public boolean withdraw(int id, int amount) {//TODO: anti pattern
		Item item = select().id(id).poll();
		final Component container = ctx.widgets.get(WIDGET, COMPONENT_CONTAINER_ITEMS);
		if (!item.isValid() || !container.isValid()) {
			return false;
		}

		final Component c = item.getComponent();
		Point p = c.getRelativeLocation();
		if (p.y == 0) {
			for (int i = 0; i < 5 && getCurrentTab() != 0; i++) {
				if (!setCurrentTab(0)) {
					sleep(100, 200);
				}
			}
		}
		if (c.getRelativeLocation().y == 0) {
			return false;
		}
		final Rectangle bounds = container.getViewportRect();
		final Component scroll = ctx.widgets.get(WIDGET, COMPONENT_SCROLL_BAR);
		if (scroll == null || bounds == null) {
			return false;
		}
		if (!bounds.contains(c.getBoundingRect())) {
			if (ctx.widgets.scroll(c, scroll, bounds.contains(ctx.mouse.getLocation()))) {
				sleep(200, 400);
			}
			if (!bounds.contains(c.getBoundingRect())) {
				return false;
			}
		}
		String action = "Withdraw-" + amount;
		if (amount == 0 ||
				(item.getStackSize() <= amount && amount != 1 && amount != 5 && amount != 10)) {
			action = "Withdraw-All";
		} else if (amount == -1 || amount == (item.getStackSize() - 1)) {
			action = "Withdraw-All but one";
		}

		final int inv = ctx.backpack.getMoneyPouch() + ctx.backpack.select().count(true);
		if (containsAction(c, action)) {
			if (!c.interact(action)) {
				return false;
			}
		} else {
			if (!c.interact("Withdraw-X")) {
				return false;
			}
			for (int i = 0; i < 20 && !isInputWidgetOpen(); i++) {
				sleep(100, 200);
			}
			if (!isInputWidgetOpen()) {
				return false;
			}
			sleep(200, 800);
			ctx.keyboard.sendln(amount + "");
		}
		for (int i = 0; i < 25 && ctx.backpack.getMoneyPouch() + ctx.backpack.select().count(true) == inv; i++) {
			sleep(100, 200);
		}
		return ctx.backpack.getMoneyPouch() + ctx.backpack.select().count(true) != inv;
	}

	public boolean deposit(int id, Amount amount) {
		return deposit(id, amount.getValue());
	}

	public boolean deposit(final int id, final int amount) {
		Item item = ctx.backpack.select().id(id).shuffle().poll();
		if (!isOpen() || amount < 0 || !item.isValid()) {
			return false;
		}

		String action = "Deposit-" + amount;
		final int c = ctx.backpack.select().id(item.getId()).count(true);
		if (c == 1) {
			action = "Deposit";
		} else if (c <= amount || amount == 0) {
			action = "Deposit-All";
		}

		final Component comp = item.getComponent();
		final int inv = ctx.backpack.select().count(true);
		if (containsAction(comp, action)) {
			if (!comp.interact(action)) {
				return false;
			}
		} else {
			if (!comp.interact("Withdraw-X")) {
				return false;
			}
			for (int i = 0; i < 20 && !isInputWidgetOpen(); i++) {
				sleep(100, 200);
			}
			if (!isInputWidgetOpen()) {
				return false;
			}
			sleep(200, 800);
			ctx.keyboard.sendln(amount + "");
		}
		for (int i = 0; i < 25 && ctx.backpack.select().count(true) == inv; i++) {
			sleep(100, 200);
		}
		return ctx.backpack.select().count(true) != inv;
	}

	public boolean depositInventory() {
		final Component c = ctx.widgets.get(WIDGET, COMPONENT_BUTTON_DEPOSIT_INVENTORY);
		if (c == null || !c.isValid()) {
			return false;
		}
		if (ctx.backpack.select().isEmpty()) {
			return true;
		}
		return c.click();
	}

	public boolean depositEquipment() {
		final Component c = ctx.widgets.get(WIDGET, COMPONENT_BUTTON_DEPOSIT_EQUIPMENT);
		return c != null && c.isValid() && c.click();
	}

	public boolean depositFamiliar() {
		final Component c = ctx.widgets.get(WIDGET, COMPONENT_BUTTON_DEPOSIT_FAMILIAR);
		return c != null && c.isValid() && c.click();
	}

	public boolean setWithdrawMode(final boolean noted) {
		if (isWithdrawModeNoted() != noted) {
			final Component c = ctx.widgets.get(WIDGET, COMPONENT_BUTTON_WITHDRAW_MODE);
			if (c != null && c.isValid() && c.click(true)) {
				for (int i = 0; i < 20 && isWithdrawModeNoted() != noted; i++) {
					sleep(100, 200);
				}
			}
		}
		return isWithdrawModeNoted() == noted;
	}

	public boolean isWithdrawModeNoted() {
		return ctx.settings.get(SETTING_WITHDRAW_MODE) == 0x1;
	}

	private boolean containsAction(final Component c, String action) {
		action = action.toLowerCase();
		final String[] actions = c.getActions();
		if (action == null) {
			return false;
		}
		for (final String a : actions) {
			if (a != null && a.toLowerCase().contains(action)) {
				return true;
			}
		}
		return false;
	}

	private boolean isInputWidgetOpen() {
		final Component child = ctx.widgets.get(1469, 1);
		return child != null && child.isVisible();
	}

	@Override
	public Item getNil() {
		return new Item(ctx, -1, -1, null);
	}

	public static enum Amount {
		ONE(1), FIVE(5), TEN(10), ALL_BUT_ONE(-1), ALL(0);

		private final int value;

		private Amount(final int value) {
			this.value = value;
		}

		public int getValue() {
			return value;
		}
	}
}
