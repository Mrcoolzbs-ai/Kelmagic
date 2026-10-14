package dev.kelmagic;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;
import dev.kelmagic.core.KelLexicon;
import dev.kelmagic.network.SpellNetwork;
import dev.kelmagic.server.MagicAttachments;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.registries.DeferredRegister;

@Mod(KelMagic.MODID)
public class KelMagic {
    public static final String MODID = "kelmagic";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
    public KelMagic(IEventBus modEventBus, ModContainer modContainer) {
        MagicContent.register(modEventBus);
        MagicArchive.RECIPES.register(modEventBus);MagicParticles.TYPES.register(modEventBus);
        BLOCKS.register(modEventBus); ITEMS.register(modEventBus);
        MagicAttachments.TYPES.register(modEventBus);
        MagicSounds.SOUNDS.register(modEventBus);
        modEventBus.addListener(SpellNetwork::register);
        modContainer.registerConfig(ModConfig.Type.SERVER,Config.SPEC);
        LOGGER.info("Kel Magic: {} verified magical words",KelLexicon.WORDS.size());
    }
}
