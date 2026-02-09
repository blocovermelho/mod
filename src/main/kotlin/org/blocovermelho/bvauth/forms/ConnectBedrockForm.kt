package org.blocovermelho.bvauth.forms

import kotlinx.coroutines.launch
import org.blocovermelho.bvauth.api.types.Profile
import org.blocovermelho.bvauth.impl.CoroutineManager
import org.geysermc.cumulus.form.CustomForm

fun ConnectBedrockForm(profiles: List<Profile>, gamertag: String, action: suspend (String) -> Unit) : CustomForm {
    return CustomForm.builder()
        .title("Link - Conectar a um perfil existente")
        .label("Selecione o perfil a ser conectado a gamertag \"$gamertag\"")
        .dropdown("Perfil", profiles.map { it.username })
        .validResultHandler {
            val selected = it.next<Int>()
            if (selected != null) {
                CoroutineManager.scope.launch {
                    action(profiles[selected].username)
                }
            }
        }
        .build()
}