package dif.clogic.druzic;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

/**
 * Created with IntelliJ IDEA.
 * User: choeseung-il
 * Date: 13. 9. 28.
 * Time: 오후 3:16
 * To change this template use File | Settings | File Templates.
 */
public class SelectColorActivity extends Activity {

    private int term = 0;

    private RelativeLayout selectView;

    private ImageButton nextButton;
    private ImageButton beforeButton;

    String[] color = { "RED", "MINT", "ORANGE", "BLUE" };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.selectcoloractivity);

        nextButton = (ImageButton)findViewById(R.id.nextButton);
        beforeButton = (ImageButton)findViewById(R.id.beforeButton);

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //To change body of implemented methods use File | Settings | File Templates.
                Intent intent = new Intent(SelectColorActivity.this, AccompanimentActivity.class);
                intent.putExtra("color", color[term%color.length]);
                startActivity(intent);
            }
        });

        beforeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //To change body of implemented methods use File | Settings | File Templates.
                Intent intent = new Intent(SelectColorActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        selectView = (RelativeLayout)findViewById(R.id.selectView);
        selectView.setBackgroundResource(R.drawable.red);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if(event.getAction() == MotionEvent.ACTION_UP)
        {
            term++;
            switch(term%color.length)
            {
                case 0:
                    selectView.setBackgroundResource(R.drawable.red);
                    break;
                case 1:
                    selectView.setBackgroundResource(R.drawable.mint);
                    break;
                case 2:
                    selectView.setBackgroundResource(R.drawable.orange);
                    break;
                case 3:
                    selectView.setBackgroundResource(R.drawable.blue);
                    break;
            }
        }
        return false;
    }
}