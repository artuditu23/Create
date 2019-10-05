package com.simibubi.create.modules.logistics.management.base;

import java.util.UUID;

import com.simibubi.create.Create;
import com.simibubi.create.foundation.block.SyncedTileEntity;
import com.simibubi.create.foundation.utility.ColorHelper;
import com.simibubi.create.modules.logistics.management.LogisticalNetwork;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

public abstract class LogisticalControllerTileEntity extends SyncedTileEntity
		implements Comparable<LogisticalControllerTileEntity>, ITickableTileEntity {

	public static final int COOLDOWN = 20;
	
	protected Priority priority = Priority.LOW;
	protected LogisticalNetwork network;
	protected String name = "";
	protected UUID networkId;
	protected boolean initialize;
	protected boolean checkTasks;
	protected int taskCooldown;

	public LogisticalControllerTileEntity(TileEntityType<?> tileEntityTypeIn) {
		super(tileEntityTypeIn);
		initialize = true;
	}

	@Override
	public void tick() {
		if (initialize) {
			initialize = false;
			initialize();
			return;
		}
		
		if (taskCooldown > 0)
			taskCooldown--;
	}

	protected void initialize() {
		if (networkId != null)
			handleAdded();
	}

	@Override
	public void remove() {
		if (networkId != null)
			handleRemoved();
		super.remove();
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}

	@Override
	public boolean hasFastRenderer() {
		return true;
	}
	
	public void notifyTaskUpdate() {
		checkTasks = true;
	}

	@Override
	public CompoundNBT write(CompoundNBT compound) {
		if (networkId != null)
			compound.putUniqueId("NetworkID", networkId);
		compound.putString("Address", name);
		return super.write(compound);
	}

	public UUID getNetworkId() {
		return networkId;
	}

	@Override
	public void read(CompoundNBT compound) {
		if (compound.contains("NetworkIDLeast"))
			networkId = compound.getUniqueId("NetworkID");
		name = compound.getString("Address");
		super.read(compound);
	}

	public int getColor() {
		return colorFromUUID(networkId);
	}

	public static int colorFromUUID(UUID uuid) {
		if (uuid == null)
			return 0x333333;
		int rainbowColor = ColorHelper.rainbowColor((int) uuid.getLeastSignificantBits());
		return ColorHelper.mixColors(rainbowColor, 0xFFFFFF, .5f);
	}

	public <T> LazyOptional<T> getCasingCapability(Capability<T> cap, Direction side) {
		return LazyOptional.empty();
	}

	public void setNetworkId(UUID uniqueId) {
		if (getNetwork() != null)
			handleRemoved();
		networkId = uniqueId;
		handleAdded();
		markDirty();
		sendData();
	}

	public void handleAdded() {
		if (world.isRemote)
			return;
		if (getNetwork() != null)
			return;
		network = Create.logisticalNetworkHandler.handleAdded(this);
	}

	public void handleRemoved() {
		if (world.isRemote)
			return;
		Create.logisticalNetworkHandler.handleRemoved(this);
		network = null;
	}

	public boolean isSupplier() {
		return false;
	}

	public boolean isReceiver() {
		return false;
	}

	@Override
	public int compareTo(LogisticalControllerTileEntity o) {
		return this.priority.compareTo(o.priority);
	}

	public LogisticalNetwork getNetwork() {
		return network;
	}

	public static enum Priority {
		LOWEST, LOW, MEDIUM, HIGH, HIGHEST;
	}

}