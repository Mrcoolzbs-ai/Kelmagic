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
        dev.kelmagic.worldgen.ArchiveWorldgen.TYPES.register(modEventBus);MagicStudy.register(modEventBus);MagicLamps.init();MagicCopper.init();dev.kelmagic.research.Research.init();MagicPortals.init();MagicMaterials.init();dev.kelmagic.content.MagicGrove.init();dev.kelmagic.content.MagicDreams.init(modEventBus);dev.kelmagic.content.ArchiveStorage.init();dev.kelmagic.content.InkDrawings.init();MagicResonance.init();MagicMascot.init();MagicApprovedContent.init();dev.kelmagic.artifact.ArtifactContent.init(modEventBus);MagicContent.register(modEventBus);
        MagicCreatures.TYPES.register(modEventBus);
        MagicAudio.register(modEventBus);
        MagicArchive.RECIPES.register(modEventBus);MagicParticles.TYPES.register(modEventBus);
        BLOCKS.register(modEventBus); ITEMS.register(modEventBus);
        MagicAttachments.TYPES.register(modEventBus);
        MagicSounds.SOUNDS.register(modEventBus);
        modEventBus.addListener(SpellNetwork::register);
        modContainer.registerConfig(ModConfig.Type.SERVER,Config.SPEC);modContainer.registerConfig(ModConfig.Type.CLIENT,MageClientConfig.SPEC);
        dev.kelmagic.compat.MonocleAccessories.init(modEventBus);
        LOGGER.info("Kel Magic: {} verified magical words",KelLexicon.WORDS.size());
    }
}
