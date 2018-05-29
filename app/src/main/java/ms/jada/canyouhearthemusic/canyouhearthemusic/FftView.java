package ms.jada.canyouhearthemusic.canyouhearthemusic;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;



import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;


public class FftView extends View {

    private byte[] mFFTData;

    private final Paint mPaint = new Paint();
    private final RectF mRect = new RectF();

    public FftView(final Context context) {
        super(context);
        init();
    }

    public FftView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        init();
    }


    public FftView(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public void init() {

        int[] colors = new int[]{Color.RED, Color.YELLOW, Color.GREEN};
        float[] positions = new float[]{0, getHeight()/3*2, getHeight()};

        Log.d("MA", String.valueOf(getHeight()));

        mPaint.setShader(new LinearGradient(0f,0f,0f, ((float)getHeight()), colors, positions,
                Shader.TileMode.CLAMP));
    }

    @Override
    protected void onDraw(final Canvas canvas) {
        super.onDraw(canvas);

        // check edit mode
        if (isInEditMode()) {
            return;
        }

        // safe check
        if (mFFTData == null) {
            return;
        }

        int length = mFFTData.length / 2;
        int width = canvas.getWidth() / length;
        int height = canvas.getHeight();

        int pos;

        for (int i = 0; i < length; ++i) {
            pos = i * width;

            float rFk = ((float) mFFTData[2 * i]) / Byte.MAX_VALUE;
            float iFk = ((float) mFFTData[(2 * i) + 1]) / Byte.MAX_VALUE;

            float magnitude = (float) Math.sqrt((rFk * rFk) + (iFk * iFk));

            mRect.set(pos, height/2 * (1 - magnitude), pos + width, height/2);
            canvas.drawRect(mRect, mPaint);

            mRect.set(pos, height /2, pos + width,  height - height/2 * (1 - magnitude));
            canvas.drawRect(mRect, mPaint);
        }
    }

    public void setFFTData(final byte[] fFTData) {
        mFFTData = fFTData;
        invalidate();
    }
}