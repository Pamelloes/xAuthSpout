package org.dyndns.pamelloes.xAuthSpout;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.configuration.file.FileConfiguration;
import org.getspout.spoutapi.event.screen.TextFieldChangeEvent;
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

/**
 * A container containing a textfields and labels for a login. Only one
 * configuration is supported at a time, though as many Logins as needed can
 * be made,
 * 
 * @author Joshua Brot
 *
 */
public abstract class Login extends GenericContainer {
	private XAuthSpout plugin;
	
	private SpoutPlayer player;
	
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
	public Login(XAuthSpout plugin, SpoutPlayer player) {
		this.plugin = plugin;
		this.player=player;
		partsString = new HashMap<String, TextField>();
		partsInt = new HashMap<Integer, TextField>();
		
		configure();
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
		
		textheight = 20;
		textwidth = 200;
		
		linetext = new String[4];
		linetext[0] = config.getString("login.username", "Username:");
		linetext[1] = config.getString("login.password", "Password:");
		linetext[2] = config.getString("login.confirm", "Confirm password:");
		linetext[3] = config.getString("login.email", "Email (optional):");
		linehidechar = new boolean[] {false,true,true,false};
	}
	
	/**
	 * Sets up this widget.
	 * 
	 * @param registered wether or not the player is registered, if false, then a register gui is shown.
	 */
	protected void makeContainer(boolean registered) {
		setLayout(ContainerType.VERTICAL);
		Container container;
		
		Label label;
		TextField textfield;

		int lines;
		if(registered) {
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
			
			textfield = new GenericTextField() {
				@Override
				public void onTextFieldChange(TextFieldChangeEvent e) {
					setFocus(true);
				}
			};
			textfield.setAnchor(WidgetAnchor.TOP_LEFT).setWidth(textwidth).setHeight(textheight); //This puts the label at top center and align the text correctly.
			textfield.setMaximumCharacters(32);
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
	public abstract Object[] doLogin();
}
