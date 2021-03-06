package com.nisovin.shopkeepers.commands.shopkeepers;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.nisovin.shopkeepers.Settings;
import com.nisovin.shopkeepers.api.ShopkeepersPlugin;
import com.nisovin.shopkeepers.api.shopkeeper.player.PlayerShopkeeper;
import com.nisovin.shopkeepers.commands.arguments.ShopkeeperArgument;
import com.nisovin.shopkeepers.commands.arguments.ShopkeeperFilter;
import com.nisovin.shopkeepers.commands.arguments.TargetShopkeeperFallback;
import com.nisovin.shopkeepers.commands.lib.Command;
import com.nisovin.shopkeepers.commands.lib.CommandContextView;
import com.nisovin.shopkeepers.commands.lib.CommandException;
import com.nisovin.shopkeepers.commands.lib.CommandInput;
import com.nisovin.shopkeepers.commands.lib.arguments.PlayerArgument;
import com.nisovin.shopkeepers.util.PermissionUtils;
import com.nisovin.shopkeepers.util.ShopkeeperUtils.TargetShopkeeperFilter;
import com.nisovin.shopkeepers.util.TextUtils;

class CommandTransfer extends Command {

	private static final String ARGUMENT_SHOPKEEPER = "shopkeeper";
	private static final String ARGUMENT_NEW_OWNER = "new-owner";

	CommandTransfer() {
		super("transfer");

		// set permission:
		this.setPermission(ShopkeepersPlugin.TRANSFER_PERMISSION);

		// set description:
		this.setDescription(Settings.msgCommandDescriptionTransfer);

		// arguments:
		this.addArgument(new TargetShopkeeperFallback(
				new ShopkeeperArgument(ARGUMENT_SHOPKEEPER, ShopkeeperFilter.PLAYER),
				TargetShopkeeperFilter.PLAYER
		));
		this.addArgument(new PlayerArgument(ARGUMENT_NEW_OWNER)); // new owner has to be online
		// TODO allow offline-player?
	}

	@Override
	protected void execute(CommandInput input, CommandContextView context) throws CommandException {
		CommandSender sender = input.getSender();

		PlayerShopkeeper shopkeeper = (PlayerShopkeeper) context.get(ARGUMENT_SHOPKEEPER);
		assert shopkeeper != null;
		Player newOwner = context.get(ARGUMENT_NEW_OWNER);
		assert newOwner != null;

		// check that the shop is owned by the executing player:
		Player player = (sender instanceof Player) ? (Player) sender : null;
		if ((player == null || !shopkeeper.isOwner(player)) && !PermissionUtils.hasPermission(sender, ShopkeepersPlugin.BYPASS_PERMISSION)) {
			TextUtils.sendMessage(sender, Settings.msgNotOwner);
			return;
		}

		// set new owner:
		shopkeeper.setOwner(newOwner);

		// success:
		TextUtils.sendMessage(player, Settings.msgOwnerSet, "owner", TextUtils.getPlayerText(newOwner));

		// save:
		ShopkeepersPlugin.getInstance().getShopkeeperStorage().save();
	}
}
