package org.dyndns.pamelloes.xAuthSpout;

import org.bukkit.event.Event;
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

public class ResultPopup extends GenericPopup {
	private XAuthSpout plugin;
	
	private SpoutPlayer player;
	
	private boolean returnOnExit;
	private StartMenu returnTo;
	
	public ResultPopup(XAuthSpout plugin, StartMenu menu, Object[] data) {
		this.plugin = plugin;
		
		player = menu.getPlayer();
		
		returnOnExit = !(Boolean) data[0];
		returnTo = menu;
		
		String[] message = ((String) data[1]).split("\n");
		makeWithMessage(message);
		
		plugin.getServer().getPluginManager().registerEvent(Event.Type.CUSTOM_EVENT, new ResultScreenListener(), Event.Priority.Normal, plugin);
	}
	
	private void makeWithMessage(String[] message) {
		int ypos = 0;
		Label label;
		for(String s : message) {
			label = new GenericLabel(s);
			label.setX(0).setY(0);
			label.setTextColor(new Color(1.0F, 0, 0, 0.0F)); //This makes the label red.
			label.setAlign(WidgetAnchor.TOP_CENTER).setAnchor(WidgetAnchor.TOP_CENTER).setWidth(getWidth()).setHeight(13); //This puts the label at top center and align the text correctly.
			ypos+=label.getHeight();
			label.shiftYPos(ypos);
			label.setAuto(true);
			attachWidget(plugin,label); // Attach the widget to the popup
		}
		Button button = (Button) new FinishButton("Continue").setWidth(200).setHeight(20); // Read more about creating widgets in Widgets
		button.setAlign(WidgetAnchor.TOP_CENTER).setAnchor(WidgetAnchor.TOP_CENTER);
		ypos+=button.getHeight()+5;
		button.shiftYPos(ypos).shiftXPos(-100);
		attachWidget(plugin,button); // Attach the widget to the popup
	}
	
	private class ResultScreenListener extends ScreenListener {
		@Override
		public void onScreenClose(ScreenCloseEvent e) {
			if(!(e.getScreenType()==ScreenType.CUSTOM_SCREEN))return;
			if (!(e.getScreen() instanceof ResultPopup)) return;
			if (returnOnExit) {
				plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
					public void run() {
						player.getMainScreen().attachPopupScreen(returnTo);
					}
				});
			}
		}
	}
	
	private class FinishButton extends GenericButton {
		
		public FinishButton(String text) {
			super(text);
		}
		
		@Override
		public void onButtonClick(ButtonClickEvent e) {
			ResultPopup.this.player.getMainScreen().closePopup();
		}
	}
}
