package net.runelite.client.plugins.equipmentinspector;

import net.runelite.api.ItemComposition;
import net.runelite.api.kit.KitType;
import net.runelite.client.plugins.playerindicators.PlayerNameLocation;
import net.runelite.client.util.AsyncBufferedImage;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.util.LinkBrowser;
import okhttp3.HttpUrl;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

class ItemPanel extends JPanel
{

    ItemPanel(ItemComposition item, KitType kitType, AsyncBufferedImage icon, String itemPrice)
    {
        setBorder(new EmptyBorder(3, 3, 3, 3));
        setBackground(ColorScheme.DARK_GRAY_COLOR);

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);

        JLabel name = new JLabel(item.getName());

        JMenuItem wiki = new JMenuItem("Wiki");

        HttpUrl WIKI_BASE = HttpUrl.parse("https://oldschool.runescape.wiki");
        String UTM_SORUCE_KEY = "utm_source";
        String UTM_SORUCE_VALUE = "runelite";

        HttpUrl.Builder urlBuilder = WIKI_BASE.newBuilder();
        urlBuilder.addPathSegments("w/Special:Lookup")
                .addQueryParameter("type", "item")
                .addQueryParameter("id", "" + item.getId())
                .addQueryParameter("name", item.getName())
                .addQueryParameter(UTM_SORUCE_KEY, UTM_SORUCE_VALUE);

        HttpUrl url = urlBuilder.build();
        wiki.addActionListener(e-> LinkBrowser.browse(url.toString()));

        JLabel location = new JLabel(StringUtils.capitalize(kitType.toString().toLowerCase()));
        location.setFont(FontManager.getRunescapeSmallFont());

        JLabel price = new JLabel(StringUtils.capitalize(itemPrice.toLowerCase()));
        location.setFont(FontManager.getRunescapeSmallFont());

        JLabel imageLabel = new JLabel();
        icon.addTo(imageLabel);

        JPopupMenu popupMenu = new JPopupMenu();
        popupMenu.setBorder(new EmptyBorder(5, 5, 5, 5));
        popupMenu.add(wiki);

        layout.setVerticalGroup(layout.createParallelGroup()
                .addComponent(imageLabel)
                .addGroup(layout.createSequentialGroup()
                        .addComponent(name)
                        .addComponent(location)
                        .addComponent(price)
                )

        );

        layout.setHorizontalGroup(layout.createSequentialGroup()
                .addComponent(imageLabel)
                .addGap(8)
                .addGroup(layout.createParallelGroup()
                        .addComponent(name)
                        .addComponent(location)
                        .addComponent(price)
                )

        );


        // AWT's Z order is weird. This put image at the back of the stack
        setComponentZOrder(imageLabel, getComponentCount() - 1);
        setComponentPopupMenu(popupMenu);
    }

}
class TotalPanel extends JPanel {

    TotalPanel(AtomicInteger total) {
        setBorder(new EmptyBorder(3, 3, 3, 3));
        setBackground(ColorScheme.DARK_GRAY_COLOR);

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);

        JLabel name = new JLabel("Total: " + NumberFormat.getNumberInstance(Locale.US).format(total));
        name.setFont(FontManager.getRunescapeFont());

        layout.setVerticalGroup(layout.createParallelGroup()
                .addComponent(name)

        );
        layout.setHorizontalGroup(layout.createSequentialGroup()
                .addGap(8)
                .addComponent(name)

        );
    }
}
