package dev.upgrader;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class UpgradeMenu extends AbstractContainerMenu {
    public final SimpleContainer input = new SimpleContainer(1);
    private final ContainerData state = new SimpleContainerData(9);
    private final Player owner;
    private long nextRoll = 0;
    public UpgradeMenu(int id, Inventory inv) {
        super(Upgrader.MENU.get(),id); owner=inv.player;
        addSlot(new Slot(input,0,34,63) {
            @Override public boolean mayPlace(ItemStack s) {
                return s.getItem()!=Upgrader.DEVICE.get() && !s.has(DataComponents.CONTAINER) && !s.has(DataComponents.BUNDLE_CONTENTS);
            }
        });
        for(int row=0;row<3;row++) for(int col=0;col<9;col++) addSlot(new Slot(inv,col+row*9+9,16+col*18,260+row*18));
        for(int col=0;col<9;col++) addSlot(new Slot(inv,col,16+col*18,318));
        addDataSlots(state);
    }
    public int state(int n){return state.get(n);}
    public Item target(){return BuiltInRegistries.ITEM.byId(state.get(0)|(state.get(1)<<16));}
    private boolean busy(){return owner.level().getGameTime()<nextRoll;}
    public void select(ResourceLocation id) {
        if(owner.level().isClientSide()||busy()||!BuiltInRegistries.ITEM.containsKey(id)) return;
        Item item=BuiltInRegistries.ITEM.get(id);
        if(Prices.get(item)<=0) return;
        int numeric=BuiltInRegistries.ITEM.getId(item);
        state.set(0,numeric&65535);state.set(1,numeric>>>16);broadcastChanges();
    }
    @Override public void broadcastChanges() {
        if(!owner.level().isClientSide()) {
            state.set(2,Prices.get(input.getItem(0).getItem()));
            state.set(3,Prices.get(target()));
            state.set(6,busy()?1:0);
        }
        super.broadcastChanges();
    }
    @Override public boolean clickMenuButton(Player player,int button) {
        if(player!=owner||player.level().isClientSide()||button!=0||busy()) return false;
        ItemStack stack=input.getItem(0);
        int a=Prices.get(stack.getItem()),b=Prices.get(target());
        if(stack.isEmpty()||a<=0||b<=a||stack.getItem()==Upgrader.DEVICE.get()||stack.has(DataComponents.CONTAINER)||stack.has(DataComponents.BUNDLE_CONTENTS)) return false;
        double chance=.8D*a/b;
        state.set(7,a);state.set(8,b);
        boolean won=player.getRandom().nextDouble()<chance;
        input.removeItem(0,1);
        nextRoll=player.level().getGameTime()+80;
        state.set(4,(state.get(4)+1)%30000);
        state.set(5,won?1:0);
        if(won) {
            ItemStack result=new ItemStack(target());
            if(!player.getInventory().add(result))player.drop(result,false);
        }
        broadcastChanges();return true;
    }
    @Override public boolean stillValid(Player p){return true;}
    @Override public void removed(Player p){super.removed(p);if(!p.level().isClientSide())clearContainer(p,input);}
    @Override public ItemStack quickMoveStack(Player p,int index) {
        Slot slot=slots.get(index);if(!slot.hasItem())return ItemStack.EMPTY;
        ItemStack stack=slot.getItem(),copy=stack.copy();
        if(index==0){if(!moveItemStackTo(stack,1,37,true))return ItemStack.EMPTY;}
        else {if(!slots.get(0).mayPlace(stack)||!moveItemStackTo(stack,0,1,false))return ItemStack.EMPTY;}
        if(stack.isEmpty())slot.setByPlayer(ItemStack.EMPTY);else slot.setChanged();
        slot.onTake(p,stack);return copy;
    }
}
