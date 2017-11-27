package vn.edu.usth.irc;

import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class ServerFragment extends Fragment {

    private String[] serverNameArray = {"Android","IPhone","WindowsMobile","Blackberry","WebOS","Ubuntu","Windows7","Max OS X","A","B","C","D","E","F","G"};

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_server, container, false);

        ArrayAdapter arrayAdapter = new ArrayAdapter<>(getContext(), R.layout.server_list_row, serverNameArray);

        ListView listView = (ListView) view.findViewById(R.id.list_server);
        listView.setAdapter(arrayAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TabLayout tabLayout = (TabLayout) getActivity().findViewById(R.id.tab);
                tabLayout.getTabAt(0).select();
            }
        });

        return view;
    }
}
