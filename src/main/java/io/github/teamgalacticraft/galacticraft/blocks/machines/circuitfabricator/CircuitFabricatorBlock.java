package io.github.teamgalacticraft.galacticraft.blocks.machines.circuitfabricator;

import alexiil.mc.lib.attributes.AttributeList;
import alexiil.mc.lib.attributes.AttributeProvider;
import io.github.teamgalacticraft.galacticraft.container.GalacticraftContainers;
import io.github.teamgalacticraft.galacticraft.util.Rotatable;
import net.fabricmc.fabric.api.container.ContainerProviderRegistry;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.gui.Screen;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateFactory;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.text.Style;
import net.minecraft.text.TextComponent;
import net.minecraft.text.TextFormat;
import net.minecraft.text.TranslatableTextComponent;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

import java.util.List;

/**
 * @author <a href="https://github.com/teamgalacticraft">TeamGalacticraft</a>
 */
public class CircuitFabricatorBlock extends BlockWithEntity implements AttributeProvider, Rotatable {

    private static final DirectionProperty FACING = DirectionProperty.create("facing", Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST);

    public CircuitFabricatorBlock(Settings settings) {
        super(settings);
    }

    @Override
    public BlockRenderType getRenderType(BlockState blockState_1) {
        return BlockRenderType.MODEL;
    }

    @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.SOLID;
    }

    @Override
    public BlockEntity createBlockEntity(BlockView blockView) {
        return new CircuitFabricatorBlockEntity();
    }

    @Override
    public void appendProperties(StateFactory.Builder<Block, BlockState> stateBuilder) {
        super.appendProperties(stateBuilder);
        stateBuilder.with(FACING);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext context) {
        return this.getDefaultState().with(FACING, context.getPlayerHorizontalFacing().getOpposite());
    }

    @Override
    public boolean activate(BlockState blockState, World world, BlockPos blockPos, PlayerEntity playerEntity, Hand hand, BlockHitResult blockHitResult) {
        if (world.isClient) return true;
        ContainerProviderRegistry.INSTANCE.openContainer(GalacticraftContainers.CIRCUIT_FABRICATOR_CONTAINER, playerEntity, packetByteBuf -> packetByteBuf.writeBlockPos(blockPos));
        return true;
    }

    @Override
    public void addAllAttributes(World world, BlockPos pos, BlockState state, AttributeList<?> to) {
        Direction dir = to.getSearchDirection();
        if (dir != null) return;
        BlockEntity be = world.getBlockEntity(pos);
        if (!(be instanceof CircuitFabricatorBlockEntity)) return;
        CircuitFabricatorBlockEntity fabricator = (CircuitFabricatorBlockEntity) be;
        to.offer(fabricator.getEnergy());
        fabricator.getItems().offerSelfAsAttribute(to, null, null);
    }

    @Override
    public void buildTooltip(ItemStack itemStack, BlockView blockView, List<TextComponent> list, TooltipContext tooltipContext) {
        if (Screen.hasShiftDown()) {
            list.add(new TranslatableTextComponent("tooltip.galacticraft-rewoven.circuit_fabricator").setStyle(new Style().setColor(TextFormat.GRAY)));
        } else {
            list.add(new TranslatableTextComponent("tooltip.galacticraft-rewoven.press_shift").setStyle(new Style().setColor(TextFormat.GRAY)));
        }
    }

    @Override
    public void onBreak(World world, BlockPos blockPos, BlockState blockState, PlayerEntity playerEntity) {
        super.onBreak(world, blockPos, blockState, playerEntity);

        BlockEntity blockEntity = world.getBlockEntity(blockPos);

        if (blockEntity != null) {
            if (blockEntity instanceof CircuitFabricatorBlockEntity) {
                CircuitFabricatorBlockEntity circuitFabricatorBlockEntity = (CircuitFabricatorBlockEntity) blockEntity;

                for (int i = 0; i < circuitFabricatorBlockEntity.inventory.getSlotCount(); i++) {
                    ItemStack itemStack = circuitFabricatorBlockEntity.inventory.getInvStack(i);

                    if (itemStack != null) {
                        world.spawnEntity(new ItemEntity(world, blockPos.getX(), blockPos.getY() + 1, blockPos.getZ(), itemStack));
                    }
                }
            }
        }
    }
}
