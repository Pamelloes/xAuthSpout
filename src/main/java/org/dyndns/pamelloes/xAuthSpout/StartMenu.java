 package org.dyndns.pamelloes.xAuthSpout;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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

import com.cypherx.xauth.xAuth;
import com.cypherx.xauth.xAuthPlayer;

public class StartMenu extends GenericPopup {
	private XAuthSpout plugin;
	private xAuth xauth;
	
	private SpoutPlayer player;
	private xAuthPlayer xplayer;
	private Login login;
	public boolean canClose = false;
	
	public StartMenu(XAuthSpout plugin, SpoutPlayer player) {
		this.plugin = plugin;
		this.player = player;

		Plugin p = plugin.getServer().getPluginManager().getPlugin("xAuth");
		if(p==null) throw new RuntimeException("xAuth could not be found, make sure it is installed and working properly.");
		xauth = (xAuth) p;
		xplayer = xauth.getPlayer(player.getName());
		
		plugin.getServer().getPluginManager().registerEvent(Event.Type.CUSTOM_EVENT, new StartMenuScreenListener(), Event.Priority.Normal, plugin);
		
		makePopup();
	}
	
	private void makePopup() {
		int ypos = showMessage();
		
		login = new Login(plugin, xauth, player, xplayer);
		login.setAnchor(WidgetAnchor.TOP_CENTER);
		login.setWidth(320);
		login.setHeight(login.getFieldCount() == 2 ? 50 : 100);
		login.shiftYPos(ypos+20).shiftXPos(-(login.getWidth()/2));
		ypos+=login.getHeight();
		attachWidget(plugin, login);
		
		Button loginbutton = (Button) new LoginButton("Login").setWidth(200).setHeight(20); // Read more about creating widgets in Widgets
		loginbutton.setAlign(WidgetAnchor.TOP_CENTER).setAnchor(WidgetAnchor.TOP_CENTER);
		ypos+=loginbutton.getHeight()+5;
		loginbutton.shiftYPos(ypos).shiftXPos(-100);
		attachWidget(plugin,loginbutton); // Attach the widget to the popup
		
		Button exitbutton = (Button) new ExitButton("Exit").setWidth(200).setHeight(20); // Read more about creating widgets in Widgets
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
			canClose=false;
		}
		
		@Override
		public void onScreenClose(ScreenCloseEvent e) {
			if(!(e.getScreenType()==ScreenType.CUSTOM_SCREEN))return;
			if(!(e.getScreen() instanceof StartMenu)) return;
			e.setCancelled(!canClose);
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
			canClose = true;
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
