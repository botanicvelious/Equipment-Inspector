package equipmentinspector;

import net.runelite.api.events.ChatMessage;
import net.runelite.client.events.ConfigChanged;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.api.events.PlayerMenuOptionClicked;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.ItemComposition;
import net.runelite.api.Player;
import net.runelite.api.kit.KitType;
import net.runelite.client.menus.MenuManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.util.Text;
import static net.runelite.api.ItemID.*;

import javax.annotation.Nullable;
import javax.imageio.ImageIO;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.ScheduledExecutorService;
import net.runelite.client.Notifier;
import net.runelite.client.eventbus.Subscribe;
import javax.inject.Provider;
import javax.swing.SwingUtilities;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.game.ItemVariationMapping;

import static net.runelite.api.ItemID.*;


@PluginDescriptor(
		name = "Equipment Inspector",
		enabledByDefault = false
)

@Slf4j
@Singleton
public class EquipmentInspectorPlugin extends Plugin
{
	private static final String INSPECT_EQUIPMENT = "Inspect Equipment";
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
	public void onPlayerMenuOptionClicked(PlayerMenuOptionClicked event)
	{
		if (event.getMenuOption().equals(INSPECT_EQUIPMENT))
		{
			executor.execute(() ->
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

				String playerName = Text.removeTags(event.getMenuTarget());
				// The player menu uses a non-breaking space in the player name, we need to replace this to compare
				// against the playerName in the player cache.
				String finalPlayerName = playerName.replace('\u00A0', ' ');

				List<Player> players = client.getPlayers();
				Optional<Player> targetPlayer = players.stream()
						.filter(Objects::nonNull)
						.filter(p -> p.getName().equals(finalPlayerName)).findFirst();

				if (targetPlayer.isPresent())
				{
					Player p = targetPlayer.get();
					Map<KitType, ItemComposition> playerEquipment = new HashMap<>();

					for (KitType kitType : KitType.values())
					{
						int itemId = p.getPlayerComposition().getEquipmentId(kitType);
						if (itemId != -1)
						{
							itemId = ItemVariationMapping.map(itemId);
							ItemComposition itemComposition = client.getItemDefinition(itemId);
							playerEquipment.put(kitType, itemComposition);
						}
					}
					final Widget wildernessLevelWidget = client.getWidget(WidgetInfo.PVP_WILDERNESS_LEVEL);
					if (wildernessLevelWidget == null)
					{
						equipmentInspectorPanel.update(playerEquipment, playerName);
					} else {
						equipmentInspectorPanel.nameLabel.setText("Can not check equipment in PVP areas.");
					}
				}
			});
		}
	}
}