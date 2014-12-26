package com.blundell.viewvideoyoutube;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import static com.blundell.viewvideoyoutube.data.VideoContract.VideoEntry;

public class VideoListAdapter extends CursorAdapter {

    public VideoListAdapter(Context context) {
        super(context, null, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item_video, parent, false);

        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);

        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        ViewHolder viewHolder = (ViewHolder) view.getTag();

        String url = VideoEntry.getThumbnailUrl(cursor);
        Picasso.with(context).load(url).into(viewHolder.thumbnailView);
        viewHolder.detailsView.setText(VideoEntry.getTitle(cursor));
    }

    public static class ViewHolder {
        final ImageView thumbnailView;
        final TextView detailsView;

        public ViewHolder(View view) {
            thumbnailView = (ImageView) view.findViewById(R.id.video_thumbnail);
            detailsView = (TextView) view.findViewById(R.id.video_title);
        }
    }
}
