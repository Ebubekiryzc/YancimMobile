package tr.edu.duzce.mf.bm.yancimmobile.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import tr.edu.duzce.mf.bm.yancimmobile.R;
import tr.edu.duzce.mf.bm.yancimmobile.model.GameType;

public class GameTypeAdapter extends RecyclerView.Adapter<GameTypeAdapter.GameTypeAdapterViewHolder> {

    private List<GameType> gameTypes;

    public GameTypeAdapter(List<GameType> gameTypes) {
        this.gameTypes = gameTypes;
    }

    @Override
    public GameTypeAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.element_game_type, parent, false);
        return new GameTypeAdapterViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull GameTypeAdapterViewHolder holder, int position) {
        GameType gameType = gameTypes.get(position);
        holder.textView.setText(gameType.getName());
        holder.imageView.setImageResource(gameType.getDrawableId());
    }

    @Override
    public int getItemCount() {
        return gameTypes.size();
    }

    class GameTypeAdapterViewHolder extends RecyclerView.ViewHolder {

        private TextView textView;
        private ImageView imageView;

        public GameTypeAdapterViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.elementGameTypeText);
            imageView = itemView.findViewById(R.id.elementGameTypeImage);
        }
    }
}
