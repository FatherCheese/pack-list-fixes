package cookie.packfix;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PackFixes implements ModInitializer {
    public static final String MOD_ID = "packfix";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Pack Fixes has been initialized.");
    }
}
