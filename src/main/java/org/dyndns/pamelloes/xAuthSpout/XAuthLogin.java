package org.dyndns.pamelloes.xAuthSpout;

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

public class XAuthLogin extends Login {
	private XAuthSpout plugin;
	private xAuth xauth;
	
	private SpoutPlayer player;
	private xAuthPlayer xplayer;

	public XAuthLogin(XAuthSpout plugin, Object xauth, SpoutPlayer player, Object xplayer) {
		super(plugin, player);
		this.player = player;
		this.plugin = plugin;
		this.xauth = (xAuth) xauth;
		this.xplayer=(xAuthPlayer) xplayer;
		makeContainer(this.xplayer.isRegistered() || xAuthSettings.authURLEnabled);
	}

	@Override
	public Object[] doLogin() {
		if(xplayer.hasSession()) xauth.createGuest(xplayer);
		if(xAuthSettings.authURLEnabled || xplayer.isRegistered()) {
			return doLoginRegistered();
		}
		return register();
	}
	
	private Object[] register() {
		String dontmatch = plugin.getConfig().getString("errors.passwordmismatch", "Passwords don't match!");
		if(!getField(1).getText().equals(getField(2).getText())) return new Object[] {false, dontmatch};
		String[] fix = CommandLineTokenizer.tokenize(getField(1).getText() + (getField(3).getText().trim().equals("") ? "" : " " + getField(3).getText()));
		String nopass = plugin.getConfig().getString("errors.nopassword", "You must fill out a password!");
		if(fix.length<1) return new Object[] {false, nopass};
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
