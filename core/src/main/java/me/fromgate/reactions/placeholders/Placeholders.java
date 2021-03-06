package me.fromgate.reactions.placeholders;

import me.fromgate.reactions.flags.Flags;
import me.fromgate.reactions.util.Param;
import me.fromgate.reactions.util.Variables;
import me.fromgate.reactions.util.message.M;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Placeholders {
    private static List<Placeholder> placeholders = new ArrayList<>();

    public static void init() {
        add(new PlaceholderPlayer());
        add(new PlaceholderMoney());
        add(new PlaceholderRandom());
        add(new PlaceholderTime());
        add(new PlaceholderCalc());
    }

    public static boolean add(Placeholder ph) {
        if (ph == null) return false;
        if (ph.getKeys().length == 0) return false;
        if (ph.getId().equalsIgnoreCase("UNKNOWN")) return false;
        placeholders.add(ph);
        return true;
    }

    public static Map<String, String> replacePlaceholders(Player p, Param param) {
        Map<String, String> resultMap = new HashMap<>();
        for (String paramKey : param.getMap().keySet()) {
            resultMap.put(paramKey, replacePlaceholders(p, param.getParam(paramKey)));
        }
        return resultMap;
    }

    public static String replacePlaceholders(Player player, String string) {
        String result = string;
        result = Variables.replaceTempVars(result);
        result = Variables.replacePlaceholders(player, result);
        Pattern pattern = Pattern.compile("(%\\w+%)|(%\\w+:\\w+%)|(%\\w+:\\S+%)");
        Matcher matcher = pattern.matcher(result);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String group = new StringBuilder("%")
                    .append(replacePlaceholders(player, matcher.group()
                            .replaceAll("^%", "").replaceAll("%$", "")))
                    .append("%").toString();
            String replacement = replacePlaceholder(player, group);
            matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement == null ? group : replacement));
        }
        matcher.appendTail(sb);
        result = sb.toString();
        if (!string.equals(result)) result = replacePlaceholders(player, result);
        return result;
    }

    private static String replacePlaceholder(Player player, String field) {
        String key = field.replaceAll("^%", "").replaceAll("%$", "");
        String value = "";
        if (field.matches("(%\\w+:\\S+%)")) {
            value = field.replaceAll("^%\\w+:", "").replaceAll("%$", "");
            key = key.replaceAll(Pattern.quote(":" + value) + "$", "");
        }
        for (Placeholder ph : placeholders) {
            if (ph.checkKey(key)) return ph.processPlaceholder(player, key, value);
        }
        return field;
    }

    public static void listPlaceholders(CommandSender sender, int pageNum) {
        List<String> phList = new ArrayList<>();
        for (Placeholder ph : placeholders) {
            for (String phKey : ph.getKeys()) {
                if (phKey.toLowerCase().equals(phKey)) continue;
                M desc = M.getByName("placeholder_" + phKey);
                if (desc == null) {
                    M.LNG_FAIL_PLACEHOLDER_DESC.log(phKey);
                } else {
                    phList.add("&6" + phKey + "&3: &a" + desc.getText("NOCOLOR"));
                }
            }
        }
        for (Flags f : Flags.values()) {
            if (f != Flags.TIME && f != Flags.CHANCE) continue;
            String name = f.name();
            M desc = M.getByName("placeholder_" + name);
            if (desc == null) {
                M.LNG_FAIL_PLACEHOLDER_DESC.log(name);
            } else {
                phList.add("&6" + name + "&3: &a" + desc.getText("NOCOLOR"));
            }
        }
        phList.add("&6VAR&3: &a" + M.PLACEHOLDER_VAR.getText("NOCOLOR"));
        phList.add("&6SIGN_LOC, SIGN_LINE1,.. SIGN_LINE4&3: &a" + M.PLACEHOLDER_SIGNACT.getText("NOCOLOR"));
        phList.add("&6ARG0, ARG1, ARG2...&3: &a" + M.PLACEHOLDER_COMMANDACT.getText("NOCOLOR"));
        M.printPage(sender, phList, M.MSG_PLACEHOLDERLISTTITLE, pageNum, sender instanceof Player ? 10 : 1000);
    }
}
