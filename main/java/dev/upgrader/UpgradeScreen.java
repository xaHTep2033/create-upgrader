package dev.upgrader;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import java.util.*;

public final class UpgradeScreen extends AbstractContainerScreen<UpgradeMenu> {
    private EditBox search;
    private Button roll;
    private List<Item> catalog=new ArrayList<>();
    private int page=0,seen=-1;
    private long started=-10000;
    private double shownChance=0,stopAngle=0;
    private boolean won=false;
    private static final int COLS=12,ROWS=3,SIZE=22,PER_PAGE=COLS*ROWS;
    public UpgradeScreen(UpgradeMenu menu,Inventory inv,Component title){
        super(menu,inv,title);imageWidth=300;imageHeight=344;
    }
    @Override protected void init(){
        super.init();
        search=new EditBox(font,leftPos+16,topPos+144,268,18,Component.literal("Поиск"));
        search.setMaxLength(100);search.setHint(Component.literal("Поиск: имя или mod:item"));
        search.setResponder(s->{page=0;filter(s);});addRenderableWidget(search);filter("");
        roll=addRenderableWidget(Button.builder(Component.literal("АПГРЕЙД"),b->{
            if(minecraft!=null&&minecraft.gameMode!=null)minecraft.gameMode.handleInventoryButtonClick(menu.containerId,0);
        }).bounds(leftPos+101,topPos+113,98,20).build());
        addRenderableWidget(Button.builder(Component.literal("<"),b->{if(page>0)page--;}).bounds(leftPos+16,topPos+234,22,18).build());
        addRenderableWidget(Button.builder(Component.literal(">"),b->{if((page+1)*PER_PAGE<catalog.size())page++;}).bounds(leftPos+262,topPos+234,22,18).build());
    }
    private void filter(String query){
        String q=query.toLowerCase(Locale.ROOT);
        catalog=BuiltInRegistries.ITEM.stream().filter(i->i!=Items.AIR&&i!=Upgrader.DEVICE.get()).filter(i->{
            String id=BuiltInRegistries.ITEM.getKey(i).toString();
            return !id.contains("creative")&&!id.contains("debug")&&!id.contains("incomplete")&&!id.endsWith("_spawn_egg")&&(id.contains(q)||i.getDescription().getString().toLowerCase(Locale.ROOT).contains(q));
        }).sorted(Comparator.comparing(i->BuiltInRegistries.ITEM.getKey(i).toString())).toList();
    }
    private double chance(){return menu.state(3)>menu.state(2)&&menu.state(2)>0?.8D*menu.state(2)/menu.state(3):0;}
    @Override public void render(GuiGraphics g,int mx,int my,float partial){
        if(seen<0)seen=menu.state(4);
        if(seen!=menu.state(4)){
            seen=menu.state(4);started=System.currentTimeMillis();won=menu.state(5)==1;
            shownChance=menu.state(8)>0?.8D*menu.state(7)/menu.state(8):0;
            double fraction=won?shownChance*.5:shownChance+(1-shownChance)*.5;
            stopAngle=fraction*Math.PI*2;
        }
        roll.active=menu.state(6)==0&&chance()>0;
        super.render(g,mx,my,partial);renderTooltip(g,mx,my);
        int cell=cellAt(mx,my);
        if(cell>=0&&page*PER_PAGE+cell<catalog.size()){
            Item item=catalog.get(page*PER_PAGE+cell);
            g.renderTooltip(font,List.of(item.getDescription(),Component.literal(BuiltInRegistries.ITEM.getKey(item).toString()),Component.literal("Нажми для выбора. Цена 0: выбор отклонён сервером.")),Optional.empty(),mx,my);
        }
    }
    @Override protected void renderBg(GuiGraphics g,float partial,int mx,int my){
        int x=leftPos,y=topPos;
        g.fill(x,y,x+imageWidth,y+imageHeight,0xff121722);
        g.fill(x+2,y+2,x+298,y+25,0xff263145);
        g.drawCenteredString(font,"АПГРЕЙДЕР",x+150,y+9,0xffe6ecff);
        g.drawCenteredString(font,"ВХОД",x+42,y+37,0xffa8b6cc);
        g.drawCenteredString(font,"ЦЕЛЬ",x+258,y+37,0xffa8b6cc);
        slot(g,x+33,y+62);slot(g,x+249,y+62);
        Item target=menu.target();if(target!=Items.AIR)g.renderItem(new ItemStack(target),x+250,y+63);
        g.drawCenteredString(font,"Цена: "+menu.state(2),x+45,y+88,0xffeeeeee);
        g.drawCenteredString(font,"Цена: "+menu.state(3),x+255,y+88,0xffeeeeee);
        long elapsed=System.currentTimeMillis()-started;
        boolean spinning=elapsed>=0&&elapsed<4000;
        double probability=spinning?shownChance:chance();
        if(elapsed>=4000&&started>0)probability=shownChance;
        int cx=x+150,cy=y+69;
        for(int deg=0;deg<360;deg++){
            double a=Math.toRadians(deg)-Math.PI/2;
            int color=deg<probability*360?0xff36cf8a:0xff98435b;
            for(int r=29;r<35;r++){
                int px=cx+(int)Math.round(Math.cos(a)*r),py=cy+(int)Math.round(Math.sin(a)*r);g.fill(px,py,px+2,py+2,color);
            }
        }
        double t=Math.min(1,Math.max(0,elapsed/4000D));
        double angle=started>0?(1-Math.pow(1-t,3))*(Math.PI*2*6+stopAngle)-Math.PI/2:-Math.PI/2;
        for(int r=20;r<39;r++){
            int px=cx+(int)(Math.cos(angle)*r),py=cy+(int)(Math.sin(angle)*r);g.fill(px-1,py-1,px+2,py+2,0xffffffff);
        }
        g.drawCenteredString(font,String.format(Locale.ROOT,"%.2f%%",probability*100),cx,cy-4,0xffffffff);
        if(started>0&&!spinning)g.drawCenteredString(font,won?"УСПЕХ":"НЕУДАЧА",cx,y+102,won?0xff36cf8a:0xffff758c);
        for(int i=0;i<PER_PAGE;i++){
            int px=x+16+(i%COLS)*SIZE,py=y+166+(i/COLS)*SIZE;
            g.fill(px,py,px+20,py+20,0xff263145);
            int index=page*PER_PAGE+i;
            if(index<catalog.size())g.renderItem(new ItemStack(catalog.get(index)),px+2,py+2);
        }
        g.drawCenteredString(font,(page+1)+" / "+Math.max(1,(catalog.size()+PER_PAGE-1)/PER_PAGE),cx,y+239,0xffa8b6cc);
        for(int row=0;row<3;row++)for(int col=0;col<9;col++)slot(g,x+15+col*18,y+259+row*18);
        for(int col=0;col<9;col++)slot(g,x+15+col*18,y+317);
        g.drawString(font,"Предметы доступны по серверному прайсу",x+16,y+336,0xff8798b0,false);
    }
    private void slot(GuiGraphics g,int x,int y){g.fill(x,y,x+18,y+18,0xff46526a);g.fill(x+1,y+1,x+17,y+17,0xff0a0e16);}
    @Override protected void renderLabels(GuiGraphics g,int mx,int my){}
    private int cellAt(double mx,double my){
        int x=(int)mx-leftPos-16,y=(int)my-topPos-166;
        if(x<0||y<0||x>=COLS*SIZE||y>=ROWS*SIZE)return -1;
        return (y/SIZE)*COLS+x/SIZE;
    }
    @Override public boolean mouseClicked(double mx,double my,int button){
        int cell=cellAt(mx,my);
        if(button==0&&cell>=0){
            int index=page*PER_PAGE+cell;
            if(index<catalog.size()&&menu.state(6)==0&&minecraft!=null&&minecraft.getConnection()!=null){
                minecraft.getConnection().sendCommand("upgrader select "+BuiltInRegistries.ITEM.getKey(catalog.get(index)));
                started=-10000;
            }
            return true;
        }
        return super.mouseClicked(mx,my,button);
    }
    @Override public boolean keyPressed(int key,int scan,int mods){
        if(search.isFocused()&&key!=256)return search.keyPressed(key,scan,mods);
        return super.keyPressed(key,scan,mods);
    }
}
