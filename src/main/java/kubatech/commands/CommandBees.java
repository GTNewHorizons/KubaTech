/*
 * KubaTech - Gregtech Addon
 * Copyright (C) 2022  kuba6000
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
 *
 */

package kubatech.commands;

import com.google.common.io.Files;
import forestry.api.apiculture.IAlleleBeeSpecies;
import forestry.api.apiculture.IBee;
import forestry.api.apiculture.IBeeGenome;
import forestry.apiculture.genetics.Bee;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static forestry.api.apiculture.BeeManager.beeRoot;

public class CommandBees extends CommandBase {
    @Override
    public String getCommandName() {
        return "bees";
    }

    @Override
    public String getCommandUsage(ICommandSender p_71518_1_) {
        return "bees";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 4;
    }

    @Override
    public void processCommand(ICommandSender p_71515_1_, String[] p_71515_2_) {
        try {
            File f = new File("bees.txt");
            BufferedWriter writer = Files.newWriter(f, StandardCharsets.UTF_8);
            String delimer = ",";

            writer.write("Bee,OLD_0.6S_0UP,OLD_0.6S_8UP,OLD_1.7S_0UP,OLD_1.7S_8UP,NEW_0.6S_0UP_1T,NEW_0.6S_8UP_1T,NEW_1.7S_0UP_1T,NEW_1.7S_8UP_1T,NEW_1.7S_8UP_8T\n");

            List<IBee> bees = beeRoot.getIndividualTemplates();
            for(IBee bee : bees){
                //System.out.println("Bee: " + bee.getDisplayName());
                StringBuilder b = new StringBuilder(bee.getDisplayName());
                b.append(",-,-,-,-,-,-,-,-,-\n");
                IBeeGenome genome = bee.getGenome();
                IAlleleBeeSpecies primary = genome.getPrimary();
                IAlleleBeeSpecies secondary = genome.getSecondary();
                primary.getProductChances().forEach((k, v) -> {
                    b.append("[PRIMARY]");
                    b.append(k.toString());
                    b.append(delimer);
                    b.append(format(productChanceOld(0, 0.6d, v)));
                    b.append(delimer);
                    b.append(format(productChanceOld(8, 0.6d, v)));
                    b.append(delimer);
                    b.append(format(productChanceOld(0, 1.7d, v)));
                    b.append(delimer);
                    b.append(format(productChanceOld(8, 1.7d, v)));
                    b.append(delimer);
                    b.append(format(productChanceNew(0, 0.6d, v, 1)));
                    b.append(delimer);
                    b.append(format(productChanceNew(8, 0.6d, v, 1)));
                    b.append(delimer);
                    b.append(format(productChanceNew(0, 1.7d, v, 1)));
                    b.append(delimer);
                    b.append(format(productChanceNew(8, 1.7d, v, 1)));
                    b.append(delimer);
                    b.append(format(productChanceNew(8, 1.7d, v, 8)));
                    b.append("\n");
                });
                secondary.getProductChances().forEach((k, v) -> {
                    b.append("[SECONDARY]");
                    b.append(k.toString());
                    b.append(delimer);
                    b.append(format(productChanceOld(0, 0.6d, v)));
                    b.append(delimer);
                    b.append(format(productChanceOld(8, 0.6d, v)));
                    b.append(delimer);
                    b.append(format(productChanceOld(0, 1.7d, v)));
                    b.append(delimer);
                    b.append(format(productChanceOld(8, 1.7d, v)));
                    b.append(delimer);
                    b.append(format(productChanceNew(0, 0.6d, v, 1)));
                    b.append(delimer);
                    b.append(format(productChanceNew(8, 0.6d, v, 1)));
                    b.append(delimer);
                    b.append(format(productChanceNew(0, 1.7d, v, 1)));
                    b.append(delimer);
                    b.append(format(productChanceNew(8, 1.7d, v, 1)));
                    b.append(delimer);
                    b.append(format(productChanceNew(8, 1.7d, v, 8)));
                    b.append("\n");
                });
                primary.getSpecialtyChances().forEach((k, v) -> {
                    b.append("[SPECIALITY]");
                    b.append(k.toString());
                    b.append(delimer);
                    b.append(format(productChanceOld(0, 0.6d, v)));
                    b.append(delimer);
                    b.append(format(productChanceOld(8, 0.6d, v)));
                    b.append(delimer);
                    b.append(format(productChanceOld(0, 1.7d, v)));
                    b.append(delimer);
                    b.append(format(productChanceOld(8, 1.7d, v)));
                    b.append(delimer);
                    b.append(format(productChanceNew(0, 0.6d, v, 1)));
                    b.append(delimer);
                    b.append(format(productChanceNew(8, 0.6d, v, 1)));
                    b.append(delimer);
                    b.append(format(productChanceNew(0, 1.7d, v, 1)));
                    b.append(delimer);
                    b.append(format(productChanceNew(8, 1.7d, v, 1)));
                    b.append(delimer);
                    b.append(format(productChanceNew(8, 1.7d, v, 8)));
                    b.append("\n");
                });
                writer.write(b.toString());
            }

            writer.flush();
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String format(double chance){
        return String.format("%.2f%%", chance*100d);
    }

    private double productChanceNew(int upgradeCount, double beeSpeed, double chance, int t){
        chance *= 100f;
        float productionModifier = (float)upgradeCount * 0.25f;
        return (float) (((1f + t / 6f) * Math.sqrt(chance) * 2f * (1f + beeSpeed)
            + Math.pow(productionModifier, Math.cbrt(chance))
            - 3f)
            / 100f);
    }

    private double productChanceOld(int upgradeCount, double beeSpeed, double chance){
        return chance * beeSpeed * Math.pow(1.2d, upgradeCount);
    }
}
