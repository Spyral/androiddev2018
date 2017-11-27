package xyz.sonbn.ircclient.activity;

import android.support.v7.widget.Toolbar;

import xyz.sonbn.ircclient.irc.IRCBinder;
import xyz.sonbn.ircclient.model.Server;

public interface ClientActivity {
    Toolbar getToolbar();
    IRCBinder getBinder();

    void setToolbarTitle(String title);
    void onServerSelected(Server server);
}
