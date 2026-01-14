package org.blocovermelho.bvauth.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.execution.ExecutionContext;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import org.blocovermelho.bvauth.BvAuthMod;
import org.blocovermelho.bvauth.impl.VisitorGetter;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;
import java.util.function.Consumer;

@Mixin(Commands.class)
public class CommandsMixin {

    @Shadow
    @Final
    private CommandDispatcher<CommandSourceStack> dispatcher;
    @Unique
    private static List<String> bv$visitorRoots = List.of("link", "registrar");
    @Unique
    private static List<String> bv$memberRoots = List.of("login");


    // This blocks sending commands which an user cant execute yet.
    // Its merely QOL.
    @ModifyExpressionValue(method = "fillUsableCommands", at = @At(value = "INVOKE", target = "Lcom/mojang/brigadier/tree/CommandNode;canUse(Ljava/lang/Object;)Z"))
    private static <S> boolean bv$commandWhitelist(boolean original, @Local(ordinal=0, argsOnly = true) CommandNode<S> root, @Local(ordinal = 2) CommandNode<S> childNode, @Local(argsOnly = true) S source) {
        String name = childNode.getName();

        // We only do this check on literals, not arguments.
        if (childNode instanceof ArgumentCommandNode<S,?>) {
            return original;
        }

        // And if the root node has a direct child relationship to it
        if (!(root instanceof RootCommandNode<S>)){
            return original;
        }

        if (source instanceof CommandSourceStack cstack) {
            PlayerList plist = cstack.getServer().getPlayerList();
            ServerPlayer player = cstack.getPlayer();
            if (player != null) {
                return bv$runCheck(plist, player, name);
            } else {
                return original;
            }
        }
        return original;
    }

    // This is for blocking hacked clients from sending a packet directly.
    @WrapWithCondition(method = "performCommand", at = @At(value = "INVOKE", target = "Lnet/minecraft/commands/Commands;executeCommandInContext(Lnet/minecraft/commands/CommandSourceStack;Ljava/util/function/Consumer;)V"))
    public boolean bv$blockCommandExecution(CommandSourceStack source, Consumer<ExecutionContext<CommandSourceStack>> contextConsumer, @Local(argsOnly = true) String command) {
        ServerPlayer player = source.getPlayer();
        PlayerList plist = source.getServer().getPlayerList();
        String check = command.split(" ")[0].toLowerCase();
        if (player == null) {
            return true;
        }

        return bv$runCheck(plist, player, check);
    }

    @Unique
    private static boolean bv$runCheck(PlayerList plist, ServerPlayer player, String command) {
        boolean isVisitor = ((VisitorGetter) plist).bv$isVisitor(player.getUUID());
        boolean isLogged = BvAuthMod.Companion.getLoggedUsers().contains(player.getUUID());
        boolean inVisitor = (isVisitor && bv$visitorRoots.contains(command));
        boolean inNonLoggedMember = (!isLogged && bv$memberRoots.contains(command));

        return isLogged || inVisitor || inNonLoggedMember;
    }
}
