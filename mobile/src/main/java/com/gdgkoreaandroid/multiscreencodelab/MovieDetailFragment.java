package com.gdgkoreaandroid.multiscreencodelab;

import android.app.Fragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.MediaRouteActionProvider;
import android.support.v7.media.MediaRouter;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.gdgkoreaandroid.multiscreencodelab.cast.CastListener;
import com.gdgkoreaandroid.multiscreencodelab.cast.MediaListener;
import com.gdgkoreaandroid.multiscreencodelab.data.Movie;
import com.gdgkoreaandroid.multiscreencodelab.data.MovieList;
import com.google.android.gms.cast.CastDevice;
import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaMetadata;
import com.google.android.gms.cast.MediaStatus;
import com.google.android.gms.cast.RemoteMediaPlayer;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.images.WebImage;

/**
 * A fragment representing a single Movie detail screen.
 * This fragment is either contained in a {@link MovieListActivity}
 * in two-pane mode (on tablets) or a {@link MovieDetailActivity}
 * on handsets.
 */
public class MovieDetailFragment extends Fragment implements CastListener, MediaListener{


    private Movie mMovie;

    private ImageView play;
    private View darkLayer;
    private ProgressBar loadProgress;

    private int mPlayerStatus = MediaStatus.PLAYER_STATE_IDLE;

    public MovieDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(MovieList.ARG_ITEM_ID)) {
            long id = getArguments().getLong(MovieList.ARG_ITEM_ID);
            mMovie = MovieList.getMovie(id);
        }

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_movie_detail, container, false);

        if (mMovie != null) {

            final ImageView thumbnail = (ImageView) rootView.findViewById(R.id.movie_detail_thumb);

            TextView title = (TextView) rootView.findViewById(R.id.movie_detail_title);
            TextView meta = (TextView) rootView.findViewById(R.id.movie_detail_meta);
            TextView description = (TextView) rootView.findViewById(R.id.movie_detail_descritpion);
            play = (ImageView) rootView.findViewById(R.id.movie_detail_play);
            darkLayer = rootView.findViewById(R.id.movie_detail_layer);
            loadProgress = (ProgressBar) rootView.findViewById(R.id.movie_detail_progress);

            View thumbContainer = rootView.findViewById(R.id.movie_detail_thumb_container);

            thumbnail.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

                @Override
                public void onGlobalLayout() {
                    thumbnail.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                    if(thumbnail.isEnabled()){
                        MyApplication.getImageDownloaderInstance().downloadImage(
                                mMovie.getBackgroundImageUrl(), thumbnail);
                    }
                }
            });

            title.setText(mMovie.getTitle());
            meta.setText(mMovie.getStudio());
            description.setText(mMovie.getDescription());

            play.setOnClickListener(mOnPlayVideoHandler);
            thumbContainer.setOnClickListener(mOnPlayVideoHandler);

            // 추가
            if(MyApplication.getCastManager().isApplicationStarted()) {
                MyApplication.getCastManager().attachMediaPlayer();
            }
            updateUiState();
        }

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        MyApplication.getCastManager().registerCastListener(this);
        MyApplication.getCastManager().startDiscovery();
    }

    @Override
    public void onPause() {
        super.onPause();

        MyApplication.getCastManager().stopDiscovery();
        MyApplication.getCastManager().unregisterCastListener();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_movie_detail, menu);
        MenuItem castMenu = menu.findItem(R.id.media_route_menu_item);
        MediaRouteActionProvider mediaRouteActionProvider =
                (MediaRouteActionProvider) MenuItemCompat.getActionProvider(castMenu);
        mediaRouteActionProvider.setRouteSelector(
                MyApplication.getCastManager().getMediaRouteSelector());
    }

    @Override
    public void onRouteSelected(MediaRouter router, MediaRouter.RouteInfo route) {
        MyApplication.getCastManager().connect();
    }

    @Override
    public void onRouteUnselected(MediaRouter router, MediaRouter.RouteInfo route) {
        mPlayerStatus = MediaStatus.PLAYER_STATE_IDLE;
        updateUiState();
    }

    @Override
    public void onConnected(Bundle bundle) {
        CastDevice dev = MyApplication.getCastManager().getCurrentDevice();
        Toast.makeText(getActivity(),
                "Connected to " + dev.getFriendlyName(), Toast.LENGTH_SHORT).show();
        MyApplication.getCastManager().launchApplication();
    }

    @Override
    public void onConnectionSuspended(int cause) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

        Toast.makeText(getActivity(), "Failed to connect", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onApplicationLaunched(boolean wasLaunched) {

        MyApplication.getCastManager().attachMediaPlayer();
    }

    @Override
    public void onApplicationStatusChanged() {

    }

    @Override
    public void onVolumeChanged() {

    }

    @Override
    public void onApplicationDisconnected(int statusCode) {

    }

    @Override
    public void onMediaLoaded(RemoteMediaPlayer.MediaChannelResult result) {

    }

    @Override
    public void onMediaLoadFailed(RemoteMediaPlayer.MediaChannelResult result) {

    }

    @Override
    public void onMediaControl(int controlType, RemoteMediaPlayer.MediaChannelResult result) {

    }

    @Override
    public void onMediaMetadataUpdated() {

    }

    @Override
    public void onMediaStatusUpdated(MediaStatus status) {
        mPlayerStatus = status.getPlayerState();
        // Set controller visibility according to playback state.
        updateUiState();
    }

    private void updateUiState() {
        darkLayer.setVisibility(
                MyApplication.getCastManager().isApplicationStarted() ? View.VISIBLE : View.GONE);
        switch (mPlayerStatus) {
            case MediaStatus.PLAYER_STATE_PLAYING:
                play.setVisibility(View.VISIBLE);
                play.setImageResource(R.drawable.ic_pause_playcontrol_normal);
                loadProgress.setVisibility(View.GONE);
                break;
            case MediaStatus.PLAYER_STATE_PAUSED:
            case MediaStatus.PLAYER_STATE_IDLE:
                play.setVisibility(View.VISIBLE);
                play.setImageResource(R.drawable.play_button);
                loadProgress.setVisibility(View.GONE);
                break;
            case MediaStatus.PLAYER_STATE_BUFFERING:
                play.setVisibility(View.GONE);
                loadProgress.setVisibility(View.VISIBLE);
                break;
        }
    }
    private final View.OnClickListener mOnPlayVideoHandler = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // If there is no cast device connected, launch video player.
            if(!MyApplication.getCastManager().isConnected()) {
                Intent intent = new Intent(v.getContext(), PlayerActivity.class);
                intent.putExtra(MovieList.ARG_ITEM_ID, mMovie.getId());
                intent.putExtra(v.getContext().getString(R.string.should_start), true);
//                postNotifications();
                v.getContext().startActivity(intent);
            } else {
                switch(mPlayerStatus) {
                    case MediaStatus.PLAYER_STATE_IDLE:
                        MediaMetadata metadata = new MediaMetadata(MediaMetadata.MEDIA_TYPE_MOVIE);
                        metadata.putString(MediaMetadata.KEY_TITLE, mMovie.getTitle());
                        metadata.putString(MediaMetadata.KEY_STUDIO, mMovie.getStudio());
                        metadata.addImage(new WebImage(Uri.parse(mMovie.getCardImageUrl())));
                        MediaInfo info = new MediaInfo.Builder(mMovie.getVideoUrl())
                                .setMetadata(metadata)
                                .setContentType("video/mp4")
                                .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED).build();
                        MyApplication.getCastManager().loadMedia(info);
                        break;
                    case MediaStatus.PLAYER_STATE_PAUSED:
                        MyApplication.getCastManager().playMedia();
                        break;
                    case MediaStatus.PLAYER_STATE_PLAYING:
                        MyApplication.getCastManager().pauseMedia();
                        break;
                }
                // Play video on the cast device
            }
        }
    };
}
