package kubatech.tileentity;

import com.gtnewhorizons.modularui.api.ModularUITextures;
import com.gtnewhorizons.modularui.api.drawable.Text;
import com.gtnewhorizons.modularui.api.math.Color;
import com.gtnewhorizons.modularui.api.screen.*;
import com.gtnewhorizons.modularui.common.builder.UIInfo;
import com.gtnewhorizons.modularui.common.internal.wrapper.ModularUIContainer;
import com.gtnewhorizons.modularui.common.widget.DynamicTextWidget;
import kubatech.api.enums.ItemList;
import kubatech.loaders.ItemLoader;
import kubatech.loaders.block.KubaBlock;
import kubatech.savedata.PlayerData;
import kubatech.savedata.PlayerDataManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

public class TeaAcceptorTile extends TileEntity
        implements IInventory, ITileWithModularUI, KubaBlock.IModularUIProvider {

    public TeaAcceptorTile() {
        super();
    }

    private String tileOwner = null;
    private PlayerData playerData = null;

    public void setTeaOwner(String teaOwner) {
        if (tileOwner == null || tileOwner.isEmpty()) {
            tileOwner = teaOwner;
            playerData = PlayerDataManager.getPlayer(tileOwner);
            markDirty();
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound NBTData) {
        tileOwner = NBTData.getString("tileOwner");
        if (!tileOwner.isEmpty()) {
            playerData = PlayerDataManager.getPlayer(tileOwner);
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound NBTData) {
        NBTData.setString("tileOwner", tileOwner);
    }

    @Override
    public boolean canUpdate() {
        return false;
    }

    @Override
    public int getSizeInventory() {
        return 10;
    }

    @Override
    public ItemStack getStackInSlot(int p_70301_1_) {
        return null;
    }

    @Override
    public ItemStack decrStackSize(int p_70298_1_, int p_70298_2_) {
        return null;
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int p_70304_1_) {
        return null;
    }

    @Override
    public void setInventorySlotContents(int p_70299_1_, ItemStack p_70299_2_) {
        if (playerData != null) {
            playerData.teaAmount += p_70299_2_.stackSize;
            playerData.markDirty();
        }
    }

    @Override
    public String getInventoryName() {
        return "Tea acceptor";
    }

    @Override
    public boolean hasCustomInventoryName() {
        return false;
    }

    @Override
    public int getInventoryStackLimit() {
        return 64;
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer p_70300_1_) {
        return p_70300_1_.getCommandSenderName().equals(tileOwner);
    }

    @Override
    public void openInventory() {}

    @Override
    public void closeInventory() {}

    private static final int minDamage = ItemList.BlackTea.get(1).getItemDamage();
    private static final int maxDamage = ItemList.YellowTea.get(1).getItemDamage();

    @Override
    public boolean isItemValidForSlot(int p_94041_1_, ItemStack p_94041_2_) {
        return p_94041_2_.getItem() == ItemLoader.kubaitems
                && p_94041_2_.getItemDamage() >= minDamage
                && p_94041_2_.getItemDamage() <= maxDamage;
    }

    private static final UIInfo<?, ?> UI = KubaBlock.TileEntityUIFactory.apply(ModularUIContainer::new);

    @Override
    public UIInfo<?, ?> getUI() {
        return UI;
    }

    @Override
    public ModularWindow createWindow(UIBuildContext buildContext) {
        ModularWindow.Builder builder = ModularWindow.builder(200, 150);
        builder.setBackground(ModularUITextures.VANILLA_BACKGROUND);
        EntityPlayer player = buildContext.getPlayer();

        DynamicTextWidget text = new DynamicTextWidget(() -> {
            if (player.getCommandSenderName().equals(tileOwner))
                return new Text("Tea: " + (playerData == null ? "ERROR" : playerData.teaAmount))
                        .color(Color.GREEN.normal);
            else return new Text("This is not your block").color(Color.RED.normal);
        });
        builder.widget(text.setPos(20, 20));
        return builder.build();
    }
}
