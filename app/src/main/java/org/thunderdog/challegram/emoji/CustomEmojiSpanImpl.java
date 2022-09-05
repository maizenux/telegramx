/*
 * This file is a part of Telegram X
 * Copyright © 2014-2022 (tgx-android@pm.me)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * File created on 05/09/2022, 16:39.
 */

package org.thunderdog.challegram.emoji;

import android.graphics.Canvas;
import android.graphics.Rect;

import androidx.annotation.Nullable;
import androidx.annotation.UiThread;

import org.thunderdog.challegram.data.ComplexMediaItem;
import org.thunderdog.challegram.data.ComplexMediaItemCustomEmoji;
import org.thunderdog.challegram.loader.ComplexReceiver;
import org.thunderdog.challegram.telegram.Tdlib;
import org.thunderdog.challegram.telegram.TdlibEmojiManager;
import org.thunderdog.challegram.tool.Paints;
import org.thunderdog.challegram.tool.Views;
import org.thunderdog.challegram.util.text.TextMedia;

import me.vkryl.core.lambda.Destroyable;

class CustomEmojiSpanImpl extends EmojiSpanImpl implements TdlibEmojiManager.Watcher, Destroyable {
  private final CustomEmojiSurfaceProvider surfaceProvider;
  private final Tdlib tdlib;
  private final long customEmojiId;
  @Nullable
  private TdlibEmojiManager.Entry customEmoji;
  private boolean emojiRequested;
  private boolean isDestroyed;

  public CustomEmojiSpanImpl (@Nullable EmojiInfo info, CustomEmojiSurfaceProvider surfaceProvider, Tdlib tdlib, long customEmojiId) {
    super(info);
    this.surfaceProvider = surfaceProvider;
    this.tdlib = tdlib;
    this.customEmojiId = customEmojiId;
    if (customEmojiId != 0) {
      setCustomEmoji(tdlib.emoji().findOrPostponeRequest(customEmojiId, this));
    }
  }

  @Override
  public boolean belongsToSurface (CustomEmojiSurfaceProvider customEmojiSurfaceProvider) {
    return this.surfaceProvider == customEmojiSurfaceProvider && !isDestroyed;
  }

  @Override
  public boolean isCustomEmoji () {
    return true;
  }

  @Override
  public long getCustomEmojiId () {
    return customEmojiId;
  }

  @Override
  public void performDestroy () {
    if (isDestroyed)
      return;
    isDestroyed = true;
    tdlib.emoji().forgetWatcher(customEmojiId, this);
    layoutEmoji(0);
  }

  @UiThread
  private void setCustomEmoji (@Nullable TdlibEmojiManager.Entry customEmoji) {
    if (this.customEmoji == customEmoji)
      return;
    this.customEmoji = customEmoji;
    if (mSize != -1) {
      layoutEmoji(mSize);
      surfaceProvider.onInvalidateSpan(this);
    }
  }

  @Override
  public void onCustomEmojiLoaded (TdlibEmojiManager context, long customEmojiId, TdlibEmojiManager.Entry entry) {
    tdlib.ui().post(() -> {
      if (!isDestroyed) {
        setCustomEmoji(entry);
      }
    });
  }

  @Override
  protected void drawEmoji (Canvas c, float centerX, float centerY, int emojiSize) {
    if (customEmoji == null) {
      return;
    }
    if (customEmoji.isNotFound()) {
      super.drawEmoji(c, centerX, centerY, emojiSize);
      return;
    }

    layoutEmoji(emojiSize);

    Rect rect = Paints.getRect();

    rect.left = (int) (centerX - emojiSize / 2f);
    rect.top = (int) (centerY - emojiSize / 2f);
    rect.right = rect.left + emojiSize;
    rect.bottom = rect.top + emojiSize;

    float scale = TextMedia.getScale(customEmoji.sticker, emojiSize);
    boolean needScale = scale != 1f;

    int restoreToCount;
    if (needScale) {
      restoreToCount = Views.save(c);
      c.scale(scale, scale, centerX, centerY);
    } else {
      restoreToCount = -1;
    }
    mediaItem.draw(c,
      rect,
      surfaceProvider.provideComplexReceiverForSpan(this),
      attachedToMediaKey,
      surfaceProvider.getDuplicateMediaItemCount(this, mediaItem) > 1
    );
    if (needScale) {
      Views.restore(c, restoreToCount);
    }
  }

  private int currentSize;
  private long attachedToMediaKey = -1;

  @Override
  public void requestCustomEmoji (ComplexReceiver receiver, int mediaKey) {
    if (this.attachedToMediaKey != mediaKey)
      throw new IllegalArgumentException();
    if (mediaItem != null) {
      mediaItem.requestComplexMedia(receiver, mediaKey);
    } else {
      receiver.clearReceivers(mediaKey);
      if (customEmoji == null && !emojiRequested) {
        emojiRequested = true;
        tdlib.emoji().performPostponedRequests();
      }
    }
  }

  private ComplexMediaItem mediaItem;

  private void layoutEmoji (int size) {
    if (this.currentSize == size) {
      return;
    }
    if (this.mediaItem != null) {
      surfaceProvider.detachFromReceivers(this, mediaItem, attachedToMediaKey);
      this.mediaItem = null;
      attachedToMediaKey = -1;
    }
    this.currentSize = size;
    if (size > 0) {
      if (customEmoji != null) {
        mediaItem = new ComplexMediaItemCustomEmoji(tdlib, customEmoji.sticker, size);
      }
      attachedToMediaKey = surfaceProvider.attachToReceivers(this, mediaItem);
    }
  }
}