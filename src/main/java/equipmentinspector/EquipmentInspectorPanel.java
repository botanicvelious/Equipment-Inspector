package equipmentinspector;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ItemComposition;
import net.runelite.api.ItemID;
import net.runelite.api.kit.KitType;
import net.runelite.client.util.AsyncBufferedImage;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import static net.runelite.api.ItemID.*;

@Slf4j
@Singleton
public class EquipmentInspectorPanel extends PluginPanel
{
    private final static String NO_PLAYER_SELECTED = "No player selected";

    private GridBagConstraints c;
    private JPanel equipmentPanels;
    private JPanel header;
    private JLabel nameLabel;
    private int itemid;

    @Inject
    private ItemManager itemManager;

    public EquipmentInspectorPanel()
    {
        GroupLayout layout = new GroupLayout(this);
        setLayout(layout);
        setBorder(new EmptyBorder(10, 10, 10, 10));
        setBackground(ColorScheme.DARK_GRAY_COLOR);

        equipmentPanels = new JPanel(new GridBagLayout());
        c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        c.gridx = 0;
        c.gridy = 0;

        header = new JPanel();
        header.setLayout(new BorderLayout());
        header.setBorder(new CompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(58, 58, 58)),
                BorderFactory.createEmptyBorder(0, 0, 10, 0)));

        nameLabel = new JLabel(NO_PLAYER_SELECTED);
        nameLabel.setForeground(Color.WHITE);

        header.add(nameLabel, BorderLayout.CENTER);

        layout.setHorizontalGroup(layout.createParallelGroup()
                .addComponent(equipmentPanels)
                .addComponent(header)
        );
        layout.setVerticalGroup(layout.createSequentialGroup()
                .addComponent(header)
                .addGap(10)
                .addComponent(equipmentPanels)
        );

        update(new HashMap<>(), "");
    }

    public void update(Map<KitType, ItemComposition> playerEquipment, String playerName)
    {
        if (playerName.isEmpty() || playerName == null)
        {
            nameLabel.setText(NO_PLAYER_SELECTED);
        }
        else
        {
            nameLabel.setText("Player: " + playerName);
        }

        SwingUtilities.invokeLater(() ->
                {
                    equipmentPanels.removeAll();
                    playerEquipment.forEach((kitType, itemComposition) ->
                    {
                        switch(itemComposition.getId()) {
                            case ABYSSAL_BRACELET1:
                            case ABYSSAL_BRACELET2:
                            case ABYSSAL_BRACELET3:
                            case ABYSSAL_BRACELET4:
                                itemid = ABYSSAL_BRACELET5;
                                break;
                            case BURNING_AMULET1:
                            case BURNING_AMULET2:
                            case BURNING_AMULET3:
                            case BURNING_AMULET4:
                                itemid = BURNING_AMULET5;
                                break;
                            case COMBAT_BRACELET:
                            case COMBAT_BRACELET1:
                            case COMBAT_BRACELET2:
                            case COMBAT_BRACELET3:
                            case COMBAT_BRACELET4:
                            case COMBAT_BRACELET5:
                               itemid = COMBAT_BRACELET6;
                                break;
                            case DIGSITE_PENDANT_1:
                            case DIGSITE_PENDANT_2:
                            case DIGSITE_PENDANT_3:
                            case DIGSITE_PENDANT_4:
                                itemid = DIGSITE_PENDANT_5;
                                break;
                            case AMULET_OF_GLORY1:
                            case AMULET_OF_GLORY2:
                            case AMULET_OF_GLORY3:
                            case AMULET_OF_GLORY4:
                            case AMULET_OF_GLORY5:
                            case AMULET_OF_GLORY:
                                itemid = AMULET_OF_GLORY6;
                                break;
                            case AMULET_OF_GLORY_T1:
                            case AMULET_OF_GLORY_T2:
                            case AMULET_OF_GLORY_T3:
                            case AMULET_OF_GLORY_T4:
                            case AMULET_OF_GLORY_T5:
                            case AMULET_OF_GLORY_T:
                                itemid = AMULET_OF_GLORY_T6;
                                break;
                            case RING_OF_WEALTH:
                            case RING_OF_WEALTH_1:
                            case RING_OF_WEALTH_2:
                            case RING_OF_WEALTH_3:
                            case RING_OF_WEALTH_4:
                                itemid = RING_OF_WEALTH_5;
                                break;
                            default:
                                itemid = itemComposition.getId();
                        }
                        AsyncBufferedImage itemImage = itemManager.getImage(itemid);
                        equipmentPanels.add(new ItemPanel(itemComposition, kitType, itemImage), c);
                        c.gridy++;

                    });
                    header.revalidate();
                    header.repaint();
                }
        );
    }
}