package team.bugbusters.acceleraudio;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ProgressBar;

public class VerticalProgressBar extends ProgressBar {
	
	private int x, y, z, w;
	
	/*@Override
	protected void drawableStateChanged() {
		super.drawableStateChanged();
	}*/

	public VerticalProgressBar(Context context) {
		super(context);
	}
	
	public VerticalProgressBar(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	
	public VerticalProgressBar(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		/*this.x = w;
		this.y = h;
		this.z = oldw;
		this.w = oldh;*/
	}
	
	@Override
	protected synchronized void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		//setMeasuredDimension(getMeasuredHeight(), getMeasuredWidth());
	}
	
	protected void onDraw(Canvas c) {
		c.rotate(-90);
		c.translate(-getHeight(), 0);
		super.onDraw(c);
	}
	
	/*@Override
	public boolean onTouchEvent(MotionEvent event) {
		if(!isEnabled()) {
			return false;
		}
		
		switch(event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			setSelected(true);
			setPressed(true);
			break;
			
		case MotionEvent.ACTION_MOVE:
			setProgress(getMax() - (int) (getMax() * event.getY() / getHeight()));
			onSizeChanged(getWidth(), getHeight(), 0, 0);
			break;
			
		case MotionEvent.ACTION_UP:
			setSelected(false);
			setPressed(false);
			break;
		}
		return true;
	}
	
	@Override
	public synchronized void setProgress(int progress) {
		if(progress >= 0) {
			super.setProgress(progress);
		}
		else {
			super.setProgress(0);
		}
		onSizeChanged(x, y, z, w);
	}*/

}
