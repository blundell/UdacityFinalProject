package com.blundell.viewvideoyoutube;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import static com.blundell.viewvideoyoutube.data.VideoContract.VideoEntry;

public class DetailsFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    static final String EXTRA_URI = "happy.christmas.extra";
    private static final int ID = 9;

    private ImageView imageView;
    private TextView titleView;
    private TextView descriptionView;
    private TextView durationView;
    private Uri uri;

    public DetailsFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        uri = getArguments().getParcelable(EXTRA_URI);
        Log.d("YouTube", "Got extra uri: " + uri);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_details, container, false);

        imageView = (ImageView) rootView.findViewById(R.id.details_image);
        titleView = (TextView) rootView.findViewById(R.id.details_title);
        descriptionView = (TextView) rootView.findViewById(R.id.details_description);
        durationView = (TextView) rootView.findViewById(R.id.details_duration);

        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getLoaderManager().initLoader(ID, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity(), uri, null, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data != null && data.moveToFirst()) {

            Picasso.with(getActivity()).load(getBestThumbnail(data)).into(imageView);
            titleView.setText(VideoEntry.getTitle(data));
            descriptionView.setText(formatDescription(data));
            durationView.setText(formatDuration(VideoEntry.getDuration(data)));
        }
    }

    private String getBestThumbnail(Cursor cursor) {
        String thumbnailUrl = VideoEntry.getHighQualityThumbnailUrl(cursor);
        if (TextUtils.isEmpty(thumbnailUrl)) {
            thumbnailUrl = VideoEntry.getThumbnailUrl(cursor);
        }
        return thumbnailUrl;
    }

    private String formatDescription(Cursor cursor) {
        String description = VideoEntry.getDescription(cursor);
        if (TextUtils.isEmpty(description)) {
            return getString(R.string.oops_no_description);
        } else {
            return description;
        }
    }

    private String formatDuration(long duration) {
        return getString(R.string.youtube_duration_format, duration);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
