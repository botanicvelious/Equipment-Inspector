package equipmentinspector;

import net.runelite.api.ItemComposition;
import net.runelite.api.kit.KitType;
import net.runelite.client.util.AsyncBufferedImage;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.util.LinkBrowser;
import net.runelite.client.util.QuantityFormatter;
import okhttp3.HttpUrl;
import org.apache.commons.lang3.StringUtils;

import javax.swing.GroupLayout;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.border.EmptyBorder;
import java.awt.Color;
import java.text.NumberFormat;
import java.util.Locale;

class ItemPanel extends JPanel
{

    ItemPanel(ItemComposition item, KitType kitType, AsyncBufferedImage icon, Integer itemPrice)
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
        JLabel price = getPriceLabel(itemPrice);

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
    public static JLabel getPriceLabel(long amount) {
        String itemPriceString = QuantityFormatter.quantityToStackSize(amount);
        JLabel price = new JLabel(StringUtils.capitalize(itemPriceString));
        if (amount > 10000000) {
            price.setForeground(Color.GREEN);
        } else if (amount > 100000) {
            price.setForeground(Color.WHITE);
        } else if (amount == 0) {
            price.setForeground(Color.LIGHT_GRAY);
        } else {
            price.setForeground(Color.YELLOW);
        }
        price.setFont(FontManager.getRunescapeFont());
        price.setToolTipText(NumberFormat.getNumberInstance(Locale.US).format(amount));
        return price;
    }
}
class TotalPanel extends JPanel {

    TotalPanel(long total) {
        setBorder(new EmptyBorder(3, 3, 3, 3));
        setBackground(ColorScheme.DARK_GRAY_COLOR);

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);

        JLabel name = new JLabel("Total: ");
        JLabel price = ItemPanel.getPriceLabel(total);
        name.setFont(FontManager.getRunescapeFont());

        layout.setVerticalGroup(layout.createParallelGroup()
                .addComponent(name)
                .addComponent(price)
        );
        layout.setHorizontalGroup(layout.createSequentialGroup()
                .addGap(8)
                .addComponent(name)
                .addComponent(price)
        );
    }
}
