package equipmentinspector;

import java.awt.image.BufferedImage;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import javax.imageio.ImageIO;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.swing.SwingUtilities;

import com.google.inject.Provides;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.ItemComposition;
import net.runelite.api.KeyCode;
import net.runelite.api.MenuAction;
import net.runelite.api.MenuEntry;
import net.runelite.api.Player;
import net.runelite.api.PlayerComposition;
import net.runelite.api.SpriteID;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.api.events.MenuOpened;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.kit.KitType;
import net.runelite.client.Notifier;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.menus.MenuManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;

@PluginDescriptor(
		name = "Equipment Inspector"
)

@Slf4j
@Singleton
public class EquipmentInspectorPlugin extends Plugin
{
	static final String CONFIG_GROUP = "equipmentinspector";
	private static final String INSPECT_EQUIPMENT = "Equipment";
	private static final String KICK_OPTION = "Kick";

	@Inject
	@Nullable
	private Client client;

	@Inject
	private Provider<MenuManager> menuManager;

	@Inject
	private ScheduledExecutorService executor;

	@Inject
	private ClientToolbar pluginToolbar;

	private NavigationButton navButton;
	private EquipmentInspectorPanel equipmentInspectorPanel;

	@Inject
	private Notifier notifier;

	@Inject
	private ItemManager itemManager;

	@Inject
	private EquipmentInspectorConfig config;
	@Inject
	private SpriteManager spriteManager;
	private boolean showMenuItem;
	@Provides
	EquipmentInspectorConfig getConfig(ConfigManager configManager)
	{
		return configManager.getConfig(EquipmentInspectorConfig.class);
	}

	private final Map<Integer, PlayerInfo> storedPlayers = new HashMap<>();

	@Override
	protected void startUp() throws Exception
	{
		equipmentInspectorPanel = injector.getInstance(EquipmentInspectorPanel.class);

		BufferedImage icon;
		synchronized (ImageIO.class)
		{
			icon = ImageIO.read(getClass().getResourceAsStream("normal.png"));
		}

		navButton = NavigationButton.builder()
				.tooltip("Equipment Inspector")
				.icon(icon)
				.priority(5)
				.panel(equipmentInspectorPanel)
				.build();
	}

	@Override
	protected void shutDown() throws Exception
	{
		menuManager.get().removePlayerMenuItem(INSPECT_EQUIPMENT);
		pluginToolbar.removeNavigation(navButton);
	}
	@Subscribe
	public synchronized void onMenuEntryAdded(MenuEntryAdded event)
	{
		if (!config.holdShift() || client.isKeyPressed(KeyCode.KC_SHIFT)) {
			if (!showMenuItem) {
				menuManager.get().addPlayerMenuItem(INSPECT_EQUIPMENT);
			}
			showMenuItem = true;
		} else {
			menuManager.get().removePlayerMenuItem(INSPECT_EQUIPMENT);
			showMenuItem = false;
		}
	}
	@Subscribe
	public void onMenuOpened(MenuOpened event)
	{
		Stream.of(event.getMenuEntries()).map(MenuEntry::getActor)
				.filter(a -> a instanceof Player)
				.map(Player.class::cast)
				.distinct()
				.map(p -> new PlayerInfo(p. getId(), p.getName(), p.getPlayerComposition()))
				.forEach(playerInfo -> storedPlayers.put(playerInfo.getId(), playerInfo));
	}
	@Subscribe
	public void onMenuOptionClicked(MenuOptionClicked event)
	{
		equipmentInspectorPanel.setMembersImage(spriteManager.getSprite(SpriteID.WORLD_SWITCHER_STAR_MEMBERS, 0));
		if (event.getMenuAction() == MenuAction.RUNELITE_PLAYER && event.getMenuOption().equals(INSPECT_EQUIPMENT))
		{
			pluginToolbar.addNavigation(navButton);
			try
			{
				SwingUtilities.invokeAndWait(() -> pluginToolbar.openPanel(navButton));
			}
			catch (InterruptedException | InvocationTargetException e)
			{
				throw new RuntimeException(e);
			}
			PlayerInfo p = getPlayerInfo(event.getId());
			if (p == null)
			{
				return;
			}

			Map<KitType, ItemComposition> playerEquipment = new HashMap<>();
			Map<KitType, Integer> equipmentPrices = new HashMap<>();

			for (KitType kitType : KitType.values())
			{
				int itemId = p.getPlayerComposition().getEquipmentId(kitType);
				if (itemId != -1)
				{
					ItemComposition itemComposition = client.getItemDefinition(itemId);
					playerEquipment.put(kitType, itemComposition);
					equipmentPrices.put(kitType, itemManager.getItemPrice(itemId));
				}
			}
			equipmentInspectorPanel.update(playerEquipment, equipmentPrices, p.getName());
		}
		storedPlayers.clear();
	}

	private PlayerInfo getPlayerInfo(int id)
	{
		Player p = client.getCachedPlayers()[id];
		if (p != null)
		{
			return new PlayerInfo(p.getId(), p.getName(), p.getPlayerComposition());
		}
		else
		{
			return storedPlayers.getOrDefault(id, null);
		}
	}

	@Value
	private static class PlayerInfo
	{
		int id;
		String name;
		PlayerComposition playerComposition;
	}
}
