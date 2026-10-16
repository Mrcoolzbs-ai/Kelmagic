package dev.kelmagic.mixin;
import dev.kelmagic.worldgen.ArchiveSearch;import net.minecraft.commands.CommandSourceStack;import net.minecraft.commands.arguments.ResourceOrTagKeyArgument;import net.minecraft.world.level.levelgen.structure.Structure;import net.minecraft.server.commands.LocateCommand;import org.spongepowered.asm.mixin.Mixin;import org.spongepowered.asm.mixin.injection.*;import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(LocateCommand.class)public abstract class ArchiveCommandMixin {
 @Inject(method="locateStructure",at=@At("HEAD"),cancellable=true)private static void kelmagic$queued(CommandSourceStack s,ResourceOrTagKeyArgument.Result<Structure> arg,CallbackInfoReturnable<Integer> ci){if(ArchiveSearch.accepts(s,arg))ci.setReturnValue(ArchiveSearch.begin(s,arg));}
}
