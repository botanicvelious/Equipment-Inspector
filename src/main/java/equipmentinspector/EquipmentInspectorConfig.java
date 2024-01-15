package equipmentinspector;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("equipmentinspector")
public interface EquipmentInspectorConfig extends Config
{
    @ConfigItem(
            keyName = "holdShift",
            name = "Hold Shift",
            description = "Configures whether you need to hold shift to inspect equipment",
            position = 1
    )
    default boolean holdShift()
    {
        return false;
    }
}
