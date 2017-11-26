/*
Yaaic - Yet Another Android IRC Client

Copyright 2009-2013 Sebastian Kaspari

This file is part of Yaaic.

Yaaic is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Yaaic is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Yaaic.  If not, see <http://www.gnu.org/licenses/>.
 */
package xyz.sonbn.ircclient.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


import java.util.List;

import xyz.sonbn.ircclient.R;
import xyz.sonbn.ircclient.model.Server;
import xyz.sonbn.ircclient.util.AppManager;

/**
 * RecyclerView adapter for server cards.
 */
public class ServersAdapter extends RecyclerView.Adapter<ServersAdapter.ViewHolder> {
    public interface ClickListener {
        void onServerSelected(Server server);
        void onConnectToServer(Server server);
        void onDisconnectFromServer(Server server);
        void onEditServer(Server server);
        void onDeleteServer(Server server);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final TextView titleView;
        public final TextView hostView;

        public ViewHolder(View view, ClickListener listener) {
            super(view);

            titleView = (TextView) view.findViewById(R.id.title);
            hostView = (TextView) view.findViewById(R.id.host);
        }
    }

    private List<Server> servers;
    private ClickListener listener;

    public ServersAdapter(ClickListener listener) {
        this.listener = listener;

        loadServers();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.
                from(parent.getContext()).
                inflate(R.layout.item_server, parent, false);

        return new ViewHolder(view, listener);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final Server server = servers.get(position);

//        int colorResource = server.isConnected() ? R.color.connected : R.color.disconnected;
//        int color = holder.itemView.getContext().getResources().getColor(colorResource);

        holder.titleView.setText(server.getTitle());
//        holder.titleView.setTextColor(color);
        String host = server.getNickname() + "@" + server.getHost() + ":" + server.getPort();
        holder.hostView.setText(host);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onServerSelected(server);
            }
        });
    }

    public void loadServers() {
        this.servers = AppManager.getInstance().getServers();
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return servers.size();
    }
}
