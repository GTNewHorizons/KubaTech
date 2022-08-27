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

package kubatech.loaders;

import static kubatech.api.enums.ItemList.*;

import cpw.mods.fml.common.registry.GameRegistry;
import kubatech.loaders.item.KubaItems;
import kubatech.loaders.item.items.Tea;
import kubatech.loaders.item.items.TeaCollection;
import kubatech.loaders.item.items.TeaUltimate;

public class ItemLoader {
    public static final KubaItems kubaitems = new KubaItems();

    public static void RegisterItems() {
        GameRegistry.registerItem(kubaitems, "kubaitems");
        LegendaryBlackTea.set(kubaitems.registerProxyItem(new TeaCollection("black_tea")));
        LegendaryButterflyTea.set(kubaitems.registerProxyItem(new TeaCollection("butterfly_tea")));
        LegendaryEarlGrayTea.set(kubaitems.registerProxyItem(new TeaCollection("earl_gray_tea")));
        LegendaryGreenTea.set(kubaitems.registerProxyItem(new TeaCollection("green_tea")));
        LegendaryLemonTea.set(kubaitems.registerProxyItem(new TeaCollection("lemon_tea")));
        LegendaryMilkTea.set(kubaitems.registerProxyItem(new TeaCollection("milk_tea")));
        LegendaryOolongTea.set(kubaitems.registerProxyItem(new TeaCollection("oolong_tea")));
        LegendaryPeppermintTea.set(kubaitems.registerProxyItem(new TeaCollection("peppermint_tea")));
        LegendaryPuerhTea.set(kubaitems.registerProxyItem(new TeaCollection("pu-erh_tea")));
        LegendaryRedTea.set(kubaitems.registerProxyItem(new TeaCollection("red_tea")));
        LegendaryWhiteTea.set(kubaitems.registerProxyItem(new TeaCollection("white_tea")));
        LegendaryYellowTea.set(kubaitems.registerProxyItem(new TeaCollection("yellow_tea")));
        LegendaryUltimateTea.set(kubaitems.registerProxyItem(new TeaUltimate()));
        BlackTea.set(kubaitems.registerProxyItem(new Tea("black_tea", 4, 0.3f)));
        ButterflyTea.set(kubaitems.registerProxyItem(new Tea("butterfly_tea", 4, 0.3f)));
        EarlGrayTea.set(kubaitems.registerProxyItem(new Tea("earl_gray_tea", 4, 0.3f)));
        GreenTea.set(kubaitems.registerProxyItem(new Tea("green_tea", 4, 0.3f)));
        LemonTea.set(kubaitems.registerProxyItem(new Tea("lemon_tea", 4, 0.3f)));
        MilkTea.set(kubaitems.registerProxyItem(new Tea("milk_tea", 4, 0.3f)));
        OolongTea.set(kubaitems.registerProxyItem(new Tea("oolong_tea", 4, 0.3f)));
        PeppermintTea.set(kubaitems.registerProxyItem(new Tea("peppermint_tea", 4, 0.3f)));
        PuerhTea.set(kubaitems.registerProxyItem(new Tea("pu-erh_tea", 4, 0.3f)));
        RedTea.set(kubaitems.registerProxyItem(new Tea("red_tea", 4, 0.3f)));
        WhiteTea.set(kubaitems.registerProxyItem(new Tea("white_tea", 4, 0.3f)));
        YellowTea.set(kubaitems.registerProxyItem(new Tea("yellow_tea", 4, 0.3f)));
    }
}
