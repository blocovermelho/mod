package org.blocovermelho.bvauth.config;

import folk.sisby.kaleido.api.WrappedConfig;
import folk.sisby.kaleido.lib.quiltconfig.api.annotations.Comment;
import folk.sisby.kaleido.lib.quiltconfig.api.values.ValueList;
import net.minecraft.world.level.GameType;

import java.util.List;

public class ModConfig extends WrappedConfig {
    @Comment("O Modo de jogo que o jogador será colocado após o login")
    public GameType Gamemode = GameType.SURVIVAL;
    public ApiSettings Auth = new ApiSettings();
    public ServerDetails Server = new ServerDetails();

    public static class ServerDetails implements Section {
        @Comment("O mome deste servidor")
        public String Nome = "";
        @Comment("Os membres da staff")
        public List<String> Staff = ValueList.create("");
        @Comment("As versões que esse servidor suporta")
        public List<String> Versoes = ValueList.create("");
    }

    public static class ApiSettings implements Section {
        @Comment("A Token da API do Bloco Vermelho para este servidor")
        public String ApiToken = "";
        @Comment("O Endpoint da API do Bloco Vermelho")
        public String Endpoint = "api.blocovermelho.org";
        @Comment("Se a conexão usa TLS. Para a API oficial, o valor sempre é `true`.")
        @Comment("Não mecha se você não sabe o que está fazendo.")
        public boolean TLS = true;
    }
}
