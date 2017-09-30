/*
 * Copyright 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package android.support.design.animation;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.AnimatorRes;
import android.support.annotation.Nullable;
import android.support.v4.util.SimpleArrayMap;
import java.util.ArrayList;
import java.util.List;

/**
 * A motion spec contains multiple named {@link MotionTiming motion timings}.
 *
 * <p>Inflate an instance of MotionSpec from XML by creating a <a
 * href="https://developer.android.com/guide/topics/resources/animation-resource.html#Property">Property
 * Animation resource</a> in {@code res/animator}. The file must contain an {@code <objectAnimator>}
 * or a {@code <set>} of object animators.
 *
 * <p>This class will store a map of String keys to MotionTiming values. Each animator's {@code
 * android:propertyName} attribute will be used as the key, while the other attributes {@code
 * android:startOffset}, {@code android:duration}, {@code android:interpolator}, {@code
 * android:repeatCount}, and {@code android:repeatMode} will be used to create the MotionTiming
 * instance.
 *
 * <p>A motion spec resource can either be an &lt;objectAnimator&gt; or a &lt;set&gt; of multiple
 * &lt;objectAnimator&gt;.
 *
 * <pre>{@code
 * <set xmlns:android="http://schemas.android.com/apk/res/android">
 *   <objectAnimator
 *       android:propertyName="alpha"
 *       android:startOffset="0"
 *       android:duration="100"
 *       android:interpolator="@interpolator/mtrl_fast_out_slow_in"/>
 *   <objectAnimator
 *       android:propertyName="translation"
 *       android:startOffset="50"
 *       android:duration="150"/>
 * </set>
 * }</pre>
 */
public class MotionSpec {

  private final SimpleArrayMap<String, MotionTiming> timings = new SimpleArrayMap<>();

  /** Returns whether this motion spec contains a MotionTiming with the given name. */
  public boolean hasTiming(String name) {
    return timings.get(name) != null;
  }

  /**
   * Returns the MotionTiming with the given name, or throws IllegalArgumentException if it does not
   * exist.
   */
  public MotionTiming getTiming(String name) {
    if (!hasTiming(name)) {
      throw new IllegalArgumentException();
    }
    return timings.get(name);
  }

  /** Sets a MotionTiming with the given name. */
  public void setTiming(String name, @Nullable MotionTiming timing) {
    timings.put(name, timing);
  }

  /**
   * Returns the total duration of this motion spec, which is the maximum delay+duration of its
   * motion timings.
   */
  public long getTotalDuration() {
    long duration = 0;
    for (int i = 0, count = timings.size(); i < count; i++) {
      MotionTiming timing = timings.valueAt(i);
      duration = Math.max(duration, timing.getDelay() + timing.getDuration());
    }
    return duration;
  }

  /** Inflates an instance of MotionSpec from the given animator resource. */
  public static MotionSpec inflate(Context context, @AnimatorRes int id) {
    try {
      Animator animator = AnimatorInflater.loadAnimator(context, id);
      if (animator instanceof AnimatorSet) {
        AnimatorSet set = (AnimatorSet) animator;
        return createSpecFromAnimators(set.getChildAnimations());
      } else {
        List<Animator> animators = new ArrayList<>();
        animators.add(animator);
        return createSpecFromAnimators(animators);
      }
    } catch (Exception e) {
      Resources.NotFoundException exception =
          new Resources.NotFoundException(
              "Can't load animation resource ID #0x" + Integer.toHexString(id));
      exception.initCause(e);
      throw exception;
    }
  }

  private static MotionSpec createSpecFromAnimators(List<Animator> animators) {
    MotionSpec spec = new MotionSpec();
    for (int i = 0, count = animators.size(); i < count; i++) {
      addTimingFromAnimator(spec, animators.get(i));
    }
    return spec;
  }

  private static void addTimingFromAnimator(MotionSpec spec, Animator animator) {
    if (animator instanceof ObjectAnimator) {
      ObjectAnimator anim = (ObjectAnimator) animator;
      spec.setTiming(anim.getPropertyName(), MotionTiming.createFromAnimator(anim));
    } else {
      throw new IllegalArgumentException("Animator must be an ObjectAnimator: " + animator);
    }
  }
}