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

import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.ItemComposition;
import net.runelite.api.MenuAction;
import net.runelite.api.MenuEntry;
import net.runelite.api.Player;
import net.runelite.api.PlayerComposition;
import net.runelite.api.events.MenuOpened;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.kit.KitType;
import net.runelite.client.Notifier;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;
import net.runelite.client.menus.MenuManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;


@PluginDescriptor(
		name = "Equipment Inspector",
		enabledByDefault = false
)

@Slf4j
@Singleton
public class EquipmentInspectorPlugin extends Plugin
{
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

	private final Map<Integer, PlayerInfo> storedPlayers = new HashMap<>();

	@Override
	protected void startUp() throws Exception
	{
		equipmentInspectorPanel = injector.getInstance(EquipmentInspectorPanel.class);
		menuManager.get().addPlayerMenuItem(INSPECT_EQUIPMENT);
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

		pluginToolbar.addNavigation(navButton);
	}

	@Override
	protected void shutDown() throws Exception
	{
		menuManager.get().removePlayerMenuItem(INSPECT_EQUIPMENT);
		pluginToolbar.removeNavigation(navButton);
	}

	@Subscribe
	public void onMenuOpened(MenuOpened event) {
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
		if (event.getMenuAction() == MenuAction.RUNELITE_PLAYER && event.getMenuOption().equals(INSPECT_EQUIPMENT))
		{
			try
			{
				SwingUtilities.invokeAndWait(() ->
				{
					if (!navButton.isSelected())
					{
						navButton.getOnSelect().run();
					}
				});
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
