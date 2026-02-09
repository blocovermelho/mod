package org.blocovermelho.bvauth.forms

import kotlinx.coroutines.launch
import org.blocovermelho.bvauth.api.types.WebSocketMessage
import org.blocovermelho.bvauth.impl.CoroutineManager
import org.geysermc.cumulus.form.SimpleForm

fun ProfileLinkOptions(profiles: Int, link: WebSocketMessage.DiscordLink, newProfileAction: suspend () -> Unit, linkProfileAction: suspend () -> Unit) : SimpleForm {
    return SimpleForm.builder()
        .title("Link Manual - Discord")
        .content("Olá @${link.extras?.nickname ?: link.username}.]\n" +
                "Foi verificado que você possui $profiles perfil(is) já conectados na sua conta do discord.\n" +
                "Escolha se você quer:\n" +
                "- Conectar a um perfil existente - Ao logar no bedrock, você irá continuar de onde parou no java edition.\n" +
                "- Novo perfil - Sua conta do bedrock será criada como um perfil novo.")
        .button("Conectar a um perfil existente")
        .button("Novo perfil")
        .validResultHandler { it ->
            if (it.clickedButton().text() == "Conectar a um perfil existente") {
                CoroutineManager.scope.launch {
                    linkProfileAction()
                }
            } else {
                CoroutineManager.scope.launch {
                    newProfileAction()
                }
            }
        }.build()
}

