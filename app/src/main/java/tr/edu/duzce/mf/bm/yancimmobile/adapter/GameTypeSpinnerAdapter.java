package tr.edu.duzce.mf.bm.yancimmobile.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.List;

import tr.edu.duzce.mf.bm.yancimmobile.R;
import tr.edu.duzce.mf.bm.yancimmobile.model.GameType;

public class GameTypeSpinnerAdapter extends BaseAdapter {
    private Context context;
    private List<GameType> gameTypes;

    public GameTypeSpinnerAdapter(Context context, List<GameType> gameTypes) {
        this.context = context;
        this.gameTypes = gameTypes;
    }

    @Override
    public int getCount() {
        return gameTypes != null ? gameTypes.size() : 0;
    }

    @Override
    public Object getItem(int i) {
        return gameTypes.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        View rootView = LayoutInflater.from(context)
                .inflate(R.layout.game_type_spinner_item, viewGroup, false);


        GameType gameType = (GameType) this.getItem(i);

        TextView textView = rootView.findViewById(R.id.gameTypeSpinnerText);
        ImageView imageView = rootView.findViewById(R.id.gameTypeSpinnerImage);

        textView.setText(gameType.getName());
        Glide.with(context).load(gameType.getImage()).into(imageView);

        return rootView;
    }
}
