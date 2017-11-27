package vn.edu.usth.irc;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;


public class PeopleFragment extends Fragment {

    private String[] peopleNameArray = {"Asrg","Bjgri","Ciwour","Dueq","Eihudos","Fuhbn","Galknln","Hiaf"};

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_people, container, false);

        ArrayAdapter arrayAdapter = new ArrayAdapter<>(getContext(), R.layout.people_list_row, peopleNameArray);

        ListView listView = (ListView) view.findViewById(R.id.list_people);
        listView.setAdapter(arrayAdapter);

        return view;
    }
}
