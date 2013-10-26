package dif.clogic.other;

import android.content.Context;
import android.graphics.Typeface;
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
 * Date: 13. 10. 27.
 * Time: 오전 5:29
 * To change this template use File | Settings | File Templates.
 */
public class MainAdapter extends ArrayAdapter<String> {

    private final Context context;
    private final ArrayList<String> itemsArrayList;

    public MainAdapter(Context context, String[] itemsArrayList) {

        super(context, R.layout.row, itemsArrayList);

        this.context = context;
        this.itemsArrayList = new ArrayList<String>();
        for(String str : itemsArrayList) {
            this.itemsArrayList.add(str);
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        // 1. Create inflater
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        // 2. Get rowView from inflater
        View rowView = inflater.inflate(R.layout.row, parent, false);

        // 3. Get the two text view from the rowView
        TextView labelView = (TextView) rowView.findViewById(R.id.filename);

        // 4. Set the text for textView
        labelView.setText(itemsArrayList.get(position));
        labelView.setTypeface(Typeface.createFromAsset(context.getAssets(), "nanumN.ttf"));

        // 5. retrn rowView
        return rowView;
    }
}
