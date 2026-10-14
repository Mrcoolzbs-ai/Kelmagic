package dev.kelmagic.mixin;
import java.util.concurrent.CompletableFuture;import net.minecraft.server.level.*;import net.minecraft.world.level.chunk.*;import net.minecraft.world.level.chunk.status.ChunkStatus;import org.spongepowered.asm.mixin.Mixin;import org.spongepowered.asm.mixin.gen.*;
@Mixin(ServerChunkCache.class)public interface ArchiveChunkAccess {
 @Invoker("getChunkFutureMainThread") CompletableFuture<ChunkResult<ChunkAccess>> kelmagic$request(int x,int z,ChunkStatus status,boolean create);
 @Accessor("distanceManager") DistanceManager kelmagic$tickets();
}
