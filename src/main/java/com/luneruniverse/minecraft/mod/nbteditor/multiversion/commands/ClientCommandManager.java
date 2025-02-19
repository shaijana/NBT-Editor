/*
 * Copyright (c) 2016, 2017, 2018, 2019 FabricMC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.luneruniverse.minecraft.mod.nbteditor.multiversion.commands;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.Version;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.network.packet.s2c.play.CommandTreeS2CPacket;
import net.minecraft.network.packet.s2c.play.GameJoinS2CPacket;
import org.jetbrains.annotations.Nullable;

/**
 * Manages client-sided commands and provides some related helper methods.
 *
 * <p>Client-sided commands are fully executed on the client,
 * so players can use them in both singleplayer and multiplayer.
 *
 * <p>Registrations can be done in handlers for {@link ClientCommandRegistrationCallback#EVENT}
 * (See example below.)
 *
 * <p>The commands are run on the client game thread by default.
 * Avoid doing any heavy calculations here as that can freeze the game's rendering.
 * For example, you can move heavy code to another thread.
 *
 * <p>This class also has alternatives to the server-side helper methods in
 * {@link net.minecraft.server.command.CommandManager}:
 * {@link #literal(String)} and {@link #argument(String, ArgumentType)}.
 *
 * <p>The precedence rules of client-sided and server-sided commands with the same name
 * are an implementation detail that is not guaranteed to remain the same in future versions.
 * The aim is to make commands from the server take precedence over client-sided commands
 * in a future version of this API.
 *
 * <h2>Example command</h2>
 * <pre>
 * {@code
 * ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
 * 		dispatcher.register(
 * 			ClientCommandManager.literal("hello").executes(context -> {
 * 				context.getSource().sendFeedback(Text.literal("Hello, world!"));
 * 				return 0;
 * 			})
 * 		);
 * });
 * }
 * </pre>
 */
@Environment(EnvType.CLIENT)
public final class ClientCommandManager {
	private ClientCommandManager() {
	}

	/**
	 * Gets the active command dispatcher that handles client command registration and execution.
	 *
	 * <p>May be null when not connected to a server (dedicated or integrated).</p>
	 *
	 * @return active dispatcher if present
	 */
	public static @Nullable CommandDispatcher<FabricClientCommandSource> getActiveDispatcher() {
		return ClientCommandInternals.getActiveDispatcher();
	}

	/**
	 * Creates a literal argument builder.
	 *
	 * @param name the literal name
	 * @return the created argument builder
	 */
	public static LiteralArgumentBuilder<FabricClientCommandSource> literal(String name) {
		return LiteralArgumentBuilder.literal(name);
	}

	/**
	 * Creates a required argument builder.
	 *
	 * @param name the name of the argument
	 * @param type the type of the argument
	 * @param <T>  the type of the parsed argument value
	 * @return the created argument builder
	 */
	public static <T> RequiredArgumentBuilder<FabricClientCommandSource, T> argument(String name, ArgumentType<T> type) {
		return RequiredArgumentBuilder.argument(name, type);
	}
	
	
	// NBT Editor stuff
	public static GameJoinS2CPacket lastGamePacket;
	public static CommandTreeS2CPacket lastCommandPacket;
	public static void createDispatcher() {
		final CommandDispatcher<FabricClientCommandSource> dispatcher = new CommandDispatcher<>();
		ClientCommandInternals.setActiveDispatcher(dispatcher);
		Object registryAccess = Version.newSwitch()
				.range("1.19.3", null, () -> CommandRegistryAccess.of(MainUtil.client.getNetworkHandler().getRegistryManager(),
						MainUtil.client.getNetworkHandler().getEnabledFeatures()))
/*				.range("1.19.0", "1.19.2", () -> Reflection.newInstance("net.minecraft.class_7157",
						new Class[] {Reflection.getClass("net.minecraft.class_5455")}, // DynamicRegistryManager.class
						lastGamePacket.registryManager()))*/ //TODO: In 1.20.2 this packet has no getter for the registrymanager anymore.
				.range(null, "1.18.2", () -> null)
				.get();
		ClientCommandRegistrationCallback.EVENT.invoker().register(dispatcher, registryAccess);
		ClientCommandInternals.finalizeInit();
	}
	public static void reregisterClientCommands() {
		if (MainUtil.client.getNetworkHandler() == null)
			return;
		createDispatcher();
		MainUtil.client.getNetworkHandler().onCommandTree(lastCommandPacket);
	}
}
