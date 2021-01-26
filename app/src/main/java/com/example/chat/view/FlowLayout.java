package com.example.chat.view;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import java.util.ArrayList;
import java.util.List;

public class FlowLayout extends ViewGroup {
    //子布局的数量
    private int childCount = 0;
    //记录所有的子view 每一项就是每一行view的集合
    private List<List<View>> childList = new ArrayList<>();
    //每一行的view
    private List<View> lineList = new ArrayList<>();
    //记录每一行的高度 只记录这一行最高的控件
    private List<Integer> heightList = new ArrayList<>();
    //FlowLayout的padding值
    private int paddingLeft = 0;
    private int paddingRight = 0;
    private int paddingTop = 0;
    private int paddingBottom = 0;
    //标志位 不要重复测量
    private boolean isMeasure = false;

    public FlowLayout(Context context) {
        super(context);
    }

    public FlowLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FlowLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /***
     * xml布局被解析成View对象的过程中，viewGroup.addView(view, params)传入的
     * params就是通过viewGroup.generateLayoutParams(attrs)获得的，参数attrs
     * 里包装的就是这个view在xml中的属性，所以如果我们不重写generateLayoutParams()
     * 方法，那这个viewGroup里的子view就不支持margin设置了。
     * @param attrs
     * @return
     */
    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new MarginLayoutParams(getContext(), attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //获取自身宽度的测量规则和父容器允许的宽度大小
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        //获取自身高度的测量规则和父容器允许的高度大小
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        //得到子布局个数
        childCount = getChildCount();
        //获取自身设置的Padding值
        paddingLeft = getPaddingLeft();
        paddingRight = getPaddingRight();
        paddingTop = getPaddingTop();
        paddingBottom = getPaddingBottom();

        //测量的宽度 记录实际的宽高 wrap_content才用得到
        int measureWidth = paddingLeft + paddingRight;
        int measureHeight = paddingTop + paddingBottom;
        //记录一行的宽度
        int lineWidth = paddingRight + paddingLeft;
        //开始测量子控件
        measureChildren(widthMeasureSpec, heightMeasureSpec);

        //子view
        View view;
        //MarginLayoutParams 用来获取每个子控件的margin值
        MarginLayoutParams params;
        //记录每行最大的高度值
        int lineHeight = 0;

        if (!isMeasure) {
            isMeasure = true;
        } else {
            //遍历每个子view
            for (int i = 0; i < childCount; i++) {
                view = getChildAt(i);
                params = (MarginLayoutParams) view.getLayoutParams();
                //lineWidth代表当前已经添加view的总宽度  如果当前已添加的宽度加上这个子view的宽度加margin超过的最大宽度，就换行
                if ((lineWidth + params.leftMargin + params.rightMargin + view.getMeasuredWidth()) > widthSize) {
                    //换行 将上一行的list记录加到总的list中
                    childList.add(lineList);
                    //重置行宽 初始值为当前行第一个view的宽度加上margin再加上viewgroup的padding值
                    lineWidth = view.getMeasuredWidth() + params.leftMargin + params.rightMargin+paddingRight + paddingLeft;
                    //新建一个list来记录当前行的view
                    lineList = new ArrayList<>();
                    //将当前view加到lineList中 当前view是每行的第一个view
                    lineList.add(view);
                    //记录当前行的高度
                    heightList.add(lineHeight);
                    //记录总的高度
                    measureHeight += lineHeight;
                    //记录当前子view的高度
                    lineHeight = view.getMeasuredHeight() + params.topMargin + params.bottomMargin;
                } else {
                    //如果当前行能放下一个子view 则将该view加到lineList中 并记录行宽行高
                    lineWidth += params.leftMargin + params.rightMargin + view.getMeasuredWidth();
                    lineList.add(view);
                    //高度取遍历本行的过程中子view最大的高
                    lineHeight = Math.max(lineHeight, view.getMeasuredHeight() + params.topMargin + params.bottomMargin);
                }
                //宽度取最宽的那一行的值
                measureWidth = Math.max(measureWidth, lineWidth);
                //因为最后一个子view不会触发换行，而换行才会将上一行的list添加到总的list中
                //所以当遍历到最后一个子view后需要手动再添加一次，并且计算总高度
                if (i == childCount - 1) {
                    childList.add(lineList);
                    heightList.add(lineHeight);
                    measureHeight += lineHeight;
                }
            }
        }
        //如果宽是wrap_content 宽就是测量出来的宽
        if (widthMode == MeasureSpec.AT_MOST) {
            widthSize = measureWidth;
        }
        //如果高是wrap_content 高是测量出来的高
        if (heightMode == MeasureSpec.AT_MOST) {
            heightSize = measureHeight;
        }

        setMeasuredDimension(widthSize, heightSize);
    }

    //摆放子view
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int left, right, top, bottom;
        MarginLayoutParams marginLayoutParams;
        top = paddingTop;
        //记录总的行高
        int allHeight = paddingTop;
        //指针 从heightList中取出每行的高度
        int i = 0;
        for (List<View> list : childList) {
            //每行都从viewgroup的paddingleft处摆放
            left = paddingLeft;
            for (View view : list) {
                marginLayoutParams = (MarginLayoutParams) view.getLayoutParams();
                //子view的左边需要加上自身的leftMargin值
                left += marginLayoutParams.leftMargin;
                //子view的上边需要加上自身的topmargin
                top += marginLayoutParams.topMargin;
                //右边就是左边加上自身宽度
                right = left + view.getMeasuredWidth();
                //底部就是顶部加上自身高度
                bottom = top + view.getMeasuredHeight();
                //开始摆放
                view.layout(left, top, right, bottom);
                //一行一行的摆放 所以left需要累加 以供它右边的子view使用
                left += view.getMeasuredWidth() + marginLayoutParams.rightMargin;
                //重置top值 因为每个子view的topmargin可能不一样 每次都有重新计算top 注意top不需要累加
                top = allHeight;
            }
            //记录当前行的高度
            allHeight += heightList.get(i++);
            //初始化行高
            top = allHeight;
        }
    }

    //这里不需要重写onDraw 子控件具体的绘制由每个子控件完成
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }

    //动态addView(View)需要重写该方法
    @Override
    protected LayoutParams generateDefaultLayoutParams() {
        return new MarginLayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
    }

}

