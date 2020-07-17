package com.varenia.vaarta.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.varenia.vaarta.R;

import java.util.ArrayList;
import java.util.List;


public class VideoShowAdapter extends RecyclerView.Adapter<VideoShowAdapter.ViewHolder> {

    private Context context;
    private ArrayList<Integer> users;
    private int parentWidth, parentHeight, enlargeUserId = 0;
    private OnVideoShowAdapterEventListener listener;
    private Boolean enlargeUser = false;

    public VideoShowAdapter(Context context, ArrayList<Integer> users, int parentWidth, int parentHeight) {
        this.context = context;
        this.users = users;
        this.parentWidth = parentWidth;
        this.parentHeight = parentHeight;
        setHasStableIds(true);
    }

    public void setAdapterListener(OnVideoShowAdapterEventListener listener) {
        this.listener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.show_video_row, parent, false);
        ViewHolder holder = new ViewHolder(view);
        holder.getVideoMainFL().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onItemClick(holder.getAdapterPosition());
            }
        });
        holder.getVideoVoiceControl().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onVoiceControl(holder.getAdapterPosition());
            }
        });
        holder.getVideoControl().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onVideoControl(holder.getAdapterPosition());
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        /*resetOriginalViews(holder);
        Integer userId = users.get(position);

        int width = StaticMethods.getWidth(parentWidth, users.size());
        int height = StaticMethods.getHeight(parentHeight, users.size());
        if (enlargeUser) {
            if (userId == enlargeUserId)
                holder.getVideoMainFL().setLayoutParams(new FrameLayout.LayoutParams(parentWidth, parentHeight));
            else {
                //holder.getVideoMainFL().setLayoutParams(new FrameLayout.LayoutParams(0, 0));
                holder.getVideoMainFL().setVisibility(View.GONE);
            }
        } else
            holder.getVideoMainFL().setLayoutParams(new FrameLayout.LayoutParams(width, height));

        QBConferencePeerConnection peerConnection = currentSession.getPeerConnection(userId);
        if (peerConnection != null) {
            QBRTCTypes.QBRTCConnectionState state = peerConnection.getState();
            String connectionStatus = context.getResources().getString(QBRTCSessionUtils.getStatusDescriptionResource(state));
            if (connectionStatus.equals(context.getResources().getString(R.string.text_status_connected)))
                holder.getConnectionStatusTV().setBackground(context.getResources().getDrawable(R.drawable.user_connected));
            else if (connectionStatus.equals(context.getResources().getString(R.string.text_status_disconnected)))
                holder.getConnectionStatusTV().setBackground(context.getResources().getDrawable(R.drawable.user_disconneted));

            // if (!holder.getBinded()) {
            holder.setUserId(userId);
            holder.setBinded(true);
            listener.onBindViewHolder(holder, position);
        *//*} else {
            listener.onBindOnlyVideoTrack(userId, holder);
        }*//*
        }*/
    }

    private void resetOriginalViews(ViewHolder holder) {
        holder.getVideoVoiceControl().setVisibility(View.VISIBLE);
        holder.getVideoMainFL().setVisibility(View.VISIBLE);
        holder.getVideoControl().setVisibility(View.GONE);
        /*holder.setBinded(false);
        holder.getVideoVoiceControl().getBackground().setColorFilter(null);
        holder.getConnectionStatusTV().setVisibility(View.VISIBLE);
        holder.getConnectionStatusTV().setBackground(context.getResources().getDrawable(R.drawable.user_disconneted));*/
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        if (enlargeUser)
            return 1;
        else
            return users.size();
    }

    public List<Integer> getOpponents() {
        return users;
    }

    public void addItem(Integer user) {
        users.add(user);
        notifyItemRangeChanged(0, users.size());
    }

    public void removeUser(Integer user) {
        users.remove(user);
        notifyDataSetChanged();
    }

    public void removeItem(int index) {
        users.remove(index);
        notifyDataSetChanged();
    }

    public void removeAllItems() {
        users.clear();
        notifyDataSetChanged();
    }

    public Boolean doesUserExist(Integer user) {
        return users.contains(user);
    }

    public int getItemQBId(int position) {
        return users.get(position);
    }

    public void enlargeItem(int userId) {
        enlargeUserId = userId;
        enlargeUser = true;
        notifyDataSetChanged();
    }

    public void resetAdapter() {
        enlargeUser = false;
        enlargeUserId = 0;
        notifyDataSetChanged();
    }

    public interface OnVideoShowAdapterEventListener {
        void onBindViewHolder(ViewHolder holder, int position);

        void onItemClick(int position);

        void onVoiceControl(int position);

        void onVideoControl(int position);

        void onBindOnlyVideoTrack(int userId, ViewHolder holder);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private FrameLayout videoMainFL;
        private TextView videoShowUserNameTV, connectionStatusTV, screenSharingTV;
        private int userId;
        private Boolean binded = false;
        private ImageButton videoVoiceControl, videoControl;

        public ViewHolder(View itemView) {
            super(itemView);
            videoMainFL = itemView.findViewById(R.id.videoMainFL);
            videoShowUserNameTV = itemView.findViewById(R.id.videoShowUserNameTV);
            connectionStatusTV = itemView.findViewById(R.id.connectionStatusTV);
            videoVoiceControl = itemView.findViewById(R.id.videoVoiceControl);
            videoControl = itemView.findViewById(R.id.videoControl);
            screenSharingTV = itemView.findViewById(R.id.screenSharingTV);
        }

        public FrameLayout getVideoMainFL() {
            return videoMainFL;
        }

        public void setVideoMainFL(FrameLayout videoMainFL) {
            this.videoMainFL = videoMainFL;
        }

        public int getUserId() {
            return userId;
        }

        public void setUserId(int userId) {
            this.userId = userId;
        }

        public Boolean getBinded() {
            return binded;
        }

        public void setBinded(Boolean binded) {
            this.binded = binded;
        }

        public TextView getVideoShowUserNameTV() {
            return videoShowUserNameTV;
        }

        public void setVideoShowUserNameTV(TextView videoShowUserNameTV) {
            this.videoShowUserNameTV = videoShowUserNameTV;
        }

        public TextView getConnectionStatusTV() {
            return connectionStatusTV;
        }

        public void setConnectionStatusTV(TextView connectionStatusTV) {
            this.connectionStatusTV = connectionStatusTV;
        }

        public ImageButton getVideoVoiceControl() {
            return videoVoiceControl;
        }

        public void setVideoVoiceControl(ImageButton videoVoiceControl) {
            this.videoVoiceControl = videoVoiceControl;
        }

        public ImageButton getVideoControl() {
            return videoControl;
        }

        public void setVideoControl(ImageButton videoControl) {
            this.videoControl = videoControl;
        }

        public TextView getScreenSharingTV() {
            return screenSharingTV;
        }

        public void setScreenSharingTV(TextView screenSharingTV) {
            this.screenSharingTV = screenSharingTV;
        }
    }
}
