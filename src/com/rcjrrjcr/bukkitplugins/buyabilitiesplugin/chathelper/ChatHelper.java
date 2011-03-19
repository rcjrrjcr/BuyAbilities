package com.rcjrrjcr.bukkitplugins.buyabilitiesplugin.chathelper;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;


/**
 * A utility class with functions that help manage printing messages to the player
 * @author rcjrrjcr
 */
public class ChatHelper {

	private final static Integer lineLength = 50;
	/**
	 * Breaks up a string of text into lines that fit into player's chat and sends to player 
	 * @param color Text colour
	 * @param msg Message string
	 * @param player Player entity
	 * @return Number of lines printed
	 */
	public static int sendMsgWrap(ChatColor color, String msg, Player player)
	{
		ArrayList<String> wrappedMsg = wrapLines(msg,color);
		for(String s : wrappedMsg)
		{
			player.sendMessage(s);
		}
		return wrappedMsg.size();
	}
	public static int sendMsgWrap(String msg, CommandSender sender)
	{
		ArrayList<String> wrappedMsg = wrapLines(msg,null);
		for(String s : wrappedMsg)
		{
			sender.sendMessage(s);
		}
		return wrappedMsg.size();
	}
	/**
	 * Formats and sends a page of data to player's chat.
	 * @author rcjrrjcr
	 * @param pageHeader Page header
	 * @param color Text colour
	 * @param data Array of lines of data
	 * @param linesPerPage Lines per page including header;
	 * @param pageNo Page number
	 * @param player Player entity
	 */
	public static void paging(String pageHeader, ChatColor color, List<String> data, final int linesPerPage, final int pageNo, Player player)
	{
		if(pageNo < 1) return;
		if(player == null) return;
		int pageLines = linesPerPage - 1; //Lines of data not including header
		if(pageLines < 1) return;
		int start = (pageNo - 1) * pageLines; //Start position in string array
//		System.out.println(data.size());
		if(start > (data.size()-1))
		{
			sendMsgWrap(color,"BuyPermissions: No such page.",player);
			return;
		}
		int end = start + pageLines; //End position in string array (first string not to be processed)
		if(end > (data.size() - 1))
		{
			end = data.size();
		}
		int pageCount = divRoundUp(data.size(),pageLines);
		if(pageNo > pageCount) 
		{
			sendMsgWrap(color,"BuyPermissions: No such page.",player);
			return;
		}
		String header = new String(pageHeader + "- Page " + String.valueOf(pageNo)+ "/" +String.valueOf(pageCount));
		start += sendMsgWrap(color,header,player) - 1;
		for(int i = start; i < end; i++)
		{
			i += sendMsgWrap(color,data.get(i),player) - 1;
		}
		return;
	}
	/**
	 * Splits message into strings that fit in player's chat (i.e. each string's length < lineLength)
	 * @param msg Message to split
	 * @param color Text colour
	 * @return ArrayList of split strings
	 */
	public static ArrayList<String> wrapLines(String msg, ChatColor color)
	{
		if(color == null)
		{
			return wrapText(msg,lineLength);
		}
		else
		{
			ArrayList<String> splitMsg =  wrapText(msg, (lineLength - color.toString().length()) );
			for(String line : splitMsg)
			{
				line = color.toString() + line;
			}
			return splitMsg;
		}
	}
	
	/**
	 * COPYCODE: Line wrapping algorithm from <a href="http://progcookbook.blogspot.com/2006/02/text-wrapping-function-for-java.html">Programmer's Cookbook</a> All credit goes to them.
	 * Minor modifications (i.e force-wrapping when newline char is found, returning ArrayList<String>.) by rcjrrjcr.
	 * @param text Text to wrap
	 * @param len Line length
	 * @return Array of wrapped strings
	 * @author Robert Hanson 
	 */
	
	public static ArrayList<String> wrapText (String text, int len)
	{
		// return empty array for null text
		if (text == null) return new ArrayList<String>();

		// return text if len is zero or less
		// return text if less than length
		if (len <= 0||text.length() <= len)
		{
			ArrayList<String> line = new ArrayList<String>();
			line.add(text);
			return line;
		}


		char [] chars = text.toCharArray();
		ArrayList<String> lines = new ArrayList<String>();
		StringBuffer line = new StringBuffer();
		StringBuffer word = new StringBuffer();

		for (int i = 0; i < chars.length; i++) {
			word.append(chars[i]);

			if (chars[i] == ' ') {
				if ((line.length() + word.length()) > len) {
					lines.add(line.toString());
					line.delete(0, line.length());
				}

				line.append(word);
				word.delete(0, word.length());
			}
			else if (chars[i] == '\n') {
				lines.add(line.toString());
				line.delete(0, line.length());
				line.append(word);
				word.delete(0, word.length());		
			}
		}
		// handle any extra chars in current word
		if (word.length() > 0) {
			if ((line.length() + word.length()) > len) {
				lines.add(line.toString());
				line.delete(0, line.length());
			}
			line.append(word);
		}

		// handle extra line
		if (line.length() > 0) {
			lines.add(line.toString());
		}
		return lines;
	}
	/**
	 * Splits array of message into strings that fit in players chat. Refer to other overload.
	 * @param msg ArrayList of messages to split
	 * @param color Text colour
	 * @return ArrayList of split strings
	 */
	public static ArrayList<String> wrapLines(ArrayList<String> msg, ChatColor color)
	{
		ArrayList<String> wrappedMsg = new ArrayList<String>(msg.size());
		for(String s : msg)
		{
			wrappedMsg.addAll(wrapLines(s,color));
		}
		return wrappedMsg;
	}
	/**
	 * Concatenates strings. If a string is null, return the other. If both are null, return null;
	 * @param s1 First string
	 * @param s2 Second string
	 * @return Concatenated string
	 */
	public static String conc(final String s1, final String s2)
	{
		if(s1==null&&s2==null) return null;
		if(s1==null) return s2;
		if(s2==null) return s1;
		return s1.concat(s2);
	}
	
	/**
	 * Divides i1 by i2 and rounds up
	 * @param i1 Dividend
	 * @param i2 Divisor
	 * @return i1/i2 rounded up
	 */
	public static int divRoundUp(final int i1, final int i2)
	{
		return ((i1/i2)+((i1%i2==0)? 0 : 1));
	}
}
