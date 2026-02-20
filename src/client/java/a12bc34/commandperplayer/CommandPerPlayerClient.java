package a12bc34.commandperplayer;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class CommandPerPlayerClient implements ClientModInitializer {

	public static KeyBinding openMenuKey;

	@Override
	public void onInitializeClient() {
		openMenuKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
				"Open command per player menu",
				InputUtil.Type.KEYSYM,
				GLFW.GLFW_KEY_J,
				KeyBinding.Category.MISC
		));
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (openMenuKey.wasPressed()) {
				client.setScreen(new MojMenuScreen(Text.literal("command per player - a12bc34")));
			}
		});
	}
}