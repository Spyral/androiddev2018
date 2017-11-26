package xyz.sonbn.ircclient.activity;

import android.support.v7.widget.Toolbar;

import xyz.sonbn.ircclient.model.Server;

public interface ClientActivity {
    Toolbar getToolbar();

    void setToolbarTitle(String title);
    void onServerSelected(Server server);
}
