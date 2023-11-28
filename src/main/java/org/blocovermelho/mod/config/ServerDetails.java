package org.blocovermelho.mod.config;

import net.minecraft.world.GameMode;
import org.blocovermelho.mod.api.models.Modpack;
import org.quiltmc.config.api.ReflectiveConfig;
import org.quiltmc.config.api.annotations.Comment;
import org.quiltmc.config.api.values.TrackedValue;
import org.quiltmc.config.api.values.ValueList;

import java.util.UUID;

public class ServerDetails extends ReflectiveConfig {
	@Comment("The saved UUID gotten from the server")
	public final TrackedValue<String> id = value(new UUID(0L, 0L).toString());
	@Comment("The name of the server to be saved on the API")
	public final TrackedValue<String> name = value("Test Server");
	@Comment("The supported minecraft versions for this server")
	public final TrackedValue<ValueList<String>> supportedVersions = list("");

	@Comment("Information about the modpack running/recommended on the server.")
	public final ModpackSection modpack = new ModpackSection();
	@Comment("The gamemode the player will be put after they are authenticated.")
	public final TrackedValue<GameMode> postLoginGamemode = value(GameMode.SURVIVAL);

	public class ModpackSection extends Section {
		@Comment("The name of the modpack")
		public final TrackedValue<String> name = value("");
		@Comment("The link to the modpack page, or where you can obtain it")
		public final TrackedValue<String> uri = value("");
		@Comment("The source of the modpack")
		public final TrackedValue<Modpack.ModpackSource> source = value(Modpack.ModpackSource.Modrinth);

		@Comment("The version of the modpack")
		public final TrackedValue<String> version = value("");

	}

}
