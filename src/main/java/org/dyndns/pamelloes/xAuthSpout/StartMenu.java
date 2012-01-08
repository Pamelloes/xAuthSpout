 package org.dyndns.pamelloes.xAuthSpout;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.bukkit.event.Event;
import org.bukkit.plugin.Plugin;
import org.getspout.spoutapi.event.screen.ButtonClickEvent;
import org.getspout.spoutapi.event.screen.ScreenCloseEvent;
import org.getspout.spoutapi.event.screen.ScreenListener;
import org.getspout.spoutapi.event.screen.ScreenOpenEvent;
import org.getspout.spoutapi.gui.Button;
import org.getspout.spoutapi.gui.Color;
import org.getspout.spoutapi.gui.GenericButton;
import org.getspout.spoutapi.gui.GenericLabel;
import org.getspout.spoutapi.gui.GenericPopup;
import org.getspout.spoutapi.gui.Label;
import org.getspout.spoutapi.gui.ScreenType;
import org.getspout.spoutapi.gui.WidgetAnchor;
import org.getspout.spoutapi.player.SpoutPlayer;

public class StartMenu extends GenericPopup {
	private XAuthSpout plugin;
	private Object xauth;
	private boolean needsxAuth = true;
	
	private SpoutPlayer player;
	private Object xplayer;
	private Login login;
	public static Map<StartMenu,Boolean> canClose = new HashMap<StartMenu,Boolean>();
	
	public StartMenu(XAuthSpout plugin, SpoutPlayer player) {
		this.plugin = plugin;
		this.player = player;

		needsxAuth = (plugin.getConfig().getString("modes.login","xauth").equalsIgnoreCase("xAuth")) || (plugin.getConfig().getString("modes.register","xauth").equalsIgnoreCase("xAuth"));
		if(needsxAuth) {
			try {
				Plugin p = plugin.getServer().getPluginManager().getPlugin("xAuth");
				if(p==null) throw new RuntimeException("xAuth could not be found, make sure it is installed and working properly.");
				Class<?> clazz = Class.forName("com.cypherx.xauth.xAuth");
				if(!clazz.isInstance(p)) throw new RuntimeException("xAuth is not an instance of com.cypherx.xauth.xAuth. Make sure you are using the correct plugin.");
				xauth = p;
				Method m = clazz.getMethod("getPlayer", String.class);
				xplayer = m.invoke(xauth,player.getName());
			} catch (Exception e) {
				e.printStackTrace();
				xauth = null;
				xplayer = null;
			}
		} else {
			xauth = null;
			xplayer = null;
		}
		
		plugin.getServer().getPluginManager().registerEvent(Event.Type.CUSTOM_EVENT, new StartMenuScreenListener(), Event.Priority.Normal, plugin);
		
		makePopup();
	}
	
	private void makePopup() {
		int ypos = showMessage();
		
		if(needsxAuth) login = new XAuthLogin(plugin, xauth, player, xplayer);
		login.setAnchor(WidgetAnchor.TOP_CENTER);
		login.setWidth(320);
		login.setHeight(login.getFieldCount() == 2 ? 50 : 100);
		login.shiftYPos(ypos+20).shiftXPos(-(login.getWidth()/2));
		ypos+=login.getHeight();
		attachWidget(plugin, login);
		
		String logintext= login.getFieldCount()==2 ? plugin.getConfig().getString("login.loginbutton", "Login") : plugin.getConfig().getString("login.registerbutton", "Register");
		Button loginbutton = (Button) new LoginButton(logintext).setWidth(200).setHeight(20); // Read more about creating widgets in Widgets
		loginbutton.setAlign(WidgetAnchor.TOP_CENTER).setAnchor(WidgetAnchor.TOP_CENTER);
		ypos+=loginbutton.getHeight()+5;
		loginbutton.shiftYPos(ypos).shiftXPos(-100);
		attachWidget(plugin,loginbutton); // Attach the widget to the popup
		
		String exit = plugin.getConfig().getString("login.exitbutton", "Exit");
		Button exitbutton = (Button) new ExitButton(exit).setWidth(200).setHeight(20); // Read more about creating widgets in Widgets
		exitbutton.setX(0).setY(0);
		exitbutton.setAlign(WidgetAnchor.TOP_CENTER).setAnchor(WidgetAnchor.TOP_CENTER);
		ypos+=exitbutton.getHeight()+4;
		exitbutton.shiftYPos(ypos).shiftXPos(-100);
		attachWidget(plugin,exitbutton); // Attach the widget to the popup

	}
	
	private int showMessage() {
		int ypos = 0;
		Label label;
		
		String title = plugin.getConfig().getString("message.title", "Welcome to the Server!");
		label = new GenericLabel(title);
		label.setX(0).setY(0);
		label.setTextColor(new Color(1.0F, 0, 0, 1.0F)); //This makes the label red.
		label.setAlign(WidgetAnchor.TOP_CENTER).setAnchor(WidgetAnchor.TOP_CENTER).setWidth(getWidth()).setHeight(13); //This puts the label at top center and align the text correctly.
		ypos+=label.getHeight();
		label.shiftYPos(ypos);
		label.setAuto(true);
		attachWidget(plugin,label); // Attach the widget to the popup
		
		@SuppressWarnings("serial")
		List<Object> list = plugin.getConfig().getList("message.contents", new ArrayList<String>() {
			{
				add("This server requires user authentication.");
				add("If this is your first time put in your password, and then confirm it in the confirm dialogue.");
				add("Otherwise, put in your registered password and click login.");
				add("Have fun!");
			}
		});
		Iterator<Object> i = list.iterator();
		while(i.hasNext()) {
			label = new GenericLabel(i.next().toString());
			label.setX(0).setY(0);
			label.setTextColor(new Color(1.0F, 1.0F, 1.0F)); //This makes the label red.
			label.setAlign(WidgetAnchor.TOP_CENTER).setAnchor(WidgetAnchor.TOP_CENTER).setWidth(getWidth()).setHeight(13); //This puts the label at top center and align the text correctly.
			ypos+=label.getHeight();
			label.shiftYPos(ypos);
			label.setAuto(true);
			attachWidget(plugin,label); // Attach the widget to the popup
		}
		
		return ypos;
	}
	
	private class StartMenuScreenListener extends ScreenListener {
		
		@Override
		public void onScreenOpen(ScreenOpenEvent e) {
			if(!(e.getScreenType()==ScreenType.CUSTOM_SCREEN))return;
			if(!(e.getScreen() instanceof StartMenu)) return;
			canClose.put(StartMenu.this, false);
		}
		
		@Override
		public void onScreenClose(ScreenCloseEvent e) {
			if(!(e.getScreenType()==ScreenType.CUSTOM_SCREEN))return;
			if(!(e.getScreen() instanceof StartMenu)) return;
			if(!e.getScreen().equals(StartMenu.this)) return;
			boolean canclose = canClose.get(StartMenu.this);
			e.setCancelled(!canclose);
			if(canclose) canClose.remove(StartMenu.this);
		}
	}
	
	private class LoginButton extends GenericButton {
		
		public LoginButton(String text) {
			super(text);
		}
		
		@Override
		public void onButtonClick(ButtonClickEvent e) {
			Object[] result = login.doLogin();
			if(result == null) return;
			ResultPopup rp = new ResultPopup(StartMenu.this.plugin, StartMenu.this, result);
			canClose.put(StartMenu.this, true);
			StartMenu.this.player.getMainScreen().closePopup();
			StartMenu.this.player.getMainScreen().attachPopupScreen(rp);
		}
	}
	
	private class ExitButton extends GenericButton {
		
		public ExitButton(String text) {
			super(text);
		}
		
		@Override
		public void onButtonClick(ButtonClickEvent e) {
			getPlayer().kickPlayer("");
		}
	}
}
