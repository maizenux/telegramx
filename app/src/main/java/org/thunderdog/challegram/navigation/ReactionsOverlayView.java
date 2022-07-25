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
 */
package org.thunderdog.challegram.navigation;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;

import org.thunderdog.challegram.component.sticker.TGStickerObj;
import org.thunderdog.challegram.data.TD;
import org.thunderdog.challegram.loader.ImageFile;
import org.thunderdog.challegram.loader.ImageReceiver;
import org.thunderdog.challegram.loader.gif.GifFile;
import org.thunderdog.challegram.loader.gif.GifReceiver;
import org.thunderdog.challegram.tool.Paints;

import java.util.ArrayList;

import me.vkryl.android.AnimatorUtils;
import me.vkryl.android.animator.FactorAnimator;
import me.vkryl.core.lambda.Destroyable;

public class ReactionsOverlayView extends ViewGroup {

  private final ArrayList<ReactionInfo> activePopups = new ArrayList<>();
  private final int[] position = new int[2];

  public ReactionsOverlayView (Context context) {
    super(context);
    setWillNotDraw(false);
  }

  @Override
  protected void onAttachedToWindow () {
    super.onAttachedToWindow();
    getLocationOnScreen(position);
  }


  @Override
  protected void onMeasure (int widthMeasureSpec, int heightMeasureSpec) {
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    reposition();
  }

  @Override
  protected void onLayout (boolean changed, int l, int t, int r, int b) {
    reposition();
  }

  @Override
  protected void onDraw (Canvas c) {
    for (ReactionInfo info : activePopups) {
      info.draw(c);
    }
  }

  public void addOffset (int dx, int dy) {
    for (ReactionInfo info : activePopups) {
      info.addOffset(dx, dy);
    }
    invalidate();
  }

  public void reposition () {
    int viewWidth = getMeasuredWidth();
    int viewHeight = getMeasuredHeight();
    if (viewWidth > 0 && viewHeight > 0) {
      invalidate();
    }
  }

  public void addOverlay (TGStickerObj stickerObj, Rect position) {
    ReactionInfo info = new ReactionInfo(this).setSticker(stickerObj).setPosition(position);
    addOverlay(info);
  }

  public void addOverlay (ReactionInfo info) {
    info.setRemoveListener(() -> this.removeOverlay(info));
    activePopups.add(info);
    info.attach();
    invalidate();
  }


  public void removeOverlay (ReactionInfo info) {
    activePopups.remove(info);
    info.detach();
    info.performDestroy();
    invalidate();
  }


  public interface LocationProvider {
    void getTargetBounds (View targetView, Rect outRect);
  }

  public interface OffsetProvider {
    void onProvideOffset (RectF rect);
  }


  public static class ReactionInfo implements Destroyable, FactorAnimator.Target {
    private final ReactionsOverlayView parentView;

    private final ImageReceiver imageReceiver;
    private final GifReceiver gifReceiver;
    private GifFile animation;
    private Runnable removeRunnable;

    // animation
    private static final int POSITION_ANIMATOR = 0;
    @Nullable
    private AnimatedPositionProvider animatedPositionProvider;
    @Nullable
    private AnimatedPositionOffsetProvider animatedPositionOffsetProvider;
    @Nullable
    private OnFinishAnimationListener endAnimationListener;
    @Nullable
    private FactorAnimator positionAnimator;
    @Nullable
    private Rect startPosition;
    @Nullable
    private Rect finishPosition;
    private long duration;

    // ?
    private final Point animationOffsetPoint = new Point(0, 0);
    private Rect position;


    public ReactionInfo (ReactionsOverlayView parentView) {
      this.parentView = parentView;
      imageReceiver = new ImageReceiver(parentView, 0);
      gifReceiver = new GifReceiver(parentView);
    }

    public ReactionInfo setSticker (TGStickerObj sticker) {
      ImageFile imageFile = sticker.getImage();
      animation = sticker.getPreviewAnimation();
      if (animation != null) {
        animation.setPlayOnce(true);
        animation.setLooped(false);
        gifReceiver.requestFile(animation);
      }
      imageReceiver.requestFile(imageFile);
      parentView.invalidate();
      return this;
    }

    public ReactionInfo setAnimatedPosition (Rect startPosition, Rect finishPosition, AnimatedPositionProvider animatedPositionProvider, long duration) {
      this.animatedPositionProvider = animatedPositionProvider;
      this.startPosition = startPosition;
      this.finishPosition = finishPosition;
      this.duration = duration;
      this.positionAnimator = new FactorAnimator(POSITION_ANIMATOR, this, AnimatorUtils.DECELERATE_INTERPOLATOR, duration, 0f);
      return setPosition(new Rect(startPosition));
    }

    public ReactionInfo setAnimatedPosition (Point startPosition, Point finishPosition, int size, AnimatedPositionProvider animatedPositionProvider, long duration) {
      return setAnimatedPosition(startPosition, finishPosition, size, size, animatedPositionProvider, duration);
    }

    public ReactionInfo setAnimatedPosition (Point startPosition, Point finishPosition, int startSize, int endSize, AnimatedPositionProvider animatedPositionProvider, long duration) {
      this.animatedPositionProvider = animatedPositionProvider;
      this.startPosition = new Rect(startPosition.x - startSize / 2, startPosition.y - startSize / 2, startPosition.x + startSize / 2, startPosition.y + startSize / 2);
      this.finishPosition = new Rect(finishPosition.x - endSize / 2, finishPosition.y - endSize / 2, finishPosition.x + endSize / 2, finishPosition.y + endSize / 2);
      this.duration = duration;
      this.positionAnimator = new FactorAnimator(POSITION_ANIMATOR, this, AnimatorUtils.DECELERATE_INTERPOLATOR, duration, 0f);
      return setPosition(new Rect(this.startPosition));
    }

    public ReactionInfo setAnimatedPositionOffsetProvider (AnimatedPositionOffsetProvider animatedPositionOffsetProvider) {
      this.animatedPositionOffsetProvider = animatedPositionOffsetProvider;
      return this;
    }

    public ReactionInfo setAnimationEndListener (OnFinishAnimationListener endAnimationListener) {
      this.endAnimationListener = endAnimationListener;
      return this;
    }

    public ReactionInfo setPosition (Point pos, int size) {
      return setPosition(new Rect(pos.x - size / 2, pos.y - size / 2, pos.x + size / 2, pos.y + size / 2));
    }

    public ReactionInfo setPosition (Rect rect) {
      position = rect;
      gifReceiver.setBounds(rect.left, rect.top, rect.right, rect.bottom);
      return this;
    }

    int scrollOffsetX, scrollOffsetY;

    public ReactionInfo addOffset (int dx, int dy) {
      scrollOffsetX += dx;
      scrollOffsetY += dy;
      return this;
    }

    private ReactionInfo setRemoveListener (Runnable listener) {
      if (animation != null && positionAnimator == null) {
        animation.addLoopListener(() -> {
          if (endAnimationListener != null) {
            endAnimationListener.onAnimationFinish();
          }
          removeRunnable.run();
        });
      }
      this.removeRunnable = listener;
      return this;
    }

    public void draw (Canvas canvas) {
      if (animatedPositionOffsetProvider != null) {
        animatedPositionOffsetProvider.getOffset(animationOffsetPoint);
      }

      canvas.save();
      canvas.translate(scrollOffsetX + animationOffsetPoint.x, scrollOffsetY + animationOffsetPoint.y);
      gifReceiver.draw(canvas);
      //canvas.drawRect(position, Paints.strokeBigPaint(Color.RED));
      canvas.restore();
    }

    public void attach () {
      imageReceiver.attach();
      gifReceiver.attach();
      if (positionAnimator != null) {
        positionAnimator.animateTo(1f);
      }
    }

    public void detach () {
      imageReceiver.detach();
      gifReceiver.detach();
      if (positionAnimator != null) {
        positionAnimator.cancel();
      }
    }


    @Nullable
    public Rect getStartPosition () {
      return startPosition;
    }

    @Nullable
    public Rect getFinishPosition () {
      return finishPosition;
    }

    @Override
    public void onFactorChanged (int id, float factor, float fraction, FactorAnimator callee) {
      if (id == POSITION_ANIMATOR) {
        if (animatedPositionProvider != null) {
          animatedPositionProvider.getPosition(this, factor, position);
        }
        setPosition(position);
        parentView.invalidate();
      }
    }

    @Override
    public void onFactorChangeFinished (int id, float finalFactor, FactorAnimator callee) {
      if (id == POSITION_ANIMATOR) {
        if (endAnimationListener != null) {
          endAnimationListener.onAnimationFinish();
        }
        removeRunnable.run();
        parentView.invalidate();
      }
    }

    @Override
    public void performDestroy () {
      imageReceiver.destroy();
      gifReceiver.destroy();
    }
  }

  public interface AnimatedPositionOffsetProvider {
    void getOffset (Point p);
  }

  public interface AnimatedPositionProvider {
    void getPosition (ReactionInfo reactionInfo, float factor, Rect outPosition);
  }

  public interface OnFinishAnimationListener {
    void onAnimationFinish ();
  }
}