package kubatech.savedata;

import net.minecraft.nbt.NBTTagCompound;

public class PlayerData {
    public long teaAmount = 0l;

    PlayerData(NBTTagCompound NBTData) {
        teaAmount = NBTData.getLong("teaAmount");
    }

    PlayerData() {}

    public NBTTagCompound toNBTData() {
        NBTTagCompound NBTData = new NBTTagCompound();
        NBTData.setLong("teaAmount", teaAmount);
        return NBTData;
    }

    public void markDirty() {
        PlayerDataManager.Instance.markDirty();
    }
}
