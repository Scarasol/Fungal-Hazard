package com.scarasol.fungalhazard.mixin;

import com.scarasol.fungalhazard.api.IFungalContainer;
import com.scarasol.sona.accessor.IBaseContainerBlockEntityAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * @author Scarasol
 */
@Mixin(BaseContainerBlockEntity.class)
public abstract class BaseContainerBlockEntityMixin extends BlockEntity implements IFungalContainer {

    @Unique
    private boolean fungalHazard$hasLurker;

    public BaseContainerBlockEntityMixin(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState) {
        super(blockEntityType, blockPos, blockState);
    }

    @Unique
    @Override
    public boolean hasLurker() {
        return fungalHazard$hasLurker;
    }

    @Unique
    @Override
    public void setHasLurker(boolean hasLurker) {
        this.fungalHazard$hasLurker = hasLurker;
    }

    @Inject(method = "saveAdditional", at = @At("RETURN"))
    private void fungalHazard$saveAdditional(CompoundTag compoundTag, CallbackInfo ci) {
        compoundTag.putBoolean("HasLurker", this.fungalHazard$hasLurker);
    }

    @Inject(method = "load", at = @At("RETURN"))
    private void fungalHazard$load(CompoundTag compoundTag, CallbackInfo ci) {
        if (this instanceof IBaseContainerBlockEntityAccessor blockEntityAccessor) {
            if (compoundTag.contains("HasLurker")) {
                this.fungalHazard$hasLurker = compoundTag.getBoolean("HasLurker");
            }else if (!blockEntityAccessor.isLocked() && compoundTag.contains("LootTable", 8)) {
                this.fungalHazard$hasLurker = true;
            }
        }
    }
}
