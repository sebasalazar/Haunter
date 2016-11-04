package cl.experti.haunter.render;


import android.opengl.GLSurfaceView;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.threed.jpct.Camera;
import com.threed.jpct.FrameBuffer;
import com.threed.jpct.Interact2D;
import com.threed.jpct.Matrix;
import com.threed.jpct.SimpleVector;
import com.threed.jpct.World;

import org.artoolkit.ar.base.ARToolKit;
import org.artoolkit.ar.base.rendering.ARRenderer;
import org.artoolkit.ar.jpct.TrackableObject3d;

import java.util.List;

import javax.microedition.khronos.opengles.GL10;

import cl.experti.haunter.activity.ARSimple;

public class HaunterRender extends ARRenderer implements GLSurfaceView.OnTouchListener {
    private final ARSimple mActivity;
    private List<TrackableObject3d> mTrackableObjects;
    private World mWorld;
    private Camera mCamera;
    private FrameBuffer mBuffer;
    private Matrix projMatrix = new Matrix();
    private boolean mFovSet;

    private static final String TAG = "HaunterRender";
    private static final float Z_PLANE = 1.0f;
    private float touchTurn = 0;
    private float touchTurnUp = 0;
    private float xpos = -1;
    private float ypos = -1;

    public HaunterRender(ARSimple arJpctActivity) {
        this.mActivity = arJpctActivity;
    }

    public boolean configureARScene() {
        this.mWorld = new World();
        this.mCamera = this.mWorld.getCamera();
        android.hardware.Camera.Parameters params = this.mActivity.getCameraPreview().getCameraParameters();
        float fov = params.getHorizontalViewAngle();
        float yfov = params.getVerticalViewAngle();
        this.mCamera.setFOV(this.mCamera.convertDEGAngleIntoFOV(fov));
        this.mCamera.setYFOV(this.mCamera.convertDEGAngleIntoFOV(yfov));
        this.mActivity.configureWorld(this.mWorld);
        //this.mActivity.setCamera(this.mCamera);
        this.mTrackableObjects = this.mActivity.getTrackableObject3DList();

        for (int i = 0; i < this.mTrackableObjects.size(); ++i) {
            TrackableObject3d trackableObject = (TrackableObject3d) this.mTrackableObjects.get(i);
            if (!trackableObject.registerMarker()) {
                return false;
            }

            trackableObject.addToWorld(this.mWorld);
        }

        this.mWorld.buildAllObjects();
        this.mFovSet = false;
        return true;
    }

    public void onSurfaceChanged(GL10 unused, int w, int h) {
        super.onSurfaceChanged(unused, w, h);
        this.mBuffer = new FrameBuffer(unused, w, h);
//        this.mActivity.setFrameBuffer(this.mBuffer);
    }

    public final void draw(GL10 gl) {
        this.mBuffer.clear();
        float[] projection = ARToolKit.getInstance().getProjectionMatrix();
        this.projMatrix.setDump(projection);
        SimpleVector translation = this.projMatrix.getTranslation();
        if (!this.mFovSet) {
            float dir = projection[5];
            float up = (float) Math.atan2(1.0D, (double) dir) * 2.0F;
            this.mCamera.setYFovAngle(up);
            float i = projection[5] / projection[0];
            float trackableObject = (float) (2.0D * Math.atan2((double) this.mCamera.getYFOV(), 2.0D) * (double) i);
            this.mCamera.setFovAngle(trackableObject);
            this.mFovSet = true;
        }

        SimpleVector zAxis = this.projMatrix.getZAxis();
        SimpleVector yAxis = this.projMatrix.getYAxis();
        this.mCamera.setPosition(translation);
        this.mCamera.setOrientation(zAxis, yAxis);

        for (int i = 0; i < this.mTrackableObjects.size(); ++i) {
            TrackableObject3d obj3d = (TrackableObject3d) this.mTrackableObjects.get(i);
            obj3d.updateMarkerTransformation();
        }

        this.mWorld.renderScene(this.mBuffer);
        this.mWorld.draw(this.mBuffer);
        this.mBuffer.display();
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        Log.d(TAG, "Tocando pantalla");


        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            xpos = event.getX();
            ypos = event.getY();
            Log.d(TAG, String.format("Action Down (%f,%f)", xpos, ypos));

            if (mBuffer != null) {
                SimpleVector pos = Interact2D.reproject2D3D(mCamera, mBuffer, (int) xpos, (int) ypos, 50f);
                pos.matMul(mCamera.getBack().invert3x3());
                this.mTrackableObjects.get(0).clearTranslation();
                this.mTrackableObjects.get(0).translate(pos);
            }
            return true;
        }

        if (event.getAction() == MotionEvent.ACTION_UP) {
            xpos = -1;
            ypos = -1;
            touchTurn = 0;
            touchTurnUp = 0;
            Log.d(TAG, "Action Up");
            return true;
        }

        if (event.getAction() == MotionEvent.ACTION_MOVE) {
            float xd = event.getX() - xpos;
            float yd = event.getY() - ypos;

            xpos = event.getX();
            ypos = event.getY();

            touchTurn = xd / -100f;
            touchTurnUp = yd / -100f;

            if (mBuffer != null) {
                SimpleVector pos = Interact2D.reproject2D3D(mCamera, mBuffer, (int) xpos, (int) ypos, 50f);
                pos.matMul(mCamera.getBack().invert3x3());
                this.mTrackableObjects.get(0).clearTranslation();
                this.mTrackableObjects.get(0).translate(pos);
            }

            Log.d(TAG, String.format("Action Move (%f,%f)", xpos, ypos));
            return true;
        }

        try {
            Thread.sleep(15);
        } catch (Exception e) {
            // No need for this...
        }
        return false;
    }
}
