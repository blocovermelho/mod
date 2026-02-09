package org.blocovermelho.bvauth.forms

import kotlinx.coroutines.launch
import org.blocovermelho.bvauth.impl.CoroutineManager
import org.geysermc.cumulus.form.CustomForm

fun LinkForm(action: suspend (String) -> Unit ) : CustomForm {
    return CustomForm.builder()
        .title("Link Manual - Discord")
        .label("Digite aqui o token gerado pelo bot do discord.")
        .input("Token - CaSe SeNsiTivE", "Quatro Palavras Separadas Aqui")
        .validResultHandler { response ->
            val token = response.next<String>()
            if (!token.isNullOrEmpty()) {
                CoroutineManager.scope.launch {
                    action(token)
                }
            }
        }.build()
}