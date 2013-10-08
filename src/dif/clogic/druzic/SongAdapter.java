package dif.clogic.druzic;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: choeseung-il
 * Date: 13. 10. 3.
 * Time: 오후 8:02
 * To change this template use File | Settings | File Templates.
 */
public class SongAdapter extends ArrayAdapter<Song> {

    private ArrayList<Song> items;
    private Context mCtx;
    private int resLayout;

    public SongAdapter(Context context, int textViewResourceId, ArrayList<Song> items) {
        super(context, textViewResourceId, items);
        this.mCtx = context;
        this.items = items;
        this.resLayout = textViewResourceId;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        if(v == null) {
            LayoutInflater vi = (LayoutInflater)mCtx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(resLayout, parent, false);
        }
        Song s = items.get(position);
        CheckBox cb = (CheckBox)v.findViewById(R.id.checkbox);
        cb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //To change body of implemented methods use File | Settings | File Templates.
                view.setSelected(!view.isSelected());
            }
        });
        if( s != null) {
            TextView text = (TextView)v.findViewById(R.id.filename);

            if(text != null) {
                text.setText(s.Name);
            }
        }
        return v;
    }
}