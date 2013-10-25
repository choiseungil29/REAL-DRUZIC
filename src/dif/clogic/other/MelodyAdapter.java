package dif.clogic.other;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import dif.clogic.app.R;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: choeseung-il
 * Date: 13. 10. 16.
 * Time: 오전 9:36
 * To change this template use File | Settings | File Templates.
 */
public class MelodyAdapter extends ArrayAdapter<Melody> {

    private ArrayList<Melody> items;
    private Context mCtx;
    private int resLayout;

    public MelodyAdapter(Context context, int textViewResourceId, ArrayList<Melody> items) {
        super(context, textViewResourceId, items);
        this.mCtx = context;
        this.items = items;
        this.resLayout = textViewResourceId;
    }

    public View getView(final int position, View convertView, ViewGroup parent) {
        View v = convertView;
        if(v == null) {
            LayoutInflater vi = (LayoutInflater)mCtx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(resLayout, parent, false);
        }
        Melody s = items.get(position);
        if( s != null) {
            TextView text = (TextView)v.findViewById(R.id.filename);

            if(text != null) {
                text.setText(s.Name);
            }
        }
        return v;
    }
}
