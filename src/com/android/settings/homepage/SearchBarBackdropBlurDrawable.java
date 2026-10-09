/*
 * Copyright (C) 2026 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings.homepage;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.RecordingCanvas;
import android.graphics.RectF;
import android.graphics.RenderEffect;
import android.graphics.RenderNode;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.card.MaterialCardView;

/**
 * Bluenixx: draws a blurred copy of {@code content} behind {@code card}, clipped to the card
 * shape. Meant to be the background of the card's parent, so it is drawn under the card.
 */
public class SearchBarBackdropBlurDrawable extends Drawable {
    private final View mCard;
    private final View mContent;
    private final int mBackingColor;
    private final float mBlurRadius;
    private final RenderNode mNode = new RenderNode("settingsSearchBarBlur");
    private final Path mClip = new Path();
    private final RectF mRect = new RectF();
    private final int[] mCardLoc = new int[2];
    private final int[] mContentLoc = new int[2];

    public SearchBarBackdropBlurDrawable(View card, View content, int backingColor,
            float blurRadius) {
        mCard = card;
        mContent = content;
        mBackingColor = backingColor;
        mBlurRadius = blurRadius;
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        final int w = mCard.getWidth();
        final int h = mCard.getHeight();
        if (!canvas.isHardwareAccelerated() || w <= 0 || h <= 0 || mContent.getWidth() <= 0) {
            return;
        }
        mCard.getLocationInWindow(mCardLoc);
        mContent.getLocationInWindow(mContentLoc);

        mNode.setPosition(0, 0, w, h);
        RecordingCanvas rc = mNode.beginRecording(w, h);
        rc.translate(mContentLoc[0] - mCardLoc[0], mContentLoc[1] - mCardLoc[1]);
        mContent.draw(rc);
        mNode.endRecording();
        mNode.setRenderEffect(RenderEffect.createBlurEffect(
                mBlurRadius, mBlurRadius, Shader.TileMode.CLAMP));

        float radius = 0f;
        if (mCard instanceof MaterialCardView) {
            radius = ((MaterialCardView) mCard).getRadius();
        }
        if (radius <= 0f || radius > h / 2f) {
            radius = h / 2f;
        }
        mRect.set(mCard.getLeft(), mCard.getTop(), mCard.getRight(), mCard.getBottom());
        mClip.reset();
        mClip.addRoundRect(mRect, radius, radius, Path.Direction.CW);

        int save = canvas.save();
        canvas.clipPath(mClip);
        // opaque backing hides the sharp content that sits under the bar
        canvas.drawColor(mBackingColor | 0xFF000000);
        canvas.translate(mCard.getLeft(), mCard.getTop());
        canvas.drawRenderNode(mNode);
        canvas.restoreToCount(save);
    }

    @Override
    public void setAlpha(int alpha) {}

    @Override
    public void setColorFilter(@Nullable ColorFilter colorFilter) {}

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }
}
