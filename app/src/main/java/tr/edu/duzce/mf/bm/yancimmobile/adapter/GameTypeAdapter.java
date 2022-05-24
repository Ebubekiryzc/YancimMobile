package tr.edu.duzce.mf.bm.yancimmobile.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.lang.reflect.Method;
import java.util.List;

import tr.edu.duzce.mf.bm.yancimmobile.R;
import tr.edu.duzce.mf.bm.yancimmobile.helpers.abstracts.ItemClickListener;
import tr.edu.duzce.mf.bm.yancimmobile.model.GameType;

public class GameTypeAdapter extends RecyclerView.Adapter<GameTypeAdapter.GameTypeAdapterViewHolder> {

    private List<GameType> gameTypes;
    private Context context;
    private ItemClickListener itemClickListener;

    public GameTypeAdapter(Context context, List<GameType> gameTypes, ItemClickListener itemClickListener) {
        this.context = context;
        this.gameTypes = gameTypes;
        this.itemClickListener = itemClickListener;
    }

    @Override
    public GameTypeAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.element_game_type, parent, false);
        return new GameTypeAdapterViewHolder(itemView, itemClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull GameTypeAdapterViewHolder holder, int position) {
        GameType gameType = gameTypes.get(position);
        holder.textView.setText(gameType.getName());

        if (gameType.getImage() == null) {
            holder.imageView.setImageResource(R.drawable.ic_no_image);
            holder.imageView.setColorFilter(R.color.primaryLightColor);
        } else {
            holder.imageView.setColorFilter(null);
            Glide.with(context).load(gameType.getImage()).into(holder.imageView);
        }
    }

    @Override
    public int getItemCount() {
        return gameTypes.size();
    }

    class GameTypeAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView textView;
        private ImageView imageView;
        private ItemClickListener itemClickListener;

        public GameTypeAdapterViewHolder(@NonNull View itemView, ItemClickListener itemClickListener) {
            super(itemView);
            textView = itemView.findViewById(R.id.elementGameTypeText);
            imageView = itemView.findViewById(R.id.elementGameTypeImage);
            this.itemClickListener = itemClickListener;

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            itemClickListener.onItemClick(getAdapterPosition());
        }
    }
}
