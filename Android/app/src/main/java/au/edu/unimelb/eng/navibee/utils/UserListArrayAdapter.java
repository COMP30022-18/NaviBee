package au.edu.unimelb.eng.navibee.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import au.edu.unimelb.eng.navibee.R;
import au.edu.unimelb.eng.navibee.social.UserInfoManager;

public class UserListArrayAdapter extends ArrayAdapter<UserInfoManager.UserInfo> {

    private final LayoutInflater mInflater;
    private final Context mContext;
    private final int mResource;
    private final int mFieldId;

    public UserListArrayAdapter(@NonNull Context context, int resource) {
        super(context, resource);
        mResource = resource;
        mContext = context;
        mInflater = LayoutInflater.from(context);
        mFieldId = 0;
    }

    public UserListArrayAdapter(@NonNull Context context, int resource, int textViewResourceId) {
        super(context, resource, textViewResourceId);
        mResource = resource;
        mContext = context;
        mInflater = LayoutInflater.from(context);
        mFieldId = 0;
    }

    public UserListArrayAdapter(@NonNull Context context, int resource, @NonNull UserInfoManager.UserInfo[] objects) {
        super(context, resource, objects);
        mResource = resource;
        mContext = context;
        mInflater = LayoutInflater.from(context);
        mFieldId = 0;
    }

    public UserListArrayAdapter(@NonNull Context context, int resource, int textViewResourceId, @NonNull UserInfoManager.UserInfo[] objects) {
        super(context, resource, textViewResourceId, objects);
        mResource = resource;
        mContext = context;
        mInflater = LayoutInflater.from(context);
        mFieldId = textViewResourceId;
    }

    public UserListArrayAdapter(@NonNull Context context, int resource, @NonNull List<UserInfoManager.UserInfo> objects) {
        super(context, resource, objects);
        mResource = resource;
        mContext = context;
        mInflater = LayoutInflater.from(context);
        mFieldId = 0;
    }

    public UserListArrayAdapter(@NonNull Context context, int resource, int textViewResourceId, @NonNull List<UserInfoManager.UserInfo> objects) {
        super(context, resource, textViewResourceId, objects);
        mResource = resource;
        mContext = context;
        mInflater = LayoutInflater.from(context);
        mFieldId = textViewResourceId;
    }


    @Override
    public @NonNull
    View getView(int position, @Nullable View convertView,
                 @NonNull ViewGroup parent) {
        return createViewFromResource(mInflater, position, convertView, parent, mResource);
    }

    private @NonNull View createViewFromResource(@NonNull LayoutInflater inflater,
                                                 int position,
                                                 @Nullable View convertView,
                                                 @NonNull ViewGroup parent,
                                                 int resource) {
        final View view;
        final TextView text;

        if (convertView == null) {
            view = inflater.inflate(resource, parent, false);
        } else {
            view = convertView;
        }

        try {
            if (mFieldId == 0) {
                //  If no custom field is assigned, assume the whole resource is a TextView
                text = (TextView) view;
            } else {
                //  Otherwise, find the TextView field within the layout
                text = view.findViewById(mFieldId);

                if (text == null) {
                    throw new RuntimeException("Failed to find view with ID "
                            + mContext.getResources().getResourceName(mFieldId)
                            + " in item layout");
                }
            }
        } catch (ClassCastException e) {
            throw new IllegalStateException(
                    "ArrayAdapter requires the resource ID to be a TextView", e);
        }

        final UserInfoManager.UserInfo item = getItem(position);
        text.setText(item.getName());
        new URLImageViewCacheLoader(item.getPhotoUrl(),
                view.findViewById(R.id.userProfileCheckedTextView_avatar))
                .roundImage(true)
                .execute();
//        new URLRoundCompoundDrawableCacheLoader(
//                item.getPhotoUrl(), text, text.getResources()).execute();

        return view;
    }
}
