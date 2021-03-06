package me.fromgate.reactions.activators;

import me.fromgate.reactions.actions.Actions;
import me.fromgate.reactions.event.PlayerInventoryClickEvent;
import me.fromgate.reactions.util.Param;
import me.fromgate.reactions.util.Variables;
import me.fromgate.reactions.util.item.ItemUtil;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;

public class InventoryClickActivator extends Activator {
    private ClickType click;
    private InventoryAction action;
    private InventoryType inventory;
    private SlotType slot;
    private String itemStr;


    public InventoryClickActivator(String name, String param) {
        super(name, "activators");
        Param params = new Param(param);
        this.click = ClickType.getByName(params.getParam("click", "ANY"));
        this.action = InventoryAction.getByName(params.getParam("action", "ANY"));
        this.inventory = InventoryType.getByName(params.getParam("inventory", "ANY"));
        this.slot = SlotType.getByName(params.getParam("slot", "ANY"));
        this.itemStr = params.getParam("item");
    }

    public InventoryClickActivator(String name, String group, YamlConfiguration cfg) {
        super(name, group, cfg);
    }


    @Override
    public boolean activate(Event event) {
        if (!(event instanceof PlayerInventoryClickEvent)) return false;
        PlayerInventoryClickEvent pice = (PlayerInventoryClickEvent) event;
        if (pice.getClickType() == null) return false;
        if (!clickCheck(pice.getClickType())) return false;
        if (!actionCheck(pice.getAction())) return false;
        if (!inventoryCheck(pice.getInventoryType())) return false;
        if (!slotCheck(pice.getSlotType())) return false;
        if (!checkItem(pice.getItemStack())) return false;
        Variables.setTempVar("click", pice.getClickType().toString());
        Variables.setTempVar("action", pice.getAction().toString());
        Variables.setTempVar("slot", pice.getSlotType().toString());
        Variables.setTempVar("inventory", pice.getInventoryType().toString());
        return Actions.executeActivator(pice.getPlayer(), this);
    }

    @Override
    public boolean isLocatedAt(Location l) {
        return false;
    }


    @Override
    public void save(String root, YamlConfiguration cfg) {
        cfg.set(root + ".click-type", click.name());
        cfg.set(root + ".action-type", action.name());
        cfg.set(root + ".inventory-type", inventory.name());
        cfg.set(root + ".slot-type", slot.name());
        cfg.set(root + ".item", this.itemStr);
    }

    @Override
    public void load(String root, YamlConfiguration cfg) {
        this.click = ClickType.getByName(cfg.getString(root + ".click-type", "ANY"));
        this.action = InventoryAction.getByName(cfg.getString(root + ".action-type", "ANY"));
        this.inventory = InventoryType.getByName(cfg.getString(root + ".inventory-type", "ANY"));
        this.slot = SlotType.getByName(cfg.getString(root + ".slot-type", "ANY"));
        this.itemStr = cfg.getString(root + ".item", "");
    }

    @Override
    public ActivatorType getType() {
        return ActivatorType.INVENTORY_CLICK;
    }

    enum ClickType {
        ANY,
        CONTROL_DROP,
        CREATIVE,
        DROP,
        DOUBLE_CLICK,
        LEFT,
        MIDDLE,
        NUMBER_KEY,
        RIGHT,
        SHIFT_LEFT,
        SHIFT_RIGHT,
        UNKNOWN,
        WINDOW_BORDER_LEFT,
        WINDOW_BORDER_RIGHT;

        public static ClickType getByName(String clickStr) {
            if (clickStr != null) {
                for (ClickType clickType : values()) {
                    if (clickStr.equalsIgnoreCase(clickType.name())) {
                        return clickType;
                    }
                }
            }
            return ClickType.ANY;
        }
    }

    enum InventoryAction {
        ANY,
        CLONE_STACK,
        COLLECT_TO_CURSOR,
        DROP_ALL_CURSOR,
        DROP_ALL_SLOT,
        DROP_ONE_CURSOR,
        DROP_ONE_SLOT,
        HOTBAR_MOVE_AND_READD,
        HOTBAR_SWAP,
        MOVE_TO_OTHER_INVENTORY,
        NOTHING,
        PICKUP_ALL,
        PICKUP_HALF,
        PICKUP_ONE,
        PICKUP_SOME,
        PLACE_ALL,
        PLACE_ONE,
        PLACE_SOME,
        SWAP_WITH_CURSOR,
        UNKNOWN;

        public static InventoryAction getByName(String actionStr) {
            if (actionStr != null) {
                for (InventoryAction action : values()) {
                    if (actionStr.equalsIgnoreCase(action.name())) {
                        return action;
                    }
                }
            }
            return InventoryAction.ANY;
        }
    }


    enum InventoryType {
        ANY,
        ANVIL,
        BEACON,
        BREWING,
        CHEST,
        CRAFTING,
        CREATIVE,
        DISPENSER,
        DROPPER,
        ENCHANTING,
        ENDER_CHEST,
        HOPPER,
        MERCHANT,
        PLAYER,
        SHULKER_BOX,
        WORKBENCH;

        public static InventoryType getByName(String inventoryStr) {
            if (inventoryStr != null) {
                for (InventoryType inventoryType : values()) {
                    if (inventoryStr.equalsIgnoreCase(inventoryType.name())) {
                        return inventoryType;
                    }
                }
            }
            return InventoryType.ANY;
        }
    }

    enum SlotType {
        ANY,
        ARMOR,
        CONTAINER,
        CRAFTING,
        FUEL,
        OUTSIDE,
        QUICKBAR,
        RESULT;

        public static SlotType getByName(String slotStr) {
            if (slotStr != null) {
                for (SlotType slotType : values()) {
                    if (slotStr.equalsIgnoreCase(slotType.name())) {
                        return slotType;
                    }
                }
            }
            return SlotType.ANY;
        }
    }

    private boolean clickCheck(org.bukkit.event.inventory.ClickType ct) {
        if (click.name().equals("ANY")) return true;
        return ct.name().equals(click.name());
    }

    private boolean actionCheck(org.bukkit.event.inventory.InventoryAction act) {
        if (action.name().equals("ANY")) return true;
        return act.name().equals(action.name());
    }

    private boolean inventoryCheck(org.bukkit.event.inventory.InventoryType it) {
        if (inventory.name().equals("ANY")) return true;
        return it.name().equals(inventory.name());
    }

    private boolean slotCheck(org.bukkit.event.inventory.InventoryType.SlotType sl) {
        if (slot.name().equals("ANY")) return true;
        return sl.name().equals(slot.name());
    }

    private boolean checkItem(ItemStack item) {
        if (this.itemStr.isEmpty()) return true;
        return ItemUtil.compareItemStr(item, this.itemStr, true);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(name).append(" [").append(getType()).append("]");
        if (!getFlags().isEmpty()) sb.append(" F:").append(getFlags().size());
        if (!getActions().isEmpty()) sb.append(" A:").append(getActions().size());
        if (!getReactions().isEmpty()) sb.append(" R:").append(getReactions().size());
        sb.append(" (");
        sb.append("click:").append(this.click.name());
        sb.append(" action:").append(this.action.name());
        sb.append(" inventory:").append(this.inventory.name());
        sb.append(" slot:").append(this.slot.name());
        sb.append(")");
        return sb.toString();
    }

    @Override
    public boolean isValid() {
        return true;
    }
}
