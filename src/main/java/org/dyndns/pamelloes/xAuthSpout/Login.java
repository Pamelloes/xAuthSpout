package org.dyndns.pamelloes.xAuthSpout;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.configuration.file.FileConfiguration;
import org.getspout.spoutapi.gui.Color;
import org.getspout.spoutapi.gui.Container;
import org.getspout.spoutapi.gui.ContainerType;
import org.getspout.spoutapi.gui.GenericContainer;
import org.getspout.spoutapi.gui.GenericLabel;
import org.getspout.spoutapi.gui.GenericTextField;
import org.getspout.spoutapi.gui.Label;
import org.getspout.spoutapi.gui.TextField;
import org.getspout.spoutapi.gui.WidgetAnchor;
import org.getspout.spoutapi.player.SpoutPlayer;

import com.cypherx.xauth.Account;
import com.cypherx.xauth.xAuth;
import com.cypherx.xauth.xAuthLog;
import com.cypherx.xauth.xAuthMessages;
import com.cypherx.xauth.xAuthPlayer;
import com.cypherx.xauth.xAuthSettings;
import com.cypherx.xauth.database.DbUtil;
import com.cypherx.xauth.util.Util;
import com.cypherx.xauth.util.Validator;
import com.cypherx.xauth.util.encryption.Encrypt;
import com.martiansoftware.jsap.CommandLineTokenizer;

/**
 * A container containing a textfields and labels for a login. Only one
 * configuration is supported at a time, though as many Logins as needed can
 * be made,
 * 
 * @author Joshua Brot
 *
 */
public class Login extends GenericContainer {
	private XAuthSpout plugin;
	private xAuth xauth;
	
	private SpoutPlayer player;
	private xAuthPlayer xplayer;
	
	private Map<String, TextField> partsString;
	private Map<Integer, TextField> partsInt;
	
	private int textheight;
	private int textwidth;
	
	private String register;
	private String[] linetext;
	private boolean[] linehidechar;

	/**
	 * Creates a new Login.
	 */
	public Login(XAuthSpout plugin, xAuth xauth, SpoutPlayer player, xAuthPlayer xplayer) {
		this.plugin = plugin;
		this.player=player;
		this.xauth = xauth;
		this.xplayer=xplayer;
		partsString = new HashMap<String, TextField>();
		partsInt = new HashMap<Integer, TextField>();
		
		configure();
		
		makeContainer();
	}
	
	/**
	 * Maps a textfield to the given id and name.
	 */
	private void registerField(TextField field, String name, int id) {
		partsString.put(name, field);
		partsInt.put(id, field);
	}
	
	/**
	 * Unmaps a textfield from its id and name.
	 */
	@SuppressWarnings("unused")
	private void unregisterField(TextField field) {
		partsString.remove(partsString.get(field));
		partsInt.remove(partsInt.get(field));
	}
	
	/**
	 * Gets a textfield with the given field.
	 */
	public TextField getField(String name) {
		return partsString.get(name);
	}
	
	/**
	 * Gets a textfield with the given id.
	 */
	public TextField getField(int id) {
		return partsInt.get(id);
	}
	
	/**
	 * How many textfields are in this Container.
	 */
	public int getFieldCount() {
		return partsInt.size();
	}
	
	/**
	 * Loads the configuration.
	 */
	private void configure() {
		FileConfiguration config = plugin.getConfig();
		
		register = config.getString("login.register","Fill out the form to register for the server.");
		
		textheight = config.getInt("login.fieldheight", 20);
		textwidth = config.getInt("login.fieldwidth",200);
		
		linetext = new String[] {"Username:","Password:","Confirm password:","Email (optional):"};
		linehidechar = new boolean[] {false,true,true,false};
	}
	
	/**
	 * Sets up this widget.
	 * 
	 * @param player The player to configure for.
	 */
	private void makeContainer() {
		setLayout(ContainerType.VERTICAL);
		Container container;
		
		Label label;
		TextField textfield;

		int lines;
		if(xplayer.isRegistered() || xAuthSettings.authURLEnabled) {
			lines = 2;
		} else {
			lines = 4;

			label = new GenericLabel(register);
			label.setTextColor(new Color(1.0F, 1.0F, 1.0F, 1.0F));
			label.setAlign(WidgetAnchor.TOP_LEFT).setAnchor(WidgetAnchor.TOP_CENTER).setWidth(90 + textwidth).setHeight(textheight); //This puts the label at top center and align the text correctly.
			label.setAuto(true);
			addChild(label);
		}
		
		for(int i=0;i<lines;i++) {
			container = new GenericContainer();
			container.setLayout(ContainerType.HORIZONTAL);
			container.setAlign(WidgetAnchor.TOP_LEFT);
			
			label = new GenericLabel(linetext[i]);
			label.setTextColor(new Color(1.0F, 1.0F, 1.0F, 1.0F));
			label.setAlign(WidgetAnchor.TOP_LEFT).setAnchor(WidgetAnchor.TOP_LEFT).setWidth(90).setHeight(textheight); //This puts the label at top center and align the text correctly.
			label.setAuto(true);
			container.addChild(label); // Attach the widget to the popup
			
			textfield = new GenericTextField();
			textfield.setAnchor(WidgetAnchor.TOP_LEFT).setWidth(textwidth).setHeight(textheight); //This puts the label at top center and align the text correctly.
			if(linehidechar[i]) textfield.setPasswordField(true);
			container.addChild(textfield); // Attach the widget to the popuplabel = new GenericLabel("User Name:");
			
			addChild(container);
			registerField(textfield, linetext[i], i);
		}

		getField(0).setText(player.getName()).setEnabled(false);
	}

	/**
	 * Attempts to log in.
	 * 
	 * @return True if login successful, false otherwise.
	 * @throws UnsupportedOperationException If a unsupported login
	 * configuration is used.
	 */
	public Object[] doLogin() {
		if(xplayer.hasSession()) xauth.createGuest(xplayer);
		if(xAuthSettings.authURLEnabled || xplayer.isRegistered()) {
			return doLoginRegistered();
		}
		return register();
	}
	
	private Object[] register() {
		if(!getField(1).getText().equals(getField(2).getText())) return new Object[] {false, "Passwords don't match!"};
		String[] fix = CommandLineTokenizer.tokenize(getField(1).getText() + (getField(3).getText().trim().equals("") ? "" : " " + getField(3).getText()));
		if(fix.length<1) return new Object[] {false, "You must fill out a password!"};
		String password = fix[0];
		String email = fix.length>1 ? fix[1] : null;
		if (!xAuthSettings.regEnabled) {
			return new Object[] {false, xAuthMessages.get("regErrDisabled", player, null)};
		} else if (xAuthSettings.requireEmail && email==null) {
			return new Object[] {false, "You need to specify an email."};
		}
		if (!Validator.isValidPass(password)) {
			return new Object[]{false,xAuthMessages.get("regErrPassword", player, null)};
		} else if (xAuthSettings.validateEmail && !Validator.isValidEmail(email)) {
			return new Object[]{false,xAuthMessages.get("regErrEmail", player, null)};
		}
		xplayer.setAccount(new Account(player.getName(), Encrypt.custom(password), email));
		xauth.login(xplayer);

		xAuthLog.info(player.getName() + " has registered!");
		return new Object[]{true, xAuthMessages.get("regSuccess", player, null)};
	}
	
	private Object[] doLoginRegistered() {
		String password = getField(1).getText();
		Account account = xplayer.getAccount();
		if(xAuthSettings.authURLEnabled && account == null){
			account = new Account(player.getName(), "authURL", null);
			xplayer.setAccount(account);
		}
		if (!xauth.checkPassword(account, password)) {
			if (xAuthSettings.maxStrikes > 0) {
				String host = Util.getHostFromPlayer(player);
				DbUtil.insertStrike(host, player.getName());
				int strikes = DbUtil.getStrikeCount(host);

				if (strikes >= xAuthSettings.maxStrikes) {
					player.kickPlayer(xAuthMessages.get("miscKickStrike", player, null));
					xAuthLog.info(player.getName() + " has exceeded the incorrect password threshold.");
					return null;
				}
			}

			return new Object[] {false,xAuthMessages.get("loginErrPassword", player, null) };
		}
		
		int active = DbUtil.getActive(player.getName());
		account.setActive(active);

		if (xAuthSettings.activation && active == 0) {
			return new Object[] {false, xAuthMessages.get("loginErrActivate", player, null)};
		}

		xauth.login(xplayer);
		xAuthLog.info(player.getName() + " has logged in");
		return new Object[] {true, xAuthMessages.get("loginSuccess", player,null)};
	}
}
