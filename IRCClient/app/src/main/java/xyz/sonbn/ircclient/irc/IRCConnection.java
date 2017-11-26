package xyz.sonbn.ircclient.irc;

import java.util.ArrayList;

import xyz.sonbn.ircclient.model.Server;
import xyz.sonbn.ircclient.util.AppManager;

/**
 * Created by sonbn on 11/25/2017.
 */

public class IRCConnection extends IRCProtocol {
    private final String TAG = "IRCConnection";

    private IRCService mIRCService;
    private final Server mServer;
    private ArrayList<String> autoJoinChannels;

    public IRCConnection(IRCService service, int serverId){
        mIRCService = service;
        mServer = AppManager.getInstance().getServerById(serverId);
    }

    public final void sendMessage(String target, String message) {
        //("PRIVMSG " + target + " :" + message);
    }
}
