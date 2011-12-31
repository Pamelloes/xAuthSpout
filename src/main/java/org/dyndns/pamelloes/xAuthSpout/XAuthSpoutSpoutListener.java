package org.dyndns.pamelloes.xAuthSpout;

import org.getspout.spoutapi.event.spout.SpoutCraftEnableEvent;
import org.getspout.spoutapi.event.spout.SpoutListener;

public class XAuthSpoutSpoutListener extends SpoutListener {
	private XAuthSpout plugin;
	
	public XAuthSpoutSpoutListener(XAuthSpout plugin) {
		this.plugin = plugin;
	}
	
	@Override
	public void onSpoutCraftEnable(final SpoutCraftEnableEvent e) {
		plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
			public void run() {
				StartMenu sm = new StartMenu(plugin, e.getPlayer());
				e.getPlayer().getMainScreen().attachPopupScreen(sm);
			}
		}, 10L);
	}
}
