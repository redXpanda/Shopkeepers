package com.nisovin.shopkeepers.shopobjects.living.types;

import java.util.List;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.entity.Rabbit;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import com.nisovin.shopkeepers.api.shopkeeper.ShopCreationData;
import com.nisovin.shopkeepers.lang.Messages;
import com.nisovin.shopkeepers.property.EnumProperty;
import com.nisovin.shopkeepers.property.Property;
import com.nisovin.shopkeepers.shopkeeper.AbstractShopkeeper;
import com.nisovin.shopkeepers.shopobjects.living.LivingShops;
import com.nisovin.shopkeepers.shopobjects.living.SKLivingShopObjectType;
import com.nisovin.shopkeepers.ui.editor.Button;
import com.nisovin.shopkeepers.ui.editor.Session;
import com.nisovin.shopkeepers.ui.editor.ShopkeeperActionButton;
import com.nisovin.shopkeepers.util.inventory.ItemUtils;
import com.nisovin.shopkeepers.util.java.EnumUtils;
import com.nisovin.shopkeepers.util.java.Validate;

public class RabbitShop extends BabyableShop<Rabbit> {

	private final Property<Rabbit.Type> rabbitTypeProperty = new EnumProperty<>(shopkeeper, Rabbit.Type.class, "rabbitType", Rabbit.Type.BROWN);

	public RabbitShop(	LivingShops livingShops, SKLivingShopObjectType<RabbitShop> livingObjectType,
						AbstractShopkeeper shopkeeper, ShopCreationData creationData) {
		super(livingShops, livingObjectType, shopkeeper, creationData);
	}

	@Override
	public void load(ConfigurationSection configSection) {
		super.load(configSection);
		rabbitTypeProperty.load(configSection);
	}

	@Override
	public void save(ConfigurationSection configSection) {
		super.save(configSection);
		rabbitTypeProperty.save(configSection);
	}

	@Override
	protected void onSpawn(Rabbit entity) {
		super.onSpawn(entity);
		this.applyRabbitType(entity);
	}

	@Override
	public List<Button> createEditorButtons() {
		List<Button> editorButtons = super.createEditorButtons();
		editorButtons.add(this.getRabbitTypeEditorButton());
		return editorButtons;
	}

	// RABBIT TYPE

	public Rabbit.Type getRabbitType() {
		return rabbitTypeProperty.getValue();
	}

	public void setRabbitType(Rabbit.Type rabbitType) {
		Validate.notNull(rabbitType, "rabbitType is null");
		rabbitTypeProperty.setValue(rabbitType);
		shopkeeper.markDirty();
		this.applyRabbitType(this.getEntity()); // Null if not spawned
	}

	public void cycleRabbitType(boolean backwards) {
		this.setRabbitType(EnumUtils.cycleEnumConstant(Rabbit.Type.class, this.getRabbitType(), backwards));
	}

	private void applyRabbitType(Rabbit entity) {
		if (entity == null) return;
		Rabbit.Type rabbitType = this.getRabbitType();
		if (rabbitType == Rabbit.Type.THE_KILLER_BUNNY) {
			// Special handling if the rabbit type is the killer rabbit:
			// If the entity's custom name is not empty, Minecraft applies the 'The Killer Rabbit' name. We therefore
			// temporarily set the custom name to something non-empty:
			String customName = entity.getCustomName(); // can be null
			entity.setCustomName(" "); // Non-empty
			entity.setRabbitType(rabbitType);
			entity.setCustomName(customName); // Reset previous name
			// CraftBukkit and Minecraft reset the rabbit's pathfinder goals, so we clear them again:
			this.overwriteAI();
			// Minecraft also sets the rabbit's armor attribute, but this doesn't affect us.
		} else {
			entity.setRabbitType(rabbitType);
		}
	}

	private ItemStack getRabbitTypeEditorItem() {
		ItemStack iconItem = new ItemStack(Material.LEATHER_CHESTPLATE);
		switch (this.getRabbitType()) {
		case BROWN:
			ItemUtils.setLeatherColor(iconItem, Color.fromRGB(141, 118, 88));
			break;
		case WHITE:
			ItemUtils.setLeatherColor(iconItem, Color.WHITE);
			break;
		case BLACK:
			ItemUtils.setLeatherColor(iconItem, Color.fromRGB(31, 31, 31));
			break;
		case BLACK_AND_WHITE:
			ItemUtils.setLeatherColor(iconItem, Color.GRAY);
			break;
		case GOLD:
			ItemUtils.setLeatherColor(iconItem, Color.fromRGB(246, 224, 136));
			break;
		case SALT_AND_PEPPER:
			ItemUtils.setLeatherColor(iconItem, Color.fromRGB(138, 120, 98));
			break;
		case THE_KILLER_BUNNY:
			ItemUtils.setLeatherColor(iconItem, Color.fromRGB(142, 11, 28));
			break;
		default:
			// Unknown type:
			ItemUtils.setLeatherColor(iconItem, Color.PURPLE);
			break;
		}
		ItemUtils.setDisplayNameAndLore(iconItem, Messages.buttonRabbitVariant, Messages.buttonRabbitVariantLore);
		return iconItem;
	}

	private Button getRabbitTypeEditorButton() {
		return new ShopkeeperActionButton() {
			@Override
			public ItemStack getIcon(Session session) {
				return getRabbitTypeEditorItem();
			}

			@Override
			protected boolean runAction(InventoryClickEvent clickEvent, Player player) {
				boolean backwards = clickEvent.isRightClick();
				cycleRabbitType(backwards);
				return true;
			}
		};
	}
}
