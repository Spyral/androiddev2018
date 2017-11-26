package xyz.sonbn.ircclient.irc;

import android.os.Binder;

/**
 * Created by sonbn on 11/23/2017.
 */

public class IRCBinder extends Binder {
    private final IRCService mIRCService;

    public IRCBinder(IRCService ircService){
        super();
        mIRCService = ircService;
    }

    public IRCService getService(){
        return mIRCService;
    }
}
