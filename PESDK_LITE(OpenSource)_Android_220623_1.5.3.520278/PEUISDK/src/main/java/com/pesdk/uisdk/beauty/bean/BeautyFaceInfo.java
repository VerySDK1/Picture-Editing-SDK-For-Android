package com.pesdk.uisdk.beauty.bean;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.RectF;
import android.os.Parcel;
import android.os.Parcelable;

import com.pesdk.uisdk.bean.CardImportResult;
import com.pesdk.uisdk.ui.card.widget.TestDrawView;
import com.vecore.models.VisualFilterConfig;
import com.vecore.utils.MiscUtils;

/**
 * 人脸
 */
public class BeautyFaceInfo implements Parcelable {

    /**
     * 脸部id
     */
    private int faceId;
    /**
     * 点
     */
    private PointF[] mFacePoint;
    /**
     * 脸部位置
     */
    private RectF mFaceRectF = new RectF();
    /**
     * 比例
     */
    private float mAspectRatio;

    /**
     * 瘦脸
     */
    private float mFaceLift = 0;
    /**
     * 下巴宽和高
     */
    private float mChinWidth;
    private float mChinHeight;
    /**
     * 额头
     */
    private float mForehead;
    /**
     * 鼻子
     */
    private float mNoseWidth;
    private float mNoseHeight;
    /**
     * 微笑
     */
    private float mSmile;
    /**
     * 眼睛
     */
    private float mEyeTilt;
    private float mEyeDistance;
    private float mEyeWidth;
    private float mEyeHeight;
    /**
     * 嘴巴
     */
    private float mMouthUpper;
    private float mMouthLower;
    private float mMouthWidth;


    // 大眼
    private float mValueEyes = 0;
    //瘦脸
    private float mValueFace = 0;

    private static final String TAG = "BeautyFaceInfo";

    public FaceHairInfo getHairInfo() {
        if (null == mHairInfo) {
            mHairInfo = new FaceHairInfo();
        }
        return mHairInfo;
    }

    public void setHairInfo(FaceHairInfo hairInfo) {
        mHairInfo = hairInfo;
    }

    private transient FaceHairInfo mHairInfo = new FaceHairInfo();


    public BeautyFaceInfo(int faceId, float asp, RectF faceRectF, PointF[] facePoint) {
        this.faceId = faceId;
        this.mAspectRatio = asp;
        this.mFacePoint = pointCopy(facePoint);
        if (faceRectF != null) {
            mFaceRectF.set(faceRectF);
        }
    }

    /**
     * 恢复剪同款时，需要依据新的媒体，重新设置人脸位置
     */
    public void restoreBaseParam(BeautyFaceInfo dst) {
        faceId = dst.faceId;
        mAspectRatio = dst.mAspectRatio;
        mFacePoint = pointCopy(dst.mFacePoint);
        if (dst.mFaceRectF != null) {
            mFaceRectF.set(dst.mFaceRectF);
        }
        mHairInfo = dst.getHairInfo().copy();
    }


    protected BeautyFaceInfo(Parcel in) {

        //当前读取的position
        int oldPosition = in.dataPosition();
        String tmp = in.readString();
        int parcelVersion = 0;
        if (VER_TAG.equals(tmp)) {
            parcelVersion = in.readInt();
            if (parcelVersion >= 1) {
                mHairInfo = in.readParcelable(FaceHairInfo.class.getClassLoader());
            }
        } else {
            //恢复到读取之前的index
            in.setDataPosition(oldPosition);
        }

        faceId = in.readInt();
        mFacePoint = in.createTypedArray(PointF.CREATOR);
        mFaceRectF = in.readParcelable(RectF.class.getClassLoader());
        mAspectRatio = in.readFloat();
        mFaceLift = in.readFloat();
        mChinWidth = in.readFloat();
        mChinHeight = in.readFloat();
        mForehead = in.readFloat();
        mNoseWidth = in.readFloat();
        mNoseHeight = in.readFloat();
        mSmile = in.readFloat();
        mEyeTilt = in.readFloat();
        mEyeDistance = in.readFloat();
        mEyeWidth = in.readFloat();
        mEyeHeight = in.readFloat();
        mMouthUpper = in.readFloat();
        mMouthLower = in.readFloat();
        mMouthWidth = in.readFloat();
        mValueEyes = in.readFloat();
        mValueFace = in.readFloat();
        mFiveSensesConfig = in.readParcelable(VisualFilterConfig.FaceAdjustmentExtra.class.getClassLoader());
        mFaceConfig = in.readParcelable(VisualFilterConfig.FaceAdjustment.class.getClassLoader());
    }

    //唯一指定标识，以后不能再更改
    private static final String VER_TAG = "220318BeautyFace";
    private static final int VER = 1; //序列化版本

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        {
            dest.writeString(VER_TAG);
            dest.writeInt(VER);
        }
        dest.writeParcelable(mHairInfo, flags);
        dest.writeInt(faceId);
        dest.writeTypedArray(mFacePoint, flags);
        dest.writeParcelable(mFaceRectF, flags);
        dest.writeFloat(mAspectRatio);
        dest.writeFloat(mFaceLift);
        dest.writeFloat(mChinWidth);
        dest.writeFloat(mChinHeight);
        dest.writeFloat(mForehead);
        dest.writeFloat(mNoseWidth);
        dest.writeFloat(mNoseHeight);
        dest.writeFloat(mSmile);
        dest.writeFloat(mEyeTilt);
        dest.writeFloat(mEyeDistance);
        dest.writeFloat(mEyeWidth);
        dest.writeFloat(mEyeHeight);
        dest.writeFloat(mMouthUpper);
        dest.writeFloat(mMouthLower);
        dest.writeFloat(mMouthWidth);
        dest.writeFloat(mValueEyes);
        dest.writeFloat(mValueFace);
        dest.writeParcelable(mFiveSensesConfig, flags);
        dest.writeParcelable(mFaceConfig, flags);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<BeautyFaceInfo> CREATOR = new Creator<BeautyFaceInfo>() {
        @Override
        public BeautyFaceInfo createFromParcel(Parcel in) {
            return new BeautyFaceInfo(in);
        }

        @Override
        public BeautyFaceInfo[] newArray(int size) {
            return new BeautyFaceInfo[size];
        }
    };

    public BeautyFaceInfo copy() {
        Parcel parcel = Parcel.obtain();
        writeToParcel(parcel, 0);
        parcel.setDataPosition(0);
        BeautyFaceInfo tmp = new BeautyFaceInfo(parcel);
        parcel.recycle();
        return tmp;
    }

    public RectF getFaceRectF() {
        return mFaceRectF;
    }

    public int getFaceId() {
        return faceId;
    }

    public void setFaceInfo(BeautyFaceInfo faceInfo) {
        if (faceInfo == null) {
            return;
        }
        mFaceLift = faceInfo.getFaceLift();

        mChinWidth = faceInfo.getChinWidth();
        mChinHeight = faceInfo.getChinHeight();

        mForehead = faceInfo.getForehead();

        mNoseWidth = faceInfo.getNoseWidth();
        mNoseHeight = faceInfo.getNoseHeight();

        mSmile = faceInfo.getSmile();

        mEyeTilt = faceInfo.getEyeTilt();
        mEyeDistance = faceInfo.getEyeDistance();
        mEyeWidth = faceInfo.getEyeWidth();
        mEyeHeight = faceInfo.getEyeHeight();

        mMouthUpper = faceInfo.getMouthUpper();
        mMouthLower = faceInfo.getMouthLower();
        mMouthWidth = faceInfo.getMouthWidth();

        //瘦脸、大眼
        mValueEyes = faceInfo.getValueEyes();
        mValueFace = faceInfo.getValueFace();
    }


    /**
     * 五官 滤镜
     */
    private VisualFilterConfig.FaceAdjustmentExtra mFiveSensesConfig;

    public VisualFilterConfig.FaceAdjustmentExtra getFiveSensesConfig() {
        if (mFiveSensesConfig == null) {
            PointF[] allPoints = pointCopy(mFacePoint);
            if (allPoints.length < 105) {
                return null;
            }
            mFiveSensesConfig = new VisualFilterConfig.FaceAdjustmentExtra();
            //点
            PointF[] cheekLeft = new PointF[]{allPoints[0], allPoints[4], allPoints[8], allPoints[13]};
            PointF[] cheekRight = new PointF[]{allPoints[33], allPoints[28], allPoints[25], allPoints[20]};
            PointF[] nose = new PointF[]{allPoints[47], allPoints[51]};
            PointF[] eyeLeft = new PointF[]{allPoints[55], allPoints[54], allPoints[53], allPoints[52], allPoints[57], allPoints[56]};
            PointF[] eyeRight = new PointF[]{allPoints[58], allPoints[59], allPoints[60], allPoints[61], allPoints[62], allPoints[63]};
            PointF[] lipOuter = new PointF[]{allPoints[84], allPoints[86], allPoints[88], allPoints[90], allPoints[92], allPoints[94]};
            PointF chinLower = allPoints[16];
            PointF lipUpperLow = allPoints[98];
            PointF lipLowerUpp = allPoints[102];
            mFiveSensesConfig.setFacePoints(mAspectRatio, cheekLeft, cheekRight, chinLower, nose, eyeLeft, eyeRight, lipOuter, lipUpperLow, lipLowerUpp);
        }
        //瘦脸
        mFiveSensesConfig.setFaceWidth(mFaceLift);
        //下巴
        mFiveSensesConfig.setChinWidth(mChinWidth);
        mFiveSensesConfig.setChinHeight(mChinHeight);
        //额头
        //mFaceConfig.setForehead(mForehead);
        //眼睛
        mFiveSensesConfig.setEyeDistance(mEyeDistance);
        mFiveSensesConfig.setEyeSlant(mEyeTilt);
        mFiveSensesConfig.setEyeWidth(mEyeWidth);
        mFiveSensesConfig.setEyeHeight(mEyeHeight);
        //鼻子
        mFiveSensesConfig.setNoseWidth(mNoseWidth);
        mFiveSensesConfig.setNoseHeight(mNoseHeight);
        //嘴巴
        mFiveSensesConfig.setMouthWidth(mMouthWidth);
        mFiveSensesConfig.setLipLower(mMouthLower);
        mFiveSensesConfig.setLipUpper(mMouthUpper);
        //微笑
        mFiveSensesConfig.setSmile(mSmile);

        return mFiveSensesConfig;
    }

    public void resetFiveSenses() {
        mFaceLift = 0;

        mChinWidth = 0;
        mChinHeight = 0;

        mForehead = 0;

        mNoseWidth = 0;
        mNoseHeight = 0;

        mSmile = 0;

        mEyeTilt = 0;
        mEyeDistance = 0;
        mEyeWidth = 0;
        mEyeHeight = 0;

        mMouthUpper = 0;
        mMouthLower = 0;
        mMouthWidth = 0;
    }


    /**
     * 瘦脸、大眼  滤镜
     */
    private VisualFilterConfig.FaceAdjustment mFaceConfig;

    public VisualFilterConfig.FaceAdjustment getFaceConfig() {
        if (mFaceConfig == null) {
            PointF[] allPoints = pointCopy(mFacePoint);
            if (allPoints.length < 105) {
                return null;
            }
            mFaceConfig = new VisualFilterConfig.FaceAdjustment();
            //点
            PointF[] faceList = new PointF[]{
                    allPoints[4], allPoints[16], allPoints[28], allPoints[46],
                    allPoints[104], allPoints[105], allPoints[0], allPoints[32],
                    allPoints[7], allPoints[26], allPoints[12], allPoints[21],
            };
            //人脸点
            mFaceConfig.setFacePoints(faceList);
        }
        mFaceConfig.setBigEyes(mValueEyes);
        mFaceConfig.setFaceLift(mValueFace);
        return mFaceConfig;
    }

    public boolean isAdjust() {
        return mFaceLift > 0 || mChinWidth > 0 || mChinHeight > 0 || mForehead > 0 || mNoseWidth > 0
                || mNoseHeight > 0 || mSmile > 0 || mEyeTilt > 0 || mEyeDistance > 0 || mEyeWidth > 0
                || mEyeHeight > 0 || mMouthUpper > 0 || mMouthLower > 0 || mMouthWidth > 0;
    }

    public void resetFace() {
        mValueEyes = 0;
        mValueFace = 0;
    }


    private PointF[] pointCopy(PointF[] pointFS) {
        if (pointFS == null) {
            return new PointF[0];
        }
        PointF[] pointF = new PointF[pointFS.length];
        for (int i = 0; i < pointFS.length; i++) {
            pointF[i] = new PointF(pointFS[i].x, pointFS[i].y);
        }
        return pointF;
    }


    public float getFaceLift() {
        return mFaceLift;
    }

    public float getChinWidth() {
        return mChinWidth;
    }

    public float getChinHeight() {
        return mChinHeight;
    }

    public float getForehead() {
        return mForehead;
    }

    public float getNoseWidth() {
        return mNoseWidth;
    }

    public float getNoseHeight() {
        return mNoseHeight;
    }

    public float getSmile() {
        return mSmile;
    }

    public float getEyeTilt() {
        return mEyeTilt;
    }

    public float getEyeDistance() {
        return mEyeDistance;
    }

    public float getEyeWidth() {
        return mEyeWidth;
    }

    public float getEyeHeight() {
        return mEyeHeight;
    }

    public float getMouthUpper() {
        return mMouthUpper;
    }

    public float getMouthLower() {
        return mMouthLower;
    }

    public float getMouthWidth() {
        return mMouthWidth;
    }

    public float getValueEyes() {
        return mValueEyes;
    }

    public float getValueFace() {
        return mValueFace;
    }

    public void setFaceLift(float faceLift) {
        mFaceLift = faceLift;
    }

    public void setChinWidth(float chinWidth) {
        mChinWidth = chinWidth;
    }

    public void setChinHeight(float chinHeight) {
        mChinHeight = chinHeight;
    }

    public void setForehead(float forehead) {
        mForehead = forehead;
    }

    public void setNoseWidth(float noseWidth) {
        mNoseWidth = noseWidth;
    }

    public void setNoseHeight(float noseHeight) {
        mNoseHeight = noseHeight;
    }

    public void setSmile(float smile) {
        mSmile = smile;
    }

    public void setEyeTilt(float eyeTilt) {
        mEyeTilt = eyeTilt;
    }

    public void setEyeDistance(float eyeDistance) {
        mEyeDistance = eyeDistance;
    }

    public void setEyeWidth(float eyeWidth) {
        mEyeWidth = eyeWidth;
    }

    public void setEyeHeight(float eyeHeight) {
        mEyeHeight = eyeHeight;
    }

    public void setMouthUpper(float mouthUpper) {
        mMouthUpper = mouthUpper;
    }

    public void setMouthLower(float mouthLower) {
        mMouthLower = mouthLower;
    }

    public void setMouthWidth(float mouthWidth) {
        mMouthWidth = mouthWidth;
    }

    public void setValueEyes(float valueEyes) {
        mValueEyes = valueEyes;
    }

    public void setValueFace(float valueFace) {
        mValueFace = valueFace;
    }

    public boolean equals(BeautyFaceInfo dst) {
        if (dst == null)
            return false;
        return dst.faceId == faceId &&
                dst.mFacePoint == mFacePoint &&
                dst.mValueFace == mValueFace &&
                dst.mValueEyes == mValueEyes &&
                dst.mEyeDistance == mEyeDistance &&
                dst.mSmile == mSmile &&
                dst.mFaceLift == mFaceLift;
    }

    public FaceEyeParam applyBaseEyeDistance() {
        //  * point_eyea location4<br>
//         * point_eyeb location5<br>

        //  allPoints[104], allPoints[105]
//        VisualFilterConfig.FaceAdjustment.setFacePoints()

        PointF eyeA = mFacePoint[104];
        PointF eyeB = mFacePoint[105];


        float eyeDistance = (Math.abs(eyeB.x - eyeA.x));  //眼睛宽,相对于原始完整图片  0~1.0f

        //两眼睛中心点在原始图片的坐标
        PointF mid = new PointF((eyeA.x + eyeB.x) / 2, (eyeA.y + eyeB.y) / 2);
//        Log.e(TAG, "getBaseEyeDistance: " + mFaceRectF + " " + eyeA + "*" + eyeB);


        return new FaceEyeParam(eyeDistance, mid);

    }

    /**
     * 换装需要的相关参数
     */
    public FaceClothesParam applyClothesParam() {

        PointF earA = mFacePoint[1];
        PointF earB = mFacePoint[31];

        float tmp = (Math.abs(earB.x - earA.x));

        PointF jaw = mFacePoint[16]; //下巴
        return new FaceClothesParam(tmp, new PointF(jaw.x, jaw.y));

    }


    public float getScale() {
        return mScale;
    }

    private transient float mScale = 1f;

    public float getDx() {
        return mDx;
    }

    public float getDy() {
        return mDy;
    }

    private transient float mDx = 0, mDy = 0;

    public PointF getNose() {
        return mFacePoint[46];
    }

    /**
     * 自动居中 （根据人脸位置）
     *
     * @param bmpW 人像Mask W
     * @param bmpH
     * @param asp  播放器比例
     * @return
     */
    public CardImportResult applyAutoCenter(TestDrawView testDrawView, Bitmap src, int bmpW, int bmpH, float asp) {

        PointF nose = mFacePoint[46]; //鼻子  （保证鼻子的X在预览区域的决定中心点(x,y)）

        int w = 1080;
        int h = (int) (w / asp);
        RectF rectF = new RectF(); //虚拟像素
        MiscUtils.fixShowRectByExpanding(bmpW * 1f / bmpH, w, h, rectF);
        RectF rec = new RectF();
        MiscUtils.fixShowRectFByExpanding(bmpW * 1f / bmpH, w, h, rec);


//        Log.e(TAG, "applyAutoCenter: bmpWH: " + bmpW + "*" + bmpH + " view-wh:" + w + "*" + h);
//        Log.e(TAG, "applyAutoCenter:  nose:" + nose + " " + rectF + " rec:" + rec);


        float offX = (0.5f - nose.x) * w;
        float offY = (0.5f - nose.y) * h;


        PointF eyeA = mFacePoint[104];
        PointF eyeB = mFacePoint[105];


//        Log.e(TAG, "applyAutoCenter: " + eyeA + " " + eyeB + " off:" + offX + "*" + offY);


        //完全居中
        //1.鼻子完全居中
        rectF.offset(offX, offY); // offset: nosie(0.5f,0.5f)

//        Log.e(TAG, "applyAutoCenter: off end: " + rectF);

        Point pa = new Point((int) (eyeA.x * bmpW), (int) (eyeA.y * bmpH));
        Point pb = new Point((int) (eyeB.x * bmpW), (int) (eyeB.y * bmpH));

        int angle = 0;
        float tmp; //眼睛0度时的宽与显示位置的比例
        //2.1 有旋转
        if (pa.y != pb.y) {//眼睛不齐平，人相需要旋转
            angle = (int) getDegree(pa.x, pa.y, pb.x, pb.y, pb.x, pa.y);

            if (pa.y > pb.y) {
                angle = -angle;
            }

            // 1. 旋转前 4个顶点 +鼻子坐标
            float[] srcLT = new float[]{rectF.left, rectF.top}; //4个坐标顶点
            float[] srcRT = new float[]{rectF.right, rectF.top};
            float[] srcLB = new float[]{rectF.left, rectF.bottom};
            float[] srcRB = new float[]{rectF.right, rectF.bottom};

            Matrix matrix = new Matrix();
            //1.1旋转
            matrix.postRotate(-angle, rectF.centerX(), rectF.centerY());


//            Log.e(TAG, "applyAutoCenter: 原始的 " + Arrays.toString(srcLT) + "_" + Arrays.toString(srcRT) + "_" + Arrays.toString(srcLB) + " _" + Arrays.toString(srcRB) + " angle:" + angle);


            float[] srcNosie = new float[]{w * 0.5f, h * 0.5f};

            //旋转之后

            float[] dstLT = new float[2]; //4个坐标顶点
            float[] dstRT = new float[2];
            float[] dstLB = new float[2];
            float[] dstRB = new float[2];

            float[] dstNosie = new float[2];


            matrix.mapPoints(dstLT, srcLT);
            matrix.mapPoints(dstRT, srcRT);
            matrix.mapPoints(dstLB, srcLB);
            matrix.mapPoints(dstRB, srcRB);

            matrix.mapPoints(dstNosie, srcNosie);


//            Log.e(TAG, "applyAutoCenter: 旋转后 " + Arrays.toString(dstLT) + "_" + Arrays.toString(dstRT) + "_" + Arrays.toString(dstLB) + " _" + Arrays.toString(dstRB));


            {
                //1.2 缩放
                RectF dstAfter = new RectF();
                matrix.mapRect(dstAfter, rectF);

                Matrix test = new Matrix();
                //1.1旋转
                test.postRotate(-angle, bmpW * 0.5f, bmpH * 0.5f);
                RectF a = new RectF(0, 0, bmpW, bmpH);
                RectF b = new RectF();
                test.mapRect(b, a);
                float px = getDistance(pa, pb); //眼睛距离px
                tmp = px / b.width(); // bmpW 需要换成旋转后的rect.width()
//                Log.e(TAG, "applyAutoCenter: rotate after dst: " + b + " " + tmp + " pa:" + pa + " pb:" + pb + " px:" + px);


                //1.2.按照眼睛的位置缩放
                {

                    matrix = new Matrix();
                    float fw = dstAfter.width();  //expadding 状态时View的像素
                    float fh = dstAfter.height();

                    float eyePx = tmp * fw; //两眼的距离px
                    float srcPx = demoEye * w; //标准图
                    float scale = srcPx / eyePx;
                    matrix.postScale(scale, scale, dstAfter.centerX(), dstAfter.centerY()); //1.鼻子对应的显示坐标作为放大的中心点缩放
                    mScale = scale;

//                    Log.e(TAG, "applyAutoCenter: " + scale);


                    float[] dstLTa = new float[2]; //4个坐标顶点
                    float[] dstRTa = new float[2];
                    float[] dstLBa = new float[2];
                    float[] dstRBa = new float[2];

                    float[] dstNosiea = new float[2];


                    matrix.mapPoints(dstLTa, dstLT);
                    matrix.mapPoints(dstRTa, dstRT);
                    matrix.mapPoints(dstLBa, dstLB);
                    matrix.mapPoints(dstRBa, dstRB);

                    matrix.mapPoints(dstNosiea, dstNosie);


                    dstLT = dstLTa;
                    dstRT = dstRTa;
                    dstLB = dstLBa;
                    dstRB = dstRBa;

                    dstNosie = dstNosiea;


                }
            }


//            {
//                //测试: c.计算相对于自身旋转的鼻子
//                Bitmap out = Bitmap.createBitmap(bmpW, bmpH, Bitmap.Config.ARGB_8888);
//                Canvas cv = new Canvas(out);
//                cv.drawColor(Color.YELLOW);
//
//                matrix = new Matrix();
//                matrix.postRotate(-angle, bmpW / 2, bmpH / 2);
//                cv.drawBitmap(src, matrix, null);
//
//
//                float[] dEyeA = new float[2];
//                float[] dEyeB = new float[2];
//
//                float[] dNosie = new float[2];
//
//                matrix.mapPoints(dEyeA, new float[]{pa.x, pa.y});
//                matrix.mapPoints(dEyeB, new float[]{pb.x, pb.y});
//
//                matrix.mapPoints(dNosie, new float[]{nose.x * bmpW, nose.y * bmpH});
//
//                Paint p = new Paint();
//                p.setColor(Color.RED);
//                p.setStyle(Paint.Style.STROKE);
//                p.setStrokeWidth(10);
//                cv.drawPoint(dEyeA[0], dEyeA[1], p);
//                cv.drawPoint(dEyeB[0], dEyeB[1], p);
//                p.setColor(Color.GREEN);
//                cv.drawPoint(dNosie[0], dNosie[1], p);
//
//
//                try {
//                    BitmapUtils.saveBitmapToFile(out, true, 100, PathUtils.getTempFileNameForSdcard("rotate", "png"));
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//
//                Log.e(TAG, "applyAutoCenter: src in bmp: " + pa + " " + pb);
//                Log.e(TAG, "applyAutoCenter: 相对于自身旋转后: " + Arrays.toString(dEyeA) + " B:" + Arrays.toString(dEyeB) + " nosie:" + Arrays.toString(dNosie));
//
//            }


            //2. 媒体旋转的中心点是自身,需要对旋转后的坐标系进行平移（保证鼻子在显示目标区域的中心点）

            float dx = srcNosie[0] - dstNosie[0];
            float dy = srcNosie[1] - dstNosie[1];

            mDx = dx;
            mDy = dy;
//            Log.e(TAG, "applyAutoCenter: srcNosie:" + srcNosie + " dstNosie:" + dstNosie + " dx: " + dx + "  " + dy);

            matrix = new Matrix();
            matrix.postTranslate(dx, dy); //坐标系平移，保证中心点仍在播放器可见区域的中心点

            float[] dstLT2 = new float[2]; //4个坐标顶点
            float[] dstRT2 = new float[2];
            float[] dstLB2 = new float[2];
            float[] dstRB2 = new float[2];

            float[] dstNosie2 = new float[2];  //用于检验平移是否正确


            matrix.mapPoints(dstLT2, dstLT);
            matrix.mapPoints(dstRT2, dstRT);
            matrix.mapPoints(dstLB2, dstLB);
            matrix.mapPoints(dstRB2, dstRB);

            matrix.mapPoints(dstNosie2, dstNosie);


//            Log.e(TAG, "applyAutoCenter: 平移之后：dstNosie2： " + Arrays.toString(dstNosie2));
//            Log.e(TAG, "applyAutoCenter 此时鼻子显示到播放器的中心点: " + Arrays.toString(dstLT2) + "_" + Arrays.toString(dstRT2) + "_" + Arrays.toString(dstLB2) + " _" + Arrays.toString(dstRB2));

            //3.再次旋转（恢复到角度为0,媒体的showRectF 都是相对于0度时）
            matrix = new Matrix();
            float x = (dstLT2[0] + dstRT2[0] + dstLB2[0] + dstRB2[0]) / 4;
            float y = (dstLT2[1] + dstRT2[1] + dstLB2[1] + dstRB2[1]) / 4;
            matrix.postRotate(angle, x, y); //旋转中心为4个顶点的中心点

            float[] dstLT3 = new float[2]; //4个坐标顶点
            float[] dstRT3 = new float[2];
            float[] dstLB3 = new float[2];
            float[] dstRB3 = new float[2];

            float[] dstNosie3 = new float[2];

            matrix.mapPoints(dstLT3, dstLT2);
            matrix.mapPoints(dstRT3, dstRT2);
            matrix.mapPoints(dstLB3, dstLB2);
            matrix.mapPoints(dstRB3, dstRB2);

            matrix.mapPoints(dstNosie3, dstNosie2);


//            Log.e(TAG, "applyAutoCenter: " + Arrays.toString(dstLT3) + "_" + Arrays.toString(dstRT3) + "_" + Arrays.toString(dstLB3) + " _" + Arrays.toString(dstRB3));

            //3.1 得到矩形坐标（角度为0）
            RectF dst = new RectF(dstLT3[0], dstLT3[1], dstRB3[0], dstRB3[1]);


            RectF out = new RectF(dst.left * 1f / w, dst.top * 1f / h, dst.right * 1f / w, dst.bottom * 1f / h);
//            Log.e(TAG, "applyAutoCenter: " + dst + " >" + out + " " + Arrays.toString(dstNosie3));


            testDrawView.setCallback(cv -> {
                Paint p = new Paint();
                p.setAntiAlias(true);
                p.setColor(Color.RED);
                p.setStyle(Paint.Style.STROKE);
                p.setStrokeWidth(10);

                int w1 = cv.getWidth();
                int h1 = cv.getHeight();
                RectF a = new RectF(out.left * w1, out.top * h1, out.right * w1, out.bottom * h1);
//                Log.e(TAG, "draw: " + w1 + "*" + h1 + " " + a + " " + out);
                cv.drawRect(a, p);


                p.setColor(Color.BLUE);
//                    cv.drawPoint(w * 0.5f, h * 0.5f, p); //鼻子
                cv.drawLine(0, 0, w1, h1, p);
                cv.drawLine(w1, 0, 0, h1, p);


            });
            testDrawView.postDelayed(() -> testDrawView.postInvalidate(), 1000);


            return new CardImportResult(out, angle);

        } else {
            angle = 0;
            tmp = Math.abs(eyeB.x - eyeA.x); //眼睛距离

            float fw = rectF.width();  //expadding 状态时View的像素
            float fh = rectF.height();

            //2.2二次缩放保证人像在播放器中的位置,避免原图人相区域过大过小
//            Log.e(TAG, "applyAutoCenter: before:" + rectF + " " + fw + "*" + fh);

            float eyePx = tmp * fw; //两眼的距离px
            float srcPx = demoEye * w; //标准图
            float scale = srcPx / eyePx;

            float scale1 = demoEye / (eyePx / w);


            Matrix matrix = new Matrix();

            //缩放中心：鼻子在播放器中的点


            matrix.postScale(scale, scale, w * 0.5f, h * 0.5f); //1.鼻子对应的显示坐标作为放大的中心点缩放
            RectF dst = new RectF();
            matrix.mapRect(dst, rectF);
            //2.缩放之后，再次校验保证鼻子的显示中心


            RectF out = new RectF(dst.left * 1f / w, dst.top * 1f / h, dst.right * 1f / w, dst.bottom * 1f / h);
//            Log.e(TAG, "applyAutoCenter: " + demoEye + "/" + tmp + " ====>" + scale + " " + "  dst:" + dst + " out:" + out);
//            Log.e(TAG, "applyAutoCenter: " + srcPx + "/" + eyePx + " ====>" + scale + "=?" + scale1 + "  dst:" + dst + " out:" + out);

            return new CardImportResult(out, angle);
        }


    }


    /**
     * 求角avb 对应的角度
     *
     * @param vertexPointX -- 角度对应顶点X坐标值
     * @param vertexPointY -- 角度对应顶点Y坐标值
     * @param aX           rt坐标X
     * @param aY
     * @param bX           与VertexPoint 水平夹角为0 的点  （ 3个点必须能构成直角三角形）
     * @param bY
     * @return
     */
    public static float getDegree(int vertexPointX, int vertexPointY, int aX, int aY, int bX, int bY) {
        return getAngle(vertexPointX, vertexPointY, aX, aY, bX, bY);
    }

    private static float getAngle(int v1, int v2, int a1, int a2, int b1, int b2) {
        // 计算旋转角度
        double a = spacing(a1, a2, v1, v2);

        double b = spacing(a1, a2, b1, b2);

        double c = spacing(b1, b2, v1, v2);

        double cosB = (a * a + c * c - b * b) / (2 * a * c);
        if (cosB > 1) {// 浮点运算的时候 cosB 有可能大于1.
            cosB = 1f;
        }
        // 新的旋转角度
        return (float) ((Math.acos(cosB) * 180 / Math.PI));
    }

    /**
     * 两点的距离
     */
    public static double spacing(float x1, float y1, float x2, float y2) {
        float x = x1 - x2;
        float y = y1 - y2;
        return Math.sqrt(x * x + y * y);
    }


    /**
     * 计算两点之间的距离 缩放
     */
    private float getDistance(Point pa, Point pb) {
        int xlen = Math.abs(pa.x - pb.x);
        int ylen = Math.abs(pa.y - pb.y);
        return (float) Math.sqrt(xlen * xlen + ylen * ylen);
    }


    //    private final float demoEye = 76f / 340;  //参照标准：眼睛的距离占预览区域的比例 (用于证件照:控制原图的缩放)
    private final float demoEye = 74f / 348;  //参照标准：眼睛的距离占预览区域的比例 (用于证件照:控制原图的缩放)


    @Override
    public String toString() {
        return "BeautyFaceInfo{" +
                "hash=" + hashCode() +
                ", faceId=" + faceId +
//                ", mFacePoint=" + Arrays.toString(mFacePoint) +
                ", mFaceRectF=" + mFaceRectF +
                ", mAspectRatio=" + mAspectRatio +
//                ", mFaceLift=" + mFaceLift +
//                ", mChinWidth=" + mChinWidth +
//                ", mChinHeight=" + mChinHeight +
//                ", mForehead=" + mForehead +
//                ", mNoseWidth=" + mNoseWidth +
//                ", mNoseHeight=" + mNoseHeight +
//                ", mSmile=" + mSmile +
//                ", mEyeTilt=" + mEyeTilt +
//                ", mEyeDistance=" + mEyeDistance +
//                ", mEyeWidth=" + mEyeWidth +
//                ", mEyeHeight=" + mEyeHeight +
//                ", mMouthUpper=" + mMouthUpper +
//                ", mMouthLower=" + mMouthLower +
//                ", mMouthWidth=" + mMouthWidth +
//                ", mValueEyes=" + mValueEyes +
//                ", mValueFace=" + mValueFace +
                ", mHairInfo=" + mHairInfo +
//                ", mFiveSensesConfig=" + mFiveSensesConfig +
//                ", mFaceConfig=" + mFaceConfig +
                '}';
    }


}
