package au.edu.unimelb.eng.navibee;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

import androidx.preference.ListPreference;

public class FriendListPreference extends ListPreference {

    public FriendListPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FriendListPreference(Context context) {
        super(context);
    }

    protected View onCreateDialogView() {

        ListView view = new ListView(getContext());
        view.setAdapter(adapter());
        setEntries(entries());
        setEntryValues(entryValues());
//        setValueIndex(initializeIndex());
        return view;

    }

    private ListAdapter adapter() {
        return new ArrayAdapter(getContext(), android.R.layout.select_dialog_singlechoice);
    }

    private CharSequence[] entries() {
        //action to provide entry data in char sequence array for list
        return null;
    }

    private CharSequence[] entryValues() {
        //action to provide value data for list
        return null;
    }

}
