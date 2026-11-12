package dev.upgrader;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.Commands;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import java.util.function.Supplier;

@Mod(Upgrader.ID)
public final class Upgrader {
    public static final String ID = "create_upgrader";
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, ID);
    private static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, ID);
    public static final Supplier<Item> DEVICE = ITEMS.register("upgrader", () -> new Item(new Item.Properties().stacksTo(1)) {
        @Override public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
            if (player instanceof ServerPlayer sp) {
                sp.openMenu(new SimpleMenuProvider((id, inv, p) -> new UpgradeMenu(id, inv), Component.literal("Апгрейдер")));
            }
            return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide());
        }
    });
    public static final Supplier<MenuType<UpgradeMenu>> MENU = MENUS.register("upgrader", () -> new MenuType<>(UpgradeMenu::new, FeatureFlags.DEFAULT_FLAGS));
    public Upgrader(IEventBus bus) {
        ITEMS.register(bus); MENUS.register(bus);
        NeoForge.EVENT_BUS.addListener(this::commands);
        NeoForge.EVENT_BUS.addListener(this::started);
    }
    private void started(ServerStartedEvent e) { Prices.load(); }
    private void commands(RegisterCommandsEvent e) {
        e.getDispatcher().register(Commands.literal("upgrader")
            .then(Commands.literal("reload").requires(s -> s.hasPermission(2)).executes(c -> {
                Prices.load(); c.getSource().sendSuccess(() -> Component.literal("Прайс обновлён"), false); return 1;
            }))
            .then(Commands.literal("select").then(Commands.argument("item", StringArgumentType.word()).executes(c -> {
                ServerPlayer p = c.getSource().getPlayerOrException();
                if (p.containerMenu instanceof UpgradeMenu menu) {
                    ResourceLocation id = ResourceLocation.tryParse(StringArgumentType.getString(c, "item"));
                    if (id != null) menu.select(id);
                }
                return 1;
            }))));
    }
}
