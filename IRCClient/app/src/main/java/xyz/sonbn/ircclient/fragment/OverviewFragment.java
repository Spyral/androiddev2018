package xyz.sonbn.ircclient.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import io.realm.Realm;
import xyz.sonbn.ircclient.R;
import xyz.sonbn.ircclient.activity.AddServerActivity;
import xyz.sonbn.ircclient.activity.ClientActivity;
import xyz.sonbn.ircclient.adapter.ServersAdapter;
import xyz.sonbn.ircclient.irc.IRCBinder;
import xyz.sonbn.ircclient.listener.ServerListener;
import xyz.sonbn.ircclient.model.Broadcast;
import xyz.sonbn.ircclient.model.Extra;
import xyz.sonbn.ircclient.model.Server;
import xyz.sonbn.ircclient.model.Status;
import xyz.sonbn.ircclient.receiver.ServerReceiver;
import xyz.sonbn.ircclient.util.AppManager;

public class OverviewFragment extends Fragment implements View.OnClickListener, ServersAdapter.ClickListener, ServerListener {
    public static final String TRANSACTION_TAG = "fragment_overview";

    private ClientActivity activity;
    private ServersAdapter mServersAdapter;
    private ServerReceiver mServerReceiver;
    private Realm mRealm;

    public OverviewFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (!(context instanceof ClientActivity)) {
            throw new IllegalArgumentException("Activity has to implement YaaicActivity interface");
        }

        this.activity = (ClientActivity) context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_overview, container, false);

        mServersAdapter = new ServersAdapter(this);

        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        recyclerView.setAdapter(mServersAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));

        FloatingActionButton addServerBtn = (FloatingActionButton) view.findViewById(R.id.fab);
        addServerBtn.setOnClickListener(this);

        mRealm = Realm.getDefaultInstance();
//        addServerBtn.setOnLongClickListener(new View.OnLongClickListener() {
//            @Override
//            public boolean onLongClick(View v) {
//                activity.onServerSelected(new Server());
//                return true;
//            }
//        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        activity.setToolbarTitle(getString(R.string.app_name));
        mServerReceiver = new ServerReceiver(this);
        getActivity().registerReceiver(mServerReceiver, new IntentFilter(Broadcast.SERVER_UPDATE));
        mServersAdapter.loadServers();
    }

    @Override
    public void onPause() {
        super.onPause();

        getActivity().unregisterReceiver(mServerReceiver);
    }

    @Override
    public void onClick(View view) {
        final Context context = view.getContext();
        Log.d(TRANSACTION_TAG, "Click");
        Intent intent = new Intent(context, AddServerActivity.class);
        context.startActivity(intent);
    }


    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onServerSelected(Server server) {
        activity.onServerSelected(server);
    }

    @Override
    public void onConnectToServer(final Server server) {
        IRCBinder binder = activity.getBinder();

        if (binder != null && server.getStatus() == Status.DISCONNECTED){
            binder.connect(server);
            mRealm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    server.setStatus(Status.CONNECTING);
                }
            });
            mServersAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onDisconnectFromServer(final Server server) {
        IRCBinder binder = activity.getBinder();

        if (binder != null) {
            server.clearConversations();
            mRealm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    server.setStatus(Status.DISCONNECTED);
                }
            });
            binder.getService().getConnection(server.getId()).quitServer();
        }
    }

    @Override
    public void onEditServer(Server server) {
        if (server.getStatus() != Status.DISCONNECTED) {
            Toast.makeText(getActivity(), getResources().getString(R.string.disconnect_before_editing), Toast.LENGTH_SHORT).show();
        } else {
            Intent intent = new Intent(getActivity(), AddServerActivity.class);
            intent.putExtra(Extra.SERVER, server.getId());
            startActivityForResult(intent, 0);
        }
    }

    @Override
    public void onDeleteServer(final Server server) {
        IRCBinder binder = activity.getBinder();

        if (binder != null) {
            binder.getService().getConnection(server.getId()).quitServer();

            mRealm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    mRealm.where(Server.class).equalTo("id", server.getId()).findAll().deleteAllFromRealm();
                }
            });

            AppManager.getInstance().removeServerById(server.getId());

            getActivity().sendBroadcast(
                    Broadcast.createServerIntent(Broadcast.SERVER_UPDATE, server.getId())
            );
        }
    }

    @Override
    public void onStatusUpdate() {
        mServersAdapter.loadServers();
    }
}
