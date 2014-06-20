package team.bugbusters.acceleraudio;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.ProgressBar;

/*-- This class is used to rotate default progress bar in order to display vertical progress bar in the portrait layout of UI3. (See ui3_layout.xml). --*/
public class VerticalProgressBar extends ProgressBar {

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
	}
	
	@Override
	protected synchronized void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}
	
	protected void onDraw(Canvas c) {
		c.rotate(-90);
		c.translate(-getHeight(), 0);
		super.onDraw(c);
	}
}
