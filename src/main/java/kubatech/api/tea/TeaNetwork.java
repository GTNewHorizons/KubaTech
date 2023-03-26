package kubatech.api.tea;

import java.math.BigInteger;
import java.util.UUID;

import kubatech.savedata.PlayerData;
import kubatech.savedata.PlayerDataManager;

import net.minecraft.nbt.NBTTagCompound;

public class TeaNetwork {

    // TODO: Optimize later :P
    public BigInteger teaAmount = BigInteger.ZERO;
    PlayerData owner;

    public static TeaNetwork getNetwork(UUID player) {
        PlayerData p = PlayerDataManager.getPlayer(player);
        if (p == null) return null;
        TeaNetwork n = p.teaNetwork;
        if (n == null) {
            p.teaNetwork = new TeaNetwork();
            p.teaNetwork.owner = p;
            return p.teaNetwork;
        }
        n.owner = p;
        return n;
    }

    public boolean canAfford(long price, boolean take) {
        return canAfford(BigInteger.valueOf(price), take);
    }

    public boolean canAfford(BigInteger price, boolean take) {
        if (teaAmount.compareTo(price) >= 0) {
            if (take) {
                teaAmount = teaAmount.subtract(price);
                markDirty();
            }
            return true;
        }
        return false;
    }

    public void addTea(long toAdd) {
        addTea(BigInteger.valueOf(toAdd));
    }

    public void addTea(BigInteger toAdd) {
        teaAmount = teaAmount.add(toAdd);
        markDirty();
    }

    public void markDirty() {
        owner.markDirty();
    }

    public NBTTagCompound toNBT() {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setByteArray("teaAmount", teaAmount.toByteArray());
        return nbt;
    }

    public static TeaNetwork fromNBT(NBTTagCompound nbt) {
        TeaNetwork teaNetwork = new TeaNetwork();
        teaNetwork.teaAmount = new BigInteger(nbt.getByteArray("teaAmount"));
        return teaNetwork;
    }
}
