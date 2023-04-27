package quick_chat.custom_views;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Size;
import android.view.MotionEvent;

import quick_chat.Utils;

public class ImageProcessingView extends androidx.appcompat.widget.AppCompatImageView
{

    private static int handleSize = 30;

    private static Paint 				borderPaint1 				= new Paint();

    private static Paint 				borderPaint2 				= new Paint();

    static
    {
        int borderColor1 = Color.BLACK;
        borderPaint1.setColor(borderColor1);
        int borderWidth1 = 6;
        borderPaint1.setStrokeWidth(borderWidth1);
        borderPaint1.setStyle(Paint.Style.STROKE);
        borderPaint1.setAntiAlias(true);

        int borderColor2 = Color.YELLOW;
        borderPaint2.setColor(borderColor2);
        int borderWidth2 = 3;
        borderPaint2.setStrokeWidth(borderWidth2);
        borderPaint2.setStyle(Paint.Style.STROKE);
        borderPaint2.setAntiAlias(true);
    }

    private Bitmap				imageBitmap					= null;
    private RectF 				savedClpFrame				= null;
    private RectF 				clippingFrame				= null;
    private RectF 				auxClpFrame					= null;

    private RectF				vHandle						= new RectF( -handleSize, -16, handleSize,  16 );
    private RectF				hHandle						= new RectF(  -16, -handleSize, 16, handleSize );

    private int	xCoOrdinateBitM;
    private int	yCoOrdinateBitM;

    Matrix 	matrix 			= new Matrix();
    int 	boundary_color 	= 0xFFF0F0F0;
    Rect	bitMapArea 		= null;


    int clippFrameResizeMode = 0;

    private boolean		showBackGrndFrame	= false;
    private boolean 	roundBitMap 		= false;
    private Size		imageSize			= null;
    //private double		diagonal			= 870;

    public ImageProcessingView( Context context, AttributeSet attrs )
    {
        super(context, attrs);
    }

    public void init(Bitmap bitmap, boolean roundBitMap, Size imageSize )
    {
        this.roundBitMap 		= roundBitMap;
        this.imageSize 			= imageSize;

        if ( bitmap != null )
        {
            this.setBitmap( bitmap);
        }
    }

    private RectF getTopHandle()
    {
        vHandle.offset( clippingFrame.centerX()  -vHandle.centerX(), clippingFrame.top        -vHandle.centerY() );

        return vHandle;
    }

    private RectF getBottomHandle()
    {
        vHandle.offset( clippingFrame.centerX()  -vHandle.centerX(), clippingFrame.bottom     -vHandle.centerY() );

        return vHandle;
    }

    private RectF getLeftHandle()
    {
        hHandle.offset( clippingFrame.left       -hHandle.centerX(), clippingFrame.centerY()  -hHandle.centerY() );

        return hHandle;
    }

    private RectF getRightHandle()
    {
        hHandle.offset( clippingFrame.right      -hHandle.centerX(), clippingFrame.centerY()  -hHandle.centerY() );

        return hHandle;
    }


    @Override
    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);

        if ( clippingFrame == null && imageBitmap != null )
        {
            this.getImageMatrix().invert( matrix );

            RectF dst = new RectF();
            RectF src = new RectF( 0,0, imageBitmap.getWidth(), imageBitmap.getHeight() );

            this.getImageMatrix().mapRect( dst, src );

            clippingFrame = new RectF( dst.left, dst.top, dst.right, dst.bottom );
            savedClpFrame = new RectF( dst.left, dst.top, dst.right, dst.bottom );
        }

        if ( imageSize != null )
        {
            float cx = clippingFrame.centerX();
            float cy = clippingFrame.centerY();

            float fact = (float)imageSize.getWidth() / (float)imageSize.getHeight();

            int calcWidth = Math.round( clippingFrame.height()*fact );

            if ( calcWidth < clippingFrame.width() )
            {
                clippingFrame.left 		= cx - calcWidth/2f;
                clippingFrame.right 	= cx + calcWidth/2f;
            }
            else
            {
                int calcHeight = Math.round(clippingFrame.width()/fact);

                clippingFrame.top 		= cy - calcHeight/2f;
                clippingFrame.bottom 	= cy + calcHeight/2f;
            }
        }

        if ( roundBitMap )
        {
            float cx = clippingFrame.centerX();
            float cy = clippingFrame.centerY();

            if ( clippingFrame.width() > clippingFrame.height() )
            {
                clippingFrame.left 	= cx - clippingFrame.height()/2f;
                clippingFrame.right = cx + clippingFrame.height()/2f;
            }
            else
            if ( clippingFrame.width() < clippingFrame.height() )
            {
                clippingFrame.top 		= cy - clippingFrame.width()/2f;
                clippingFrame.bottom 	= cy + clippingFrame.width()/2f;
            }
        }

        if ( roundBitMap )
        {
            canvas.drawRoundRect( clippingFrame.left, clippingFrame.top, clippingFrame.right, clippingFrame.bottom, clippingFrame.width()/2, clippingFrame.height()/2, borderPaint1 );
            canvas.drawRoundRect( clippingFrame.left, clippingFrame.top, clippingFrame.right, clippingFrame.bottom, clippingFrame.width()/2, clippingFrame.height()/2, borderPaint2 );
        }
        else
        {
            if ( clippingFrame != null )
            {
                canvas.drawRect( clippingFrame, borderPaint1 );
                canvas.drawRect( clippingFrame, borderPaint2 );
            }
        }

        if ( clippingFrame != null )
        {
            canvas.drawRect( getTopHandle(), borderPaint1 );

            canvas.drawRect( getBottomHandle(), borderPaint1 );

            canvas.drawRect( getLeftHandle(), borderPaint1 );

            canvas.drawRect( getRightHandle(), borderPaint1 );
        }
    }


    /**
     * A method to set the bitmap for the image view
     *
     * @param bmp
     *            The target bitmap
     */
    public void setBitmap(Bitmap bmp)
    {
        this.imageBitmap 		= bmp;
        this.clippingFrame 		= null;

        this.setImageBitmap( imageBitmap );
    }


    /**
     * @return the showBackGrndFrame
     */
    public boolean isShowBackGrndFrame()
    {
        return showBackGrndFrame;
    }

    /**
     * @param showBackGrndFrame the showBackGrndFrame to set
     */
    public void setShowBackGrndFrame(boolean showBackGrndFrame)
    {
        this.showBackGrndFrame = showBackGrndFrame;
    }

    /**
     * @param showClippingFrame the showClippingFrame to set
     */
    public void setShowClippingFrame(boolean showClippingFrame)
    {
        this.showBackGrndFrame 	= false;
    }

    /**
     * @return the clippingFrame
     */
    public RectF getClippingFrame()
    {
        return clippingFrame;
    }

    /**
     * @param clippingFrame the clippingFrame to set
     */
    public void setClippingFrame(RectF clippingFrame)
    {
        this.clippingFrame = clippingFrame;
    }

    public void onTouch(MotionEvent event)
    {
        switch (event.getActionMasked())
        {
            case MotionEvent.ACTION_DOWN:
                int xCoOrdinate = Math.round(event.getX());
                int yCoOrdinate = Math.round(event.getY());

                RectF fingerArea = new RectF( xCoOrdinate -120, yCoOrdinate -120, xCoOrdinate +120, yCoOrdinate +120 );

                if ( showBackGrndFrame )
                {
                    this.getImageMatrix().invert( matrix );

                    float[] pts = new float[2];

                    pts[0] = xCoOrdinate;
                    pts[1] = yCoOrdinate;

                    matrix.mapPoints(pts);

                    xCoOrdinateBitM = Math.round(pts[0]);
                    yCoOrdinateBitM = Math.round(pts[1]);
                }

                double minDist = 1000000000;

                clippFrameResizeMode = 0;

                if ( RectF.intersects(getTopHandle(),fingerArea) )
                {
                    double dist = Math.hypot( getTopHandle().centerX()- xCoOrdinate, getTopHandle().centerY()- yCoOrdinate);

                    if ( dist < minDist )
                    {
                        minDist = dist;

                        clippFrameResizeMode = 1;
                    }
                }

                if ( RectF.intersects(getBottomHandle(),fingerArea) )
                {
                    double dist = Math.hypot( getBottomHandle().centerX()- xCoOrdinate, getBottomHandle().centerY()- yCoOrdinate);

                    if ( dist < minDist )
                    {
                        minDist = dist;

                        clippFrameResizeMode = 2;
                    }
                }

                if ( RectF.intersects(getLeftHandle(),fingerArea) )
                {
                    double dist = Math.hypot( getLeftHandle().centerX()- xCoOrdinate, getLeftHandle().centerY()- yCoOrdinate);

                    if ( dist < minDist )
                    {
                        minDist = dist;

                        clippFrameResizeMode = 3;
                    }

                }

                if ( RectF.intersects(getRightHandle(),fingerArea) )
                {
                    double dist = Math.hypot( getRightHandle().centerX()- xCoOrdinate, getRightHandle().centerY()- yCoOrdinate);

                    if ( dist < minDist )
                    {
                        minDist = dist;

                        clippFrameResizeMode = 4;
                    }
                }

            {
                double dist = Math.hypot( clippingFrame.centerX()- xCoOrdinate, clippingFrame.centerY()- yCoOrdinate);

                if ( dist < minDist )
                {
                    minDist = dist;

                    auxClpFrame = new RectF( clippingFrame );

                    clippFrameResizeMode = 5;
                }
            }

            break;
            case MotionEvent.ACTION_MOVE:
                if ( clippingFrame != null )
                {
                    float 	cx 		= clippingFrame.centerX();
                    float 	cy 		= clippingFrame.centerY();

                    switch ( clippFrameResizeMode )
                    {
                        case 1:
                            clippingFrame.top 		= (int)event.getY();
                            if ( imageSize != null )
                            {
                                float fact = (float)imageSize.getWidth() / (float)imageSize.getHeight();

                                int calcWidth = Math.round(clippingFrame.height()*fact);

                                clippingFrame.left 		= cx - calcWidth/2f;
                                clippingFrame.right 	= cx + calcWidth/2f;
                            }
                            if ( roundBitMap )
                            {
                                clippingFrame.left 	= cx - clippingFrame.height()/2f;
                                clippingFrame.right = cx + clippingFrame.height()/2f;
                            }
                            break;
                        case 2:
                            clippingFrame.bottom 	= (int)event.getY();
                            if ( imageSize != null )
                            {
                                float fact = (float)imageSize.getWidth() / (float)imageSize.getHeight();

                                int calcWidth = Math.round(clippingFrame.height()*fact);

                                clippingFrame.left 		= cx - calcWidth/2f;
                                clippingFrame.right 	= cx + calcWidth/2f;
                            }
                            if ( roundBitMap )
                            {
                                clippingFrame.left 	= cx - clippingFrame.height()/2f;
                                clippingFrame.right = cx + clippingFrame.height()/2f;
                            }
                            break;
                        case 3:
                            clippingFrame.left 		= (int)event.getX();
                            if ( imageSize != null )
                            {
                                float fact = (float)imageSize.getWidth() / (float)imageSize.getHeight();

                                int calcHeight = Math.round(clippingFrame.width()/fact);

                                clippingFrame.top 		= cy - calcHeight/2f;
                                clippingFrame.bottom 	= cy + calcHeight/2f;
                            }
                            if ( roundBitMap )
                            {
                                clippingFrame.top 		= cy - clippingFrame.width()/2f;
                                clippingFrame.bottom 	= cy + clippingFrame.width()/2f;
                            }
                            break;
                        case 4:
                            clippingFrame.right 	= (int)event.getX();
                            if ( imageSize != null )
                            {
                                float fact = (float)imageSize.getWidth() / (float)imageSize.getHeight();

                                int calcHeight = Math.round(clippingFrame.width()/fact);

                                clippingFrame.top 		= cy - calcHeight/2f;
                                clippingFrame.bottom 	= cy + calcHeight/2f;
                            }
                            if ( roundBitMap )
                            {
                                clippingFrame.top 		= cy - clippingFrame.width()/2f;
                                clippingFrame.bottom 	= cy + clippingFrame.width()/2f;
                            }
                            break;
                        case 5:
                            clippingFrame.top 		= (int)event.getY() - auxClpFrame.height()/2f;
                            clippingFrame.bottom 	= (int)event.getY() + auxClpFrame.height()/2f;
                            clippingFrame.left 		= (int)event.getX() - auxClpFrame.width()/2f;
                            clippingFrame.right 	= (int)event.getX() + auxClpFrame.width()/2f;
                            break;
                    }

                    clippingFrame.top 		= Math.max(clippingFrame.top, savedClpFrame.top);
                    clippingFrame.top 		= Math.min(clippingFrame.top, clippingFrame.bottom-(handleSize*2f));
                    clippingFrame.bottom 	= Math.min(clippingFrame.bottom, savedClpFrame.bottom);
                    clippingFrame.bottom 	= Math.max(clippingFrame.bottom, clippingFrame.top+(handleSize*2f));
                    clippingFrame.left 		= Math.max(clippingFrame.left, savedClpFrame.left);
                    clippingFrame.left 		= Math.min(clippingFrame.left, clippingFrame.right-(handleSize*2f));
                    clippingFrame.right 	= Math.min(clippingFrame.right, savedClpFrame.right );
                    clippingFrame.right 	= Math.max(clippingFrame.right, clippingFrame.left+(handleSize*2f));


                    this.invalidate();
                }
                //view.animate().x(event.getRawX() + xCoOrdinate).y(event.getRawY() + yCoOrdinate).setDuration(0).start();
                break;
        }
    }

    void flood( int[] pixels, int width, int height, int x, int y, int boundary_color )
    {
        if ( x<0 || x>=width || y<0 || y>=height )
        {
            return;
        }

        int size = width*height;

        int[] stackP = new int[size];
        int[] stackX = new int[size];
        int[] stackY = new int[size];

        int	stackH 	= 0;
        int indx1 	= y * width + x;

        if ( pixels[indx1] != boundary_color )
        {
            stackX[stackH] = x;
            stackY[stackH] = y;
            stackP[stackH] = pixels[indx1];
            stackH++;

            while ( stackH > 0 )
            {
                stackH--;
                int px 		= stackX[stackH];
                int py 		= stackY[stackH];
                int pix1 	= stackP[stackH];

                if ( px>=0 && px<width && py>=0 && py<height )
                {
                    int indx2 	= py * width + px;
                    int pix2 	= pixels[indx2];

                    if ( pix2 != boundary_color )
                    {
                        int r2 = pix2 >> 16 & 0XFF;
                        int g2 = pix2 >>  8 & 0XFF;
                        int b2 = pix2       & 0XFF;

                        int r1 = pix1 >> 16 & 0XFF ;
                        int g1 = pix1 >>  8 & 0XFF ;
                        int b1 = pix1       & 0XFF ;

                        int dr = r1 - r2;
                        int dg = g1 - g2;
                        int db = b1 - b2;

                        if ( (dr*dr + dg*dg + db*db) < 30 )
                        {
                                            pix1 = pixels[indx2];
                            pixels[indx2] = boundary_color;

                            if ( stackH < size-1 )
                            {
                                stackX[stackH] = px;
                                stackY[stackH] = py-1;
                                stackP[stackH] = pix1;
                                stackH++;
                            }

                            if ( stackH < size-1 )
                            {
                                stackX[stackH] = px;
                                stackY[stackH] = py+1;
                                stackP[stackH] = pix1;
                                stackH++;
                            }

                            if ( stackH < size-1 )
                            {
                                stackX[stackH] = px-1;
                                stackY[stackH] = py;
                                stackP[stackH] = pix1;
                                stackH++;
                            }

                            if ( stackH < size-1 )
                            {
                                stackX[stackH] = px+1;
                                stackY[stackH] = py;
                                stackP[stackH] = pix1;
                                stackH++;
                            }
                        }
                    }
                }
            }

            for ( int h=0; h<width-1; h++ )
            {
                for ( int v=0; v<height; v++ )
                {
                    int i = h+v*width;

                    int pix1 	= pixels[i];

                    if ( pix1 == boundary_color )
                    {
                        int pix2 	= pixels[i+1];

                        if ( pix2 != boundary_color )
                        {
                            int r2 = pix2 >> 16 & 0XFF;
                            int g2 = pix2 >>  8 & 0XFF;
                            int b2 = pix2       & 0XFF;

                            int r1 = pix1 >> 16 & 0XFF ;
                            int g1 = pix1 >>  8 & 0XFF ;
                            int b1 = pix1       & 0XFF ;

                            int dr = (r1 - r2)/3;
                            int dg = (g1 - g2)/3;
                            int db = (b1 - b2)/3;

                            pixels[ i ] = 0xFF000000 | ((r1-dr) << 16) | ((g1-dg) << 8) | (b1-db);
                            pixels[i+1] = 0xFF000000 | ((r2+dr) << 16) | ((g2+dg) << 8) | (b2+db);
                        }
                    }
                }
            }

            for ( int h=width-1; h>1; h-- )
            {
                for ( int v=0; v<height; v++ )
                {
                    int i = h+v*width;

                    int pix1 	= pixels[i];

                    if ( pix1 == boundary_color )
                    {
                        int pix2 	= pixels[i-1];

                        if ( pix2 != boundary_color )
                        {
                            int r2 = pix2 >> 16 & 0XFF;
                            int g2 = pix2 >>  8 & 0XFF;
                            int b2 = pix2       & 0XFF;

                            int r1 = pix1 >> 16 & 0XFF ;
                            int g1 = pix1 >>  8 & 0XFF ;
                            int b1 = pix1       & 0XFF ;

                            int dr = (r1 - r2)/3;
                            int dg = (g1 - g2)/3;
                            int db = (b1 - b2)/3;

                            pixels[ i ] = 0xFF000000 | ((r1-dr) << 16) | ((g1-dg) << 8) | (b1-db);
                            pixels[i+1] = 0xFF000000 | ((r2+dr) << 16) | ((g2+dg) << 8) | (b2+db);
                        }
                    }
                }
            }

            for ( int v=0; v<height-1; v++ )
            {
                for ( int h=0; h<width; h++ )
                {
                    int i = h+v*width;

                    int pix1 	= pixels[i];

                    if ( pix1 == boundary_color )
                    {
                        int pix2 	= pixels[i+width];

                        if ( pix2 != boundary_color )
                        {
                            int r2 = pix2 >> 16 & 0XFF;
                            int g2 = pix2 >>  8 & 0XFF;
                            int b2 = pix2       & 0XFF;

                            int r1 = pix1 >> 16 & 0XFF ;
                            int g1 = pix1 >>  8 & 0XFF ;
                            int b1 = pix1       & 0XFF ;

                            int dr = (r1 - r2)/3;
                            int dg = (g1 - g2)/3;
                            int db = (b1 - b2)/3;

                            pixels[ i ] 	= 0xFF000000 | ((r1-dr) << 16) | ((g1-dg) << 8) | (b1-db);
                            pixels[i+width] = 0xFF000000 | ((r2+dr) << 16) | ((g2+dg) << 8) | (b2+db);
                        }
                    }
                }
            }

            for ( int v=height-1; v>0; v-- )
            {
                for ( int h=0; h<width; h++ )
                {
                    int i = h+v*width;

                    int pix1 	= pixels[i];

                    if ( pix1 == boundary_color )
                    {
                        int pix2 	= pixels[i-width];

                        if ( pix2 != boundary_color )
                        {
                            int r2 = pix2 >> 16 & 0XFF;
                            int g2 = pix2 >>  8 & 0XFF;
                            int b2 = pix2       & 0XFF;

                            int r1 = pix1 >> 16 & 0XFF ;
                            int g1 = pix1 >>  8 & 0XFF ;
                            int b1 = pix1       & 0XFF ;

                            int dr = (r1 - r2)/3;
                            int dg = (g1 - g2)/3;
                            int db = (b1 - b2)/3;

                            pixels[ i ] 	= 0xFF000000 | ((r1-dr) << 16) | ((g1-dg) << 8) | (b1-db);
                            pixels[i-width] = 0xFF000000 | ((r2+dr) << 16) | ((g2+dg) << 8) | (b2+db);
                        }
                    }
                }
            }
        }
    }


    public void clip()
    {
        if ( clippingFrame != null )
        {
            this.getImageMatrix().invert( matrix );

            RectF dst = new RectF(0,0,0,0);
            RectF src = new RectF(clippingFrame);

            matrix.mapRect( dst, src );

            Bitmap oldBitMap = imageBitmap;

            int x = (int)Math.max( dst.left, 0 );
            int y = (int)Math.max( dst.top,  0 );
            int w = (int)Math.min( dst.width(), imageBitmap.getWidth()  -x );
            int h = (int)Math.min( dst.height(), imageBitmap.getHeight()-y );

            imageBitmap = Bitmap.createBitmap( imageBitmap, x, y, w, h );

            if ( oldBitMap != imageBitmap )
            {
                oldBitMap.recycle();
            }

            Bitmap aux1 = imageBitmap;

            if ( imageSize != null )
            {
                imageBitmap = Utils.resizeBitMap(imageBitmap, imageSize);
            }
            else
            {
                //imageBitmap = Utils.resizeBitMap(imageBitmap, diagonal);
            }

            if ( aux1 != imageBitmap )
            {
                aux1.recycle();
            }

            if ( roundBitMap )
            {
                Bitmap aux2 = imageBitmap;
                imageBitmap = Utils.roundBitMap( imageBitmap);
                aux2.recycle();
            }

            this.setBitmap( imageBitmap );
        }
    }



    public void eraseBackground( int startX, int startY )
    {
        Bitmap oldBitMap = imageBitmap;

        int   width 	= imageBitmap.getWidth();
        int   height	= imageBitmap.getHeight();
        int[] pixels 	= new int[width*height];

        Bitmap newBmp = imageBitmap.copy( Bitmap.Config.ARGB_8888 , true);

        int 	x 		= 0;
        int 	y 		= 0;
        int 	offset 	= 0;

        newBmp.getPixels(pixels, offset, width, x, y, width, height);

        bitMapArea = new Rect( 0, 0, newBmp.getWidth(), newBmp.getHeight() );

        flood( pixels, width, height, startX, startY, boundary_color );

        newBmp.setPixels(pixels, offset, width, x, y, width, height);

        this.setBitmap( newBmp );

        oldBitMap.recycle();
    }

    /*
    public double getDiagonal()
    {
        return diagonal;
    }

    public void setDiagonal(double diagonal )
    {
        this.diagonal = diagonal;
    }
*/
    public Bitmap getBitMap()
    {
        return imageBitmap;
    }

    public void recycle()
    {
        if ( imageBitmap != null )
        {
            //imageBitmap.recycle();
        }
    }

    public void eraseBG( )
    {
        //clip();

        eraseBackground( 1, 1 );
    }

    public void rotate( float anngle )
    {
        Matrix matrix = new Matrix();

        matrix.postRotate(anngle );

        Bitmap rotatedBitmap = Bitmap.createBitmap(imageBitmap, 0, 0, imageBitmap.getWidth(), imageBitmap.getHeight(), matrix, true);

        this.setBitmap(rotatedBitmap );
    }

}
