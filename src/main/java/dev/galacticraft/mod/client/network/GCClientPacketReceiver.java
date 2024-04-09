/*
 * Copyright (c) 2019-2024 Team Galacticraft
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package dev.galacticraft.mod.client.network;

import dev.galacticraft.api.registry.AddonRegistries;
import dev.galacticraft.api.rocket.RocketData;
import dev.galacticraft.mod.Constant;
import dev.galacticraft.mod.Constant.Packet;
import dev.galacticraft.mod.attachments.GCClientPlayer;
import dev.galacticraft.mod.client.gui.screen.ingame.CelestialSelectionScreen;
import dev.galacticraft.mod.client.network.packets.FootprintPacket;
import dev.galacticraft.mod.client.network.packets.FootprintRemovedPacket;
import dev.galacticraft.mod.client.network.packets.ResetThirdPersonPacket;
import dev.galacticraft.mod.client.render.FootprintRenderer;
import dev.galacticraft.mod.content.block.entity.machine.OxygenBubbleDistributorBlockEntity;
import dev.galacticraft.mod.content.entity.orbital.RocketEntity;
import dev.galacticraft.mod.content.item.GCItems;
import dev.galacticraft.mod.misc.footprint.Footprint;
import dev.galacticraft.mod.misc.footprint.FootprintManager;
import dev.galacticraft.mod.network.GCScreenType;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Handles client-bound (S2C) packets
 * @author <a href="https://github.com/TeamGalacticraft">TeamGalacticraft</a>
 */
@Environment(EnvType.CLIENT)
public class GCClientPacketReceiver {
    public static void register() {
        ClientPlayNetworking.registerGlobalReceiver(Packet.BUBBLE_SIZE, (client, handler, buf, responseSender) -> {
            FriendlyByteBuf buffer = new FriendlyByteBuf(buf.copy());
            client.execute(() -> {
                BlockPos pos = buffer.readBlockPos();
                if (client.level.hasChunk(pos.getX() >> 4, pos.getZ() >> 4)) {
                    BlockEntity entity = client.level.getBlockEntity(pos);
                    if (entity instanceof OxygenBubbleDistributorBlockEntity machine) {
                        machine.setSize(buffer.readDouble());
                    }
                }
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(Packet.OPEN_SCREEN, (client, handler, buf, responseSender) -> {
            var screen = buf.readEnum(GCScreenType.class);
            switch (screen) {
                case CELESTIAL -> {
                    boolean mapMode = buf.readBoolean();
                    client.execute(() -> client.setScreen(new CelestialSelectionScreen(mapMode, RocketData.fromNbt(GCItems.ROCKET.getDefaultInstance().getTag()), true, null)));
                }
                default -> Constant.LOGGER.error("No screen found!");
            }
        });

        ClientPlayNetworking.registerGlobalReceiver(new ResourceLocation(Constant.MOD_ID, "planet_menu_open"), (minecraftClient, clientPlayNetworkHandler, buf, packetSender) -> {
            RocketData rocketData = RocketData.fromNbt(Objects.requireNonNull(buf.readNbt()));
            int cBody = buf.readInt();
            minecraftClient.execute(() -> minecraftClient.setScreen(new CelestialSelectionScreen(false, rocketData, true, cBody == -1 ? null : clientPlayNetworkHandler.registryAccess().registryOrThrow(AddonRegistries.CELESTIAL_BODY).getHolder(cBody).orElseThrow().value())));
        });

        ClientPlayNetworking.registerGlobalReceiver(new ResourceLocation(Constant.MOD_ID, "rocket_spawn"), ((client, handler, buf, responseSender) -> {
            EntityType<? extends RocketEntity> type = (EntityType<? extends RocketEntity>) BuiltInRegistries.ENTITY_TYPE.byId(buf.readVarInt());

            int entityID = buf.readVarInt();
            UUID entityUUID = buf.readUUID();

            double x = buf.readDouble();
            double y = buf.readDouble();
            double z = buf.readDouble();

            float pitch = (buf.readByte() * 360) / 256.0F;
            float yaw = (buf.readByte() * 360) / 256.0F;

            RocketData data = RocketData.fromNbt(buf.readNbt());

            client.execute(() -> {
                RocketEntity entity = type.create(client.level);
                assert entity != null;
                entity.syncPacketPositionCodec(x, y, z);
                entity.setPos(x, y, z);
                entity.setXRot(pitch);
                entity.setYRot(yaw);
                entity.setId(entityID);
                entity.setUUID(entityUUID);

                entity.setData(data);

                Minecraft.getInstance().level.addEntity(entity);
            });
        }));

        ClientPlayNetworking.registerGlobalReceiver(FootprintPacket.FOOTPRINT_PACKET, (packet, player, responseSender) -> {
            FootprintRenderer.setFootprints(packet.packedPos(), packet.footprints());
        });

        ClientPlayNetworking.registerGlobalReceiver(FootprintRemovedPacket.FOOTPRINT_REMOVED_PACKET, (packet, player, responseSender) -> {
            long packedPos = packet.packedPos();
            BlockPos pos = packet.pos();
            FootprintManager manager = player.level().galacticraft$getFootprintManager();
            List<Footprint> footprintList = manager.getFootprints().get(packedPos);
            List<Footprint> toRemove = new ArrayList<>();

            if (footprintList != null) {
                for (Footprint footprint : footprintList) {
                    if (footprint.position.x > pos.getX() && footprint.position.x < pos.getX() + 1 && footprint.position.z > pos.getZ() && footprint.position.z < pos.getZ() + 1) {
                        toRemove.add(footprint);
                    }
                }
            }

            if (!toRemove.isEmpty()) {
                footprintList.removeAll(toRemove);
                manager.getFootprints().put(packedPos, footprintList);
            }
        });

        ClientPlayNetworking.registerGlobalReceiver(ResetThirdPersonPacket.TYPE, (packet, player, responseSender) -> {
            Minecraft.getInstance().options.setCameraType(GCClientPlayer.get(player).getCameraType());
        });
    }
}
