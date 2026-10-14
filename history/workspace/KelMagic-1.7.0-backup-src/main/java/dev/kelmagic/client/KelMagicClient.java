package dev.kelmagic.client;

import dev.kelmagic.KelMagic;
import dev.kelmagic.network.SpellNetwork;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;

/** Physical-client-only entry point. Dedicated servers never link client classes. */
@Mod(value=KelMagic.MODID,dist=Dist.CLIENT)
public final class KelMagicClient {
    public KelMagicClient() { SpellNetwork.BURN_SINK=ArcaneFireVisuals::accept; SpellNetwork.CLIENT_SINK=ClientSpellState::accept; SpellNetwork.HEART_SINK=HeartEffects::activate; SpellNetwork.CODEX_SINK=CodexScreen::open; }
}
