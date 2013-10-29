package dif.clogic.other;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import dif.clogic.app.R;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: choeseung-il
 * Date: 13. 10. 3.
 * Time: 오후 8:02
 * To change this template use File | Settings | File Templates.
 */
public class AccompanimentAdapter extends ArrayAdapter<Accompaniment> {

    private ArrayList<Accompaniment> items;
    private Context mCtx;
    private int resLayout;

    public AccompanimentAdapter(Context context, int textViewResourceId, ArrayList<Accompaniment> items) {
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

            final CheckBox cb = (CheckBox)v.findViewById(R.id.checkbox);
            if(cb != null) {
                cb.setChecked(false);
                cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                        //To change body of implemented methods use File | Settings | File Templates.
                        items.get(position).checked = b;
                    }
                });
            }

            Accompaniment s = items.get(position);
            if( s != null) {
                TextView text = (TextView)v.findViewById(R.id.filename);

                if(text != null) {
                    text.setText(s.Name);
                    text.setTypeface(Typeface.createFromAsset(mCtx.getAssets(), "nanumN.ttf"));
                }
            }
        }
        return v;
    }
}