package com.spongeyts.sneaktoggle;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class SneakToggleClient implements ClientModInitializer {

    private static KeyBinding toggleSneakKey;
    private static KeyBinding holdSneakKey;

    private static boolean toggled = false;
    private static boolean sentSneak = false;

    @Override
    public void onInitializeClient() {

        toggleSneakKey = KeyBindingHelper.registerKeyBinding(
                new KeyBinding(
                        "key.sneaktoggle.toggle",
                        InputUtil.Type.KEYSYM,
                        GLFW.GLFW_KEY_G,
                        "category.sneaktoggle"
                )
        );

        holdSneakKey = KeyBindingHelper.registerKeyBinding(
                new KeyBinding(
                        "key.sneaktoggle.hold",
                        InputUtil.Type.KEYSYM,
                        GLFW.GLFW_KEY_V,
                        "category.sneaktoggle"
                )
        );

        ClientTickEvents.END_CLIENT_TICK.register(client -> {

            if (client.player == null || client.getNetworkHandler() == null) {
                return;
            }

            while (toggleSneakKey.wasPressed()) {
                toggled = !toggled;

                client.player.sendMessage(
                        Text.literal(
                                "Toggle Sneak: " + (toggled ? "ON" : "OFF")
                        ),
                        true
                );
            }

            boolean holding = holdSneakKey.isPressed();
            boolean shouldSneak = toggled || holding;

            if (shouldSneak && !sentSneak) {
                client.getNetworkHandler().sendPacket(
                        new ClientCommandC2SPacket(
                                client.player,
                                ClientCommandC2SPacket.Mode.PRESS_SHIFT_KEY
                        )
                );

                sentSneak = true;
            }

            if (!shouldSneak && sentSneak) {
                client.getNetworkHandler().sendPacket(
                        new ClientCommandC2SPacket(
                                client.player,
                                ClientCommandC2SPacket.Mode.RELEASE_SHIFT_KEY
                        )
                );

                sentSneak = false;
            }
        });
    }
}
