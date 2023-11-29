package org.blocovermelho.mod.config;

import org.quiltmc.config.api.ReflectiveConfig;
import org.quiltmc.config.api.annotations.Comment;
import org.quiltmc.config.api.values.TrackedValue;

public class ApiSettings extends ReflectiveConfig {
	@Comment("The api endpoint")
	public final TrackedValue<String> endpoint = value("localhost:8080");
	@Comment("The Bearer token used for authenticating with the API")
	public final TrackedValue<String> token = value("test");
	@Comment("If TLS should be used")
	public final TrackedValue<Boolean> tls = value(false);
}
