package dev.upgrader;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@EventBusSubscriber(modid=Upgrader.ID,bus=EventBusSubscriber.Bus.MOD,value=Dist.CLIENT)
public final class ClientEvents {
    @SubscribeEvent public static void screens(RegisterMenuScreensEvent e){e.register(Upgrader.MENU.get(),UpgradeScreen::new);}
}
