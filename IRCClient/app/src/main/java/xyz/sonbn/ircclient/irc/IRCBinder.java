package xyz.sonbn.ircclient.irc;

import android.os.Binder;

import xyz.sonbn.ircclient.model.Server;

/**
 * Created by sonbn on 11/23/2017.
 */

public class IRCBinder extends Binder {
    private final IRCService mIRCService;

    public IRCBinder(IRCService ircService){
        super();
        mIRCService = ircService;
    }

    public void connect(final Server server){
        mIRCService.connect(server);
    }

    public IRCService getService(){
        return mIRCService;
    }
}
