package com.hiroshi.cimoc.ui.adapter;

import android.content.Context;
import android.graphics.Rect;
import android.net.Uri;
import androidx.recyclerview.widget.RecyclerView;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.common.ResizeOptions;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.google.android.gms.ads.formats.MediaView;
import com.google.android.gms.ads.formats.NativeAd;
import com.google.android.gms.ads.formats.UnifiedNativeAd;
import com.google.android.gms.ads.formats.UnifiedNativeAdView;
import com.hiroshi.cimoc.App;
import com.hiroshi.cimoc.R;
import com.hiroshi.cimoc.fresco.ControllerBuilderProvider;
import com.hiroshi.cimoc.manager.PreferenceManager;
import com.hiroshi.cimoc.manager.SourceManager;
import com.hiroshi.cimoc.model.MiniComic;
import com.hiroshi.cimoc.utils.FrescoUtils;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import butterknife.BindView;

/**
 * Created by Hiroshi on 2016/7/1.
 */
public class GridAdapter extends BaseAdapter<Object> {

    public static final int TYPE_GRID = 2016101213;
    private static final int UNIFIED_NATIVE_AD_VIEW_TYPE_GRID = 2020121201;

    private ControllerBuilderProvider mProvider;
    private SourceManager.TitleGetter mTitleGetter;
    private boolean symbol = false;

    public GridAdapter(Context context, List<Object> list) {
        super(context, list);
    }

    @Override
    public int getItemViewType(int position) {
        Object recyclerViewItem = mDataSet.get(position);
        if (recyclerViewItem instanceof UnifiedNativeAd) {
            return UNIFIED_NATIVE_AD_VIEW_TYPE_GRID;
        }
        return TYPE_GRID;
    }

    @NotNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case UNIFIED_NATIVE_AD_VIEW_TYPE_GRID:
                View unifiedNativeLayoutView = mInflater.inflate(R.layout.item_grid_ad, parent, false);
                return new UnifiedNativeAdViewHolder(unifiedNativeLayoutView);
            case TYPE_GRID:
            default:
                View view = mInflater.inflate(R.layout.item_grid, parent, false);
                return new GridHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        int viewType = getItemViewType(position);
        switch (viewType) {
            case UNIFIED_NATIVE_AD_VIEW_TYPE_GRID:
                UnifiedNativeAd nativeAd = (UnifiedNativeAd) mDataSet.get(position);
                populateNativeAdView(nativeAd, ((UnifiedNativeAdViewHolder) holder).getAdView());
                break;
            case TYPE_GRID:
            default:
                MiniComic comic = (MiniComic)mDataSet.get(position);
                GridHolder gridHolder = (GridHolder) holder;
                gridHolder.comicTitle.setText(comic.getTitle());
                gridHolder.comicSource.setText(mTitleGetter.getTitle(comic.getSource()));
                if (mProvider != null) {
                    //            ImageRequest request = ImageRequestBuilder
                    //                    .newBuilderWithSource(Uri.parse(comic.getCover()))
                    //                    .setResizeOptions(new ResizeOptions(App.mCoverWidthPixels / 3, App.mCoverHeightPixels / 3))
                    //                    .build();
                    ImageRequest request = null;
                    try {
                        if (!App.getManager_wifi().isWifiEnabled() && App.getPreferenceManager().getBoolean(PreferenceManager.PREF_OTHER_CONNECT_ONLY_WIFI, false)) {
                            //                    request = null;
                            if (FrescoUtils.isCached(comic.getCover())) {
                                request = ImageRequestBuilder
                                        .newBuilderWithSource(Uri.fromFile(FrescoUtils.getFileFromDiskCache(comic.getCover())))
                                        .setResizeOptions(new ResizeOptions(App.mCoverWidthPixels / 3, App.mCoverHeightPixels / 3))
                                        .build();
                            }
                        } else if (!App.getManager_wifi().isWifiEnabled() && App.getPreferenceManager().getBoolean(PreferenceManager.PREF_OTHER_LOADCOVER_ONLY_WIFI, false)) {
                            //                    request = null;
                            if (FrescoUtils.isCached(comic.getCover())) {
                                request = ImageRequestBuilder
                                        .newBuilderWithSource(Uri.fromFile(FrescoUtils.getFileFromDiskCache(comic.getCover())))
                                        .setResizeOptions(new ResizeOptions(App.mCoverWidthPixels / 3, App.mCoverHeightPixels / 3))
                                        .build();
                            }
                        } else {
                            if (FrescoUtils.isCached(comic.getCover())) {
                                request = ImageRequestBuilder
                                        .newBuilderWithSource(Uri.fromFile(FrescoUtils.getFileFromDiskCache(comic.getCover())))
                                        .setResizeOptions(new ResizeOptions(App.mCoverWidthPixels / 3, App.mCoverHeightPixels / 3))
                                        .build();
                            } else {
                                request = ImageRequestBuilder
                                        .newBuilderWithSource(Uri.parse(comic.getCover()))
                                        .setResizeOptions(new ResizeOptions(App.mCoverWidthPixels / 3, App.mCoverHeightPixels / 3))
                                        .build();
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    DraweeController controller = mProvider.get(comic.getSource())
                            .setOldController(gridHolder.comicImage.getController())
                            .setImageRequest(request)
                            .build();
                    gridHolder.comicImage.setController(controller);
                }
                gridHolder.comicHighlight.setVisibility(symbol && comic.isHighlight() ? View.VISIBLE : View.INVISIBLE);
        }
    }

    public void setProvider(ControllerBuilderProvider provider) {
        mProvider = provider;
    }

    public void setTitleGetter(SourceManager.TitleGetter getter) {
        mTitleGetter = getter;
    }

    public void setSymbol(boolean symbol) {
        this.symbol = symbol;
    }

    @Override
    public RecyclerView.ItemDecoration getItemDecoration() {
        return new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                int offset = parent.getWidth() / 90;
                outRect.set(offset, 0, offset, (int) (2.8 * offset));
            }
        };
    }

    public void removeItemById(long id) {
        for (Object O_comic : mDataSet) {
            if (O_comic instanceof UnifiedNativeAd){
                continue;
            }
            MiniComic comic = (MiniComic) O_comic;
            if (id == comic.getId()) {
                remove(comic);
                break;
            }
        }
    }

    public int findFirstNotHighlight() {
        int count = 0;
        if (symbol) {
            for (Object O_comic : mDataSet) {
                if (O_comic instanceof UnifiedNativeAd){
                    continue;
                }
                MiniComic comic = (MiniComic) O_comic;
                if (!comic.isHighlight()) {
                    break;
                }
                ++count;
            }
        }
        return count;
    }

    public void cancelAllHighlight() {
        int count = 0;
        for (Object O_comic : mDataSet) {
            if (O_comic instanceof UnifiedNativeAd){
                continue;
            }
            MiniComic comic = (MiniComic) O_comic;
            if (!comic.isHighlight()) {
                break;
            }
            ++count;
            comic.setHighlight(false);
        }
        notifyItemRangeChanged(0, count);
    }

    public void moveItemTop(MiniComic comic) {
        if (remove(comic)) {
            add(findFirstNotHighlight(), comic);
        }
    }

    static class GridHolder extends BaseViewHolder {
        @BindView(R.id.item_grid_image)
        SimpleDraweeView comicImage;
        @BindView(R.id.item_grid_title)
        TextView comicTitle;
        @BindView(R.id.item_grid_subtitle)
        TextView comicSource;
        @BindView(R.id.item_grid_symbol)
        View comicHighlight;

        GridHolder(View view) {
            super(view);
        }
    }

    class UnifiedNativeAdViewHolder extends RecyclerView.ViewHolder {

        private UnifiedNativeAdView adView;

        public UnifiedNativeAdView getAdView() {
            return adView;
        }

        UnifiedNativeAdViewHolder(View view) {
            super(view);
            adView = (UnifiedNativeAdView) view.findViewById(R.id.ad_grid_view);

            adView.setMediaView((MediaView) adView.findViewById(R.id.ad_grid_media));
            adView.setIconView(adView.findViewById(R.id.ad_grid_app_icon));
            adView.setHeadlineView(adView.findViewById(R.id.ad_grid_headline));
            adView.setBodyView(adView.findViewById(R.id.ad_grid_body));
            adView.setStarRatingView(adView.findViewById(R.id.ad_grid_stars));
            adView.setAdvertiserView(adView.findViewById(R.id.ad_grid_advertiser));
        }
    }

    private void populateNativeAdView(UnifiedNativeAd nativeAd, UnifiedNativeAdView adView) {
        ((TextView) adView.getHeadlineView()).setText(nativeAd.getHeadline());
        adView.getMediaView().setMediaContent(nativeAd.getMediaContent());

        NativeAd.Image icon = nativeAd.getIcon();
        if (icon == null) {
            adView.getIconView().setVisibility(View.INVISIBLE);
        } else {
            ((ImageView) adView.getIconView()).setImageDrawable(icon.getDrawable());
            adView.getIconView().setVisibility(View.VISIBLE);
        }

        if (nativeAd.getBody() == null) {
            adView.getBodyView().setVisibility(View.INVISIBLE);
        } else {
            adView.getBodyView().setVisibility(View.VISIBLE);
            ((TextView) adView.getBodyView()).setText(nativeAd.getBody());
        }

        if (nativeAd.getStarRating() == null) {
            adView.getStarRatingView().setVisibility(View.INVISIBLE);
        } else {
            ((RatingBar) adView.getStarRatingView())
                    .setRating(nativeAd.getStarRating().floatValue());
            adView.getStarRatingView().setVisibility(View.VISIBLE);
        }

        if (nativeAd.getAdvertiser() == null) {
            adView.getAdvertiserView().setVisibility(View.INVISIBLE);
        } else {
            ((TextView) adView.getAdvertiserView()).setText(nativeAd.getAdvertiser());
            adView.getAdvertiserView().setVisibility(View.VISIBLE);
        }

        // Assign native ad object to the native view.
        adView.setNativeAd(nativeAd);
    }
}
