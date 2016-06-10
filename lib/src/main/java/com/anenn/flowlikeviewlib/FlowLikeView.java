package com.anenn.flowlikeviewlib;

import android.content.Context;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorListenerAdapter;
import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.animation.TypeEvaluator;
import com.nineoldandroids.animation.ValueAnimator;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by Anenn on 6/10/16.
 */
public class FlowLikeView extends RelativeLayout {

    private List<Drawable> mLikeDrawables; // 图片的集合
    private LayoutParams mLayoutParams; // 用于设置动画对象的位置参数
    private Random mRandom; // 用于产生随机数,如生成随机图片

    private int mViewWidth; // 控件的宽度
    private int mViewHeight; // 控件的高度

    private int mPicWidth; // 图片的宽度
    private int mPicHeight; // 图片的高度

    private int mChildViewHeight; // 在 XML 布局文件中添加的子View的总高度

    public FlowLikeView(Context context) {
        this(context, null);
    }

    public FlowLikeView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FlowLikeView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        initParams();
    }

    private void initParams() {
        mLikeDrawables = new ArrayList<>();
        mLikeDrawables.add(generateDrawable(R.drawable.heart0));
        mLikeDrawables.add(generateDrawable(R.drawable.heart1));
        mLikeDrawables.add(generateDrawable(R.drawable.heart2));
        mLikeDrawables.add(generateDrawable(R.drawable.heart3));
        mLikeDrawables.add(generateDrawable(R.drawable.heart4));
        mLikeDrawables.add(generateDrawable(R.drawable.heart5));
        mLikeDrawables.add(generateDrawable(R.drawable.heart6));
        mLikeDrawables.add(generateDrawable(R.drawable.heart7));
        mLikeDrawables.add(generateDrawable(R.drawable.heart8));

        // 获取图片的宽高, 由于图片大小一致,故直接获取第一张图片的宽高
        mPicWidth = mLikeDrawables.get(0).getIntrinsicWidth();
        mPicHeight = mLikeDrawables.get(0).getIntrinsicHeight();

        // 初始化布局参数
        mLayoutParams = new RelativeLayout.LayoutParams(mPicWidth, mPicHeight);
        mLayoutParams.addRule(CENTER_HORIZONTAL);
        mLayoutParams.addRule(ALIGN_PARENT_BOTTOM);

        mRandom = new Random();
    }

    private Drawable generateDrawable(int resID) {
        return ContextCompat.getDrawable(getContext(), resID);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        if (mChildViewHeight <= 0) {
            for (int i = 0, size = getChildCount(); i < size; i++) {
                View childView = getChildAt(i);
                measureChild(childView, widthMeasureSpec, heightMeasureSpec);
                mChildViewHeight += childView.getMeasuredHeight();
            }

            // 设置底部间距
            mLayoutParams.bottomMargin = mChildViewHeight;
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        mViewWidth = getWidth();
        mViewHeight = getHeight();
    }

    /**
     * 动态添加 FlowView
     */
    public void addLikeView() {
        ImageView likeView = new ImageView(getContext());
        likeView.setImageDrawable(mLikeDrawables.get(mRandom.nextInt(mLikeDrawables.size())));
        likeView.setLayoutParams(mLayoutParams);

        addView(likeView);
        startAnimation(likeView);
    }

    private void startAnimation(View target) {
        // 设置进入动画
        AnimatorSet enterAnimator = generateEnterAnimation(target);
        // 设置路径动画
        ValueAnimator curveAnimator = generateCurveAnimation(target);

        // 设置动画集合, 先执行进入动画,最后再执行运动曲线动画
        AnimatorSet finalAnimatorSet = new AnimatorSet();
        finalAnimatorSet.setTarget(target);
        finalAnimatorSet.playSequentially(enterAnimator, curveAnimator);
        finalAnimatorSet.addListener(new AnimationEndListener(target));
        finalAnimatorSet.start();
    }

    /**
     * 生成进入动画
     *
     * @return 动画集合
     */
    private AnimatorSet generateEnterAnimation(View target) {
        ObjectAnimator alpha = ObjectAnimator.ofFloat(target, "alpha", 0.2f, 1f);
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(target, "scaleX", 0.5f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(target, "scaleY", 0.5f, 1f);
        AnimatorSet enterAnimation = new AnimatorSet();
        enterAnimation.playTogether(alpha, scaleX, scaleY);
        enterAnimation.setDuration(150);
        enterAnimation.setTarget(target);
        return enterAnimation;
    }

    /**
     * 生成曲线运动动画
     *
     * @return 动画集合
     */
    private ValueAnimator generateCurveAnimation(View target) {
        CurveEvaluator evaluator = new CurveEvaluator(generateCTRLPointF(1), generateCTRLPointF(2));
        ValueAnimator valueAnimator = ValueAnimator.ofObject(evaluator,
                new PointF((mViewWidth - mPicWidth) / 2, mViewHeight - mChildViewHeight - mPicHeight),
                new PointF((mViewWidth) / 2 + (mRandom.nextBoolean() ? 1 : -1) * mRandom.nextInt(100), 0));
        valueAnimator.setDuration(3000);
        valueAnimator.addUpdateListener(new CurveUpdateLister(target));
        valueAnimator.setTarget(target);

        return valueAnimator;
    }

    /**
     * 生成贝塞儿曲线的控制点
     *
     * @param value 设置控制点 y 轴上取值区域
     * @return 控制点的 x y 坐标
     */
    private PointF generateCTRLPointF(int value) {
        PointF pointF = new PointF();
        pointF.x = mViewWidth / 2 - mRandom.nextInt(100);
        pointF.y = mRandom.nextInt(mViewHeight / value);

        return pointF;
    }

    /**
     * 自定义估值算法, 计算对象当前运动的具体位置 Point
     */
    private class CurveEvaluator implements TypeEvaluator<PointF> {

        // 由于这里使用的是三阶的贝塞儿曲线, 所以我们要定义两个控制点
        private PointF ctrlPointF1;
        private PointF ctrlPointF2;

        public CurveEvaluator(PointF ctrlPointF1, PointF ctrlPointF2) {
            this.ctrlPointF1 = ctrlPointF1;
            this.ctrlPointF2 = ctrlPointF2;
        }

        @Override
        public PointF evaluate(float fraction, PointF startValue, PointF endValue) {

            // 这里运用了三阶贝塞儿曲线的公式, 请自行上网查阅
            float leftTime = 1.0f - fraction;
            PointF resultPointF = new PointF();

            // 三阶贝塞儿曲线
            resultPointF.x = (float) Math.pow(leftTime, 3) * startValue.x
                    + 3 * (float) Math.pow(leftTime, 2) * fraction * ctrlPointF1.x
                    + 3 * leftTime * (float) Math.pow(fraction, 2) * ctrlPointF2.x
                    + (float) Math.pow(fraction, 3) * endValue.x;
            resultPointF.y = (float) Math.pow(leftTime, 3) * startValue.y
                    + 3 * (float) Math.pow(leftTime, 2) * fraction * ctrlPointF1.y
                    + 3 * leftTime * fraction * fraction * ctrlPointF2.y
                    + (float) Math.pow(fraction, 3) * endValue.y;

            // 二阶贝塞儿曲线
//            resultPointF.x = (float) Math.pow(leftTime, 2) * startValue.x + 2 * fraction * leftTime * ctrlPointF1.x
//                    + ((float) Math.pow(fraction, 2)) * endValue.x;
//            resultPointF.y = (float) Math.pow(leftTime, 2) * startValue.y + 2 * fraction * leftTime * ctrlPointF1.y
//                    + ((float) Math.pow(fraction, 2)) * endValue.y;

            return resultPointF;
        }
    }

    /**
     * 动画曲线路径更新监听器, 用于动态更新动画作用对象的位置
     */
    private class CurveUpdateLister implements ValueAnimator.AnimatorUpdateListener {
        private View target;

        public CurveUpdateLister(View target) {
            this.target = target;
        }

        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            // 获取当前动画运行的状态值, 使得动画作用对象沿着曲线(涉及贝塞儿曲线)运动
            PointF pointF = (PointF) animation.getAnimatedValue();
            ViewCompat.setX(target, pointF.x);
            ViewCompat.setY(target, pointF.y);
            // 改变对象的透明度
            ViewCompat.setAlpha(target, 1 - animation.getAnimatedFraction());
        }
    }

    /**
     * 动画结束监听器,用于释放无用的资源
     */
    private class AnimationEndListener extends AnimatorListenerAdapter {
        private View target;

        public AnimationEndListener(View target) {
            this.target = target;
        }

        @Override
        public void onAnimationEnd(Animator animation) {
            super.onAnimationEnd(animation);

            removeView(target);
        }
    }
}
