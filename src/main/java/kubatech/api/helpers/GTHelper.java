/*
 * spotless:off
 * KubaTech - Gregtech Addon
 * Copyright (C) 2022 - 2023  kuba6000
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library. If not, see <https://www.gnu.org/licenses/>.
 * spotless:on
 */

package kubatech.api.helpers;

import static gregtech.api.metatileentity.implementations.GT_MetaTileEntity_MultiBlockBase.isValidMetaTileEntity;
import static kubatech.api.Variables.ln4;

import java.io.IOException;
import java.util.ArrayList;

import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;

import com.kuba6000.mobsinfo.api.utils.ItemID;

import gregtech.api.metatileentity.implementations.GT_MetaTileEntity_Hatch_Energy;
import gregtech.api.metatileentity.implementations.GT_MetaTileEntity_MultiBlockBase;
import kubatech.api.implementations.KubaTechGTMultiBlockBase;

public class GTHelper {

    public static long getMaxInputEU(GT_MetaTileEntity_MultiBlockBase mte) {
        if (mte instanceof KubaTechGTMultiBlockBase) return ((KubaTechGTMultiBlockBase<?>) mte).getMaxInputEu();
        long rEU = 0;
        for (GT_MetaTileEntity_Hatch_Energy tHatch : mte.mEnergyHatches)
            if (isValidMetaTileEntity(tHatch)) rEU += tHatch.maxEUInput() * tHatch.maxAmperesIn();
        return rEU;
    }

    public static double getVoltageTierD(long voltage) {
        return Math.log((double) voltage / 8L) / ln4;
    }

    public static double getVoltageTierD(GT_MetaTileEntity_MultiBlockBase mte) {
        return Math.log((double) getMaxInputEU(mte) / 8L) / ln4;
    }

    public static int getVoltageTier(long voltage) {
        return (int) getVoltageTierD(voltage);
    }

    public static int getVoltageTier(GT_MetaTileEntity_MultiBlockBase mte) {
        return (int) getVoltageTierD(mte);
    }

    public static class StackableGUISlot {

        public StackableGUISlot() {};

        public StackableGUISlot(int count, ItemStack stack, ArrayList<Integer> realSlots) {
            this.count = count;
            this.stack = stack;
            this.realSlots = realSlots;
        }

        public int count;
        public ItemStack stack;
        public ArrayList<Integer> realSlots = new ArrayList<>();

        public void write(PacketBuffer buffer) throws IOException {
            buffer.writeVarIntToBuffer(count);
            buffer.writeItemStackToBuffer(stack);
        }

        public static StackableGUISlot read(PacketBuffer buffer) throws IOException {
            StackableGUISlot slot = new StackableGUISlot();
            slot.count = buffer.readVarIntFromBuffer();
            slot.stack = buffer.readItemStackFromBuffer();
            return slot;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof StackableGUISlot)) return false;
            return count == ((StackableGUISlot) obj).count && ItemID.createNoCopy(stack, false)
                .hashCode()
                == ItemID.createNoCopy(((StackableGUISlot) obj).stack, false)
                    .hashCode()
                && realSlots.equals(((StackableGUISlot) obj).realSlots);
        }
    }
}
