package net.osmand.plus.wikivoyage.explore.travelcards;

import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;

import net.osmand.plus.OsmandApplication;
import net.osmand.plus.R;

public abstract class BaseTravelCard {

	protected OsmandApplication app;
	protected boolean nightMode;

	public BaseTravelCard(OsmandApplication app, boolean nightMode) {
		this.app = app;
		this.nightMode = nightMode;
	}

	public abstract void bindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder);

	public abstract int getCardType();

	@ColorInt
	protected int getResolvedColor(@ColorRes int colorId) {
		return ContextCompat.getColor(app, colorId);
	}

	protected Drawable getContentIcon(@DrawableRes int icon) {
		return getColoredIcon(icon, R.color.icon_color);
	}

	protected Drawable getActiveIcon(@DrawableRes int icon) {
		return getColoredIcon(icon, R.color.wikivoyage_active_light, R.color.wikivoyage_active_dark);
	}

	protected Drawable getColoredIcon(@DrawableRes int icon, @ColorRes int colorLight, @ColorRes int colorDark) {
		return getColoredIcon(icon, nightMode ? colorDark : colorLight);
	}

	protected Drawable getColoredIcon(@DrawableRes int icon, @ColorRes int color) {
		return app.getUIUtilities().getIcon(icon, color);
	}

	protected boolean isInternetAvailable() {
		return app.getSettings().isInternetConnectionAvailable();
	}

	@DrawableRes
	protected int getPrimaryBtnBgRes(boolean enabled) {
		if (enabled) {
			return nightMode ? R.drawable.wikivoyage_primary_btn_bg_dark : R.drawable.wikivoyage_primary_btn_bg_light;
		}
		return nightMode ? R.drawable.wikivoyage_secondary_btn_bg_dark : R.drawable.wikivoyage_secondary_btn_bg_light;
	}

	@ColorRes
	protected int getPrimaryBtnTextColorRes(boolean enabled) {
		if (enabled) {
			return nightMode ? R.color.wikivoyage_primary_btn_text_dark : R.color.wikivoyage_primary_btn_text_light;
		}
		return R.color.wikivoyage_secondary_text;
	}
}