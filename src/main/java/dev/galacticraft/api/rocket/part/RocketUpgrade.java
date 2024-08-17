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

package dev.galacticraft.api.rocket.part;

import com.mojang.serialization.Codec;
import dev.galacticraft.api.registry.BuiltInRocketRegistries;
import dev.galacticraft.api.registry.RocketRegistries;
import dev.galacticraft.api.rocket.part.config.RocketUpgradeConfig;
import dev.galacticraft.api.rocket.part.type.RocketUpgradeType;
import dev.galacticraft.impl.codec.StreamCodecs;
import dev.galacticraft.impl.rocket.part.RocketUpgradeImpl;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.world.item.EitherHolder;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public non-sealed interface RocketUpgrade<C extends RocketUpgradeConfig, T extends RocketUpgradeType<C>> extends RocketPart<C, T> {
    Codec<RocketUpgrade<?, ?>> DIRECT_CODEC = BuiltInRocketRegistries.ROCKET_UPGRADE_TYPE.byNameCodec().dispatch(RocketUpgrade::type, RocketUpgradeType::codec);
    Codec<Holder<RocketUpgrade<?, ?>>> CODEC = RegistryFileCodec.create(RocketRegistries.ROCKET_UPGRADE, DIRECT_CODEC);
    Codec<HolderSet<RocketUpgrade<?, ?>>> LIST_CODEC = RegistryCodecs.homogeneousList(RocketRegistries.ROCKET_UPGRADE, DIRECT_CODEC);
    StreamCodec<RegistryFriendlyByteBuf, Holder<RocketUpgrade<?, ?>>> STREAM_CODEC = StreamCodecs.ofHolder(RocketRegistries.ROCKET_UPGRADE);

    Codec<EitherHolder<RocketUpgrade<?, ?>>> EITHER_CODEC = EitherHolder.codec(RocketRegistries.ROCKET_UPGRADE, CODEC);
    StreamCodec<RegistryFriendlyByteBuf, EitherHolder<RocketUpgrade<?, ?>>> EITHER_STREAM_CODEC = EitherHolder.streamCodec(RocketRegistries.ROCKET_UPGRADE, STREAM_CODEC);

    @Contract(pure = true, value = "_, _ -> new")
    static @NotNull <C extends RocketUpgradeConfig, T extends RocketUpgradeType<C>> RocketUpgrade<C, T> create(@NotNull C config, @NotNull T type) {
        return new RocketUpgradeImpl<>(config, type);
    }
}
