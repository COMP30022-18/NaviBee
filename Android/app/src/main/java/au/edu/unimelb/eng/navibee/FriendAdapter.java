package au.edu.unimelb.eng.navibee;

import java.util.ArrayList;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.os.AsyncTask;
import java.io.InputStream;
import android.graphics.BitmapFactory;
import android.util.Log;

public class FriendAdapter extends BaseAdapter {
    private ArrayList<ContactPerson> contactList;
    private LayoutInflater l_Inflater;

    public FriendAdapter(Context context, ArrayList<ContactPerson> contactList){
        this.contactList = contactList;
        l_Inflater = LayoutInflater.from(context);
    }

    public int getCount(){
        return contactList.size();
    }
    public Object getItem(int position){
        return contactList.get(position);
    }
    public long getItemId(int position){
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent){
        ViewHolder holder;
        if (convertView == null) {
            convertView = l_Inflater.inflate(R.layout.friend_item, null);
            holder = new ViewHolder();
            holder.image = (ImageView) convertView.findViewById(R.id.friend_icon);
            holder.text = (TextView) convertView.findViewById(R.id.friend_name);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        if (contactList.size() <= 0){
            holder.text.setText("No Data");
        }
        else{
            ContactPerson tempPerson = contactList.get(position);
            holder.text.setText(tempPerson.getName());
            new DownloadImageTask(holder.image)
                    .execute(tempPerson.getUrl());
            //holder.image.setImageResource(R.drawable.ic_navibee_color);
        }

        return convertView;
    }

    public static class ViewHolder {
        public ImageView image;
        public TextView text;
    }
    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }
}
