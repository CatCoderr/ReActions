/*  
 *  ReActions, Minecraft bukkit plugin
 *  (c)2012-2017, fromgate, fromgate@gmail.com
 *  http://dev.bukkit.org/server-mods/reactions/
 *    
 *  This file is part of ReActions.
 *  
 *  ReActions is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  ReActions is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with ReActions.  If not, see <http://www.gnorg/licenses/>.
 * 
 */


package me.fromgate.reactions.activators;

import com.google.common.base.Objects;
import com.google.common.collect.ComparisonChain;
import me.fromgate.reactions.ReActions;
import me.fromgate.reactions.actions.Actions;
import me.fromgate.reactions.flags.Flags;
import me.fromgate.reactions.util.ActVal;
import me.fromgate.reactions.util.FlagVal;
import me.fromgate.reactions.util.Variables;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Event;

import java.util.ArrayList;
import java.util.List;

public abstract class Activator implements Comparable<Activator> {

    String name;
    String group;

    public Activator(String name, String group) {
        this.name = name;
        this.group = group;
    }

    public Activator(String name, String group, YamlConfiguration cfg) {
        this.name = name;
        this.loadActivator(cfg);
        this.group = group;
    }

    private List<FlagVal> flags = new ArrayList<>();
    private List<ActVal> actions = new ArrayList<>();
    private List<ActVal> reactions = new ArrayList<>();

    public void addFlag(String flag, String param, boolean not) {
        flags.add(new FlagVal(Flags.getValidName(flag), param, not));
    }

    public boolean removeFlag(int index) {
        if (flags.size() <= index) return false;
        flags.remove(index);
        return true;
    }

    public List<FlagVal> getFlags() {
        return flags;
    }

    public void addAction(String action, String param) {
        actions.add(new ActVal(Actions.getValidName(action), param));
    }

    public boolean removeAction(int index) {
        if (actions.size() <= index) return false;
        actions.remove(index);
        return true;
    }

    public void addReaction(String action, String param) {
        reactions.add(new ActVal(Actions.getValidName(action), param));
    }

    public boolean removeReaction(int index) {
        if (reactions.size() <= index) return false;
        reactions.remove(index);
        return true;
    }

    public List<ActVal> getActions() {
        return actions;
    }

    public List<ActVal> getReactions() {
        return reactions;
    }


    public void clearFlags() {
        flags.clear();
    }

    public void clearActions() {
        actions.clear();
    }

    public void clearReactions() {
        reactions.clear();
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this).omitNullValues().toString();
    }


    public boolean equals(String name) {
        if (name == null) return false;
        if (name.isEmpty()) return false;
        return this.name.equalsIgnoreCase(name);
    }

    @Override
    public int compareTo(Activator o) {
        return ComparisonChain.start()
                .compare(o.name, name, String.CASE_INSENSITIVE_ORDER)
                .compare(o.group, group)
                .result();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof Activator))
            return false;
        Activator other = (Activator) obj;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!this.name.equals(other.name))
            return false;
        return true;
    }

    /*
     *  Группу по идее дублировать не надо... т.е. она должна выставляться "из вне"...
     */
    public void saveActivator(YamlConfiguration cfg) {
        String key = getType() + "." + this.name;
        save(key, cfg);
        List<String> flg = new ArrayList<>();
        for (FlagVal f : flags) flg.add(f.toString());
        cfg.set(key + ".flags", flg.isEmpty() && !ReActions.getPlugin().saveEmpty() ? null : flg);
        flg = new ArrayList<>();
        for (ActVal a : actions) flg.add(a.toString());
        cfg.set(key + ".actions", flg.isEmpty() && !ReActions.getPlugin().saveEmpty() ? null : flg);
        flg = new ArrayList<>();
        for (ActVal a : reactions) flg.add(a.toString());
        cfg.set(key + ".reactions", flg.isEmpty() && !ReActions.getPlugin().saveEmpty() ? null : flg);
    }

    public void loadActivator(YamlConfiguration cfg) {
        String key = getType().name() + "." + this.name;
        load(key, cfg);
        List<String> flg = cfg.getStringList(key + ".flags");
        for (String flgstr : flg) {
            String flag = flgstr;
            String param = "";
            boolean not = false;
            if (flgstr.contains("=")) {
                flag = new String(flgstr.substring(0, flgstr.indexOf("=")));
                if (flgstr.indexOf("=") < flgstr.length())
                    param = new String(flgstr.substring(flgstr.indexOf("=") + 1, flgstr.length()));
            }
            if (flag.startsWith("!")) {
                flag = flag.replaceFirst("!", "");
                not = true;
            }
            addFlag(flag, param, not);
        }

        flg = cfg.getStringList(key + ".actions");
        for (String flgstr : flg) {
            String flag = flgstr;
            String param = "";
            if (flgstr.contains("=")) {
                flag = flgstr.substring(0, flgstr.indexOf("="));
                param = flgstr.substring(flgstr.indexOf("=") + 1, flgstr.length());
            }
            addAction(flag, param);
        }

        flg = cfg.getStringList(key + ".reactions");
        for (String flgstr : flg) {
            String flag = flgstr;
            String param = "";
            if (flgstr.contains("=")) {
                flag = new String(flgstr.substring(0, flgstr.indexOf("=")));
                param = new String(flgstr.substring(flgstr.indexOf("=") + 1, flgstr.length()));
            }
            addReaction(flag, param);
        }
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getGroup() {
        return this.group;
    }

    public String getName() {
        return this.name;
    }


    public boolean executeActivator(Event event) {
        boolean result = activate(event);
        Variables.clearAllTempVar();
        return result;
    }


    public abstract boolean activate(Event event); // Наверное всё-таки так

    public abstract boolean isLocatedAt(Location loc);

    public abstract void save(String root, YamlConfiguration cfg);

    public abstract void load(String root, YamlConfiguration cfg);

    public abstract ActivatorType getType();

    public boolean isTypeOf(String str) {
        return ((getType().name().equalsIgnoreCase(str)) || (getType().getAlias().equalsIgnoreCase(str)));
    }

    public abstract boolean isValid();

}
