/*
 * Copyright (c) 2019-2025 Team Galacticraft
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

package dev.galacticraft.api.universe.display.ring;

import com.mojang.serialization.Codec;
import dev.galacticraft.api.registry.BuiltInAddonRegistries;
import dev.galacticraft.api.universe.celestialbody.CelestialBody;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphics;
import org.joml.Vector3f;

public record CelestialRingDisplay<C extends CelestialRingDisplayConfig, T extends CelestialRingDisplayType<C>>(T type, C config) {
    public static final Codec<CelestialRingDisplay<?, ?>> CODEC = BuiltInAddonRegistries.CELESTIAL_RING_DISPLAY_TYPE.byNameCodec().dispatch(CelestialRingDisplay::type, CelestialRingDisplayType::codec);

    @Environment(EnvType.CLIENT)
    public boolean render(CelestialBody<?, ?> body, GuiGraphics graphics, int count, Vector3f systemOffset, float alpha, float lineScale, double mouseX, double mouseY, float delta) {
        return this.type().render(body, graphics, count, systemOffset, lineScale, alpha, mouseX, mouseY, delta, this.config());
    }
}
