package me.fromgate.reactions.commands;

import me.fromgate.reactions.util.Locator;
import me.fromgate.reactions.util.Selector;
import me.fromgate.reactions.util.message.M;
import org.bukkit.entity.Player;

@CmdDefine(command = "react", description = M.CMD_SELECT, permission = "reactions.select",
        subCommands = {"select|sel"}, allowConsole = false, shortDescription = "&3/react select")
public class CmdSelect extends Cmd {
    @Override
    public boolean execute(Player player, String[] args) {
        Selector.selectLocation(player, null);
        M.CMD_SELECTED.print(player, Locator.locationToStringFormated(Selector.getSelectedLocation(player)));
        return true;
    }
}
