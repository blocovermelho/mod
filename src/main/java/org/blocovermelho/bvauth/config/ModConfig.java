package org.blocovermelho.bvauth.config;


import folk.sisby.kaleido.api.ReflectiveConfig;
import folk.sisby.kaleido.lib.quiltconfig.api.annotations.Comment;
import folk.sisby.kaleido.lib.quiltconfig.api.values.TrackedValue;
import folk.sisby.kaleido.lib.quiltconfig.api.values.ValueList;
import net.minecraft.world.level.GameType;


public class ModConfig extends ReflectiveConfig {
    @Comment("O Modo de jogo que o jogador será colocado após o login")
    public final TrackedValue<GameType> Gamemode = this.value(GameType.SURVIVAL);
    public final ApiSettings Auth =  new ApiSettings();
    public final ServerDetails Server = new ServerDetails();


    public static class ServerDetails extends Section {
        @Comment("O mome deste servidor")
        public final TrackedValue<String> Nome = this.value("");
        @Comment("Os membres da staff")
        public final TrackedValue<ValueList<String>> Staff = this.list("");
        @Comment("As versões que esse servidor suporta")
        public final TrackedValue<ValueList<String>> Versoes = this.list("");
    }

    public static class ApiSettings  extends Section {
        @Comment("A Token da API do Bloco Vermelho para este servidor")
        public final TrackedValue<String> ApiToken = this.value("");
        @Comment("O Endpoint da API do Bloco Vermelho")
        public final TrackedValue<String> Endpoint = this.value("api.blocovermelho.org");
        @Comment("Se a conexão usa TLS. Para a API oficial, o valor sempre é `true`.")
        @Comment("Não mecha se você não sabe o que está fazendo.")
        public final TrackedValue<Boolean> TLS = this.value(true);
    }
}
