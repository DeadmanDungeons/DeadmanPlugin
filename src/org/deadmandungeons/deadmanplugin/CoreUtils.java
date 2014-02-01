package org.deadmandungeons.deadmanplugin;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;

public class CoreUtils {
	
	private CoreUtils(){}
	

	public static Sign getSignState(final Block block) {
		Sign sign = null;
		if(block.getType().equals(Material.SIGN_POST) || block.getType().equals(Material.WALL_SIGN)) {
			BlockState bs = block.getState();
			sign = (Sign) bs;
		}
		return sign;
	}
	
	
	public static void clearSign(Sign sign) {
		sign.setLine(0, "");
		sign.setLine(1, "");
		sign.setLine(2, "");
		sign.setLine(3, "");
		sign.update();
	}
	
	/**
	 * TODO remove deprecated. put this here to remind myself that the parameter is the colro string and not the config path
	 * @param color
	 * @return
	 */
	@Deprecated
	public static ChatColor getChatColor(String color) {
		ChatColor signColor = null;
		if (isColorValid(color)) {
			if (ChatColor.valueOf(color).isColor()) {
				signColor = ChatColor.valueOf(color);
			}
		}
		return signColor;
	}
	
	public static boolean isColorValid(String color) {
		for (ChatColor chatColor : ChatColor.values()) {
			if (chatColor.name().equals(color)) {
				return true;
			}
		}
		return false;
	}
	
	public static String formatList(List<String> list) {
		String result = "";
		if (list.size() > 0) {
			for (int n=0; n<list.size(); n++) {
				result += (n != 0) ? (n == list.size() -1 ? ChatColor.GRAY + " and " : ChatColor.GRAY + ", ") + ChatColor.GOLD + list.get(n) : ChatColor.GOLD + list.get(n);
			}
		} else {
			result = ChatColor.RED + "none";
		}
		
		return result;
	}
	
	public static String seperateElements(String[] args, String seperator) {
		String cmdStr = "";
		for (int i=0; i<args.length; i++) {
			cmdStr += (i > 0 ? seperator : "") + args[i];
		}
		return cmdStr;
	}
	
	
	public static boolean isInteger(String number) {
		try {
			Integer.parseInt(number);
		} catch(NumberFormatException e) {
			return false;
		}
		return true;
	}
	
	public static boolean isPositiveNumber(String str) {
		if (str == null || str.isEmpty()) {
			return false;
		}
	    for (char c : str.toCharArray()) {
	        if (!Character.isDigit(c)) {
	        	return false;
	        }
	    }
	    return true;
	}
	
	public static boolean isNumeric(String str) {
		return str.matches("-?\\d+(\\.\\d+)?");
	}
	
}
