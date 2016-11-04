package cl.experti.haunter.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.threed.jpct.Camera;
import com.threed.jpct.FrameBuffer;
import com.threed.jpct.Interact2D;
import com.threed.jpct.Loader;
import com.threed.jpct.Object3D;
import com.threed.jpct.SimpleVector;
import com.threed.jpct.World;

import org.artoolkit.ar.jpct.ArJpctActivity;
import org.artoolkit.ar.jpct.TrackableObject3d;

import java.util.List;

import cl.experti.haunter.R;
import cl.experti.haunter.render.HaunterRender;

public class ARSimple extends ArJpctActivity {

    private static final String TAG = "ARSimple";
    private World world = null;
    private FrameBuffer frameBuffer = null;
    private Camera camera = null;
    private float touchTurn = 0;
    private float touchTurnUp = 0;

    private float xpos = -1;
    private float ypos = -1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_arsimple);
        findViewById(R.id.arLayout).setOnTouchListener(supplyRenderer());
    }

    /**
     * Use the FrameLayout in this Activity's UI.
     */
    @Override
    protected FrameLayout supplyFrameLayout() {
        return (FrameLayout) this.findViewById(R.id.arLayout);
    }

    public void configureWorld(World world) {
        world.setAmbientLight(150, 150, 150);
        this.world = world;
    }

    private Object3D getPorsche() {
        Object3D car = null;
        try {
            Object3D[] arrayObj = Loader.loadOBJ(this.getAssets().open("Data/models/Porsche_911_GT3.obj"),
                    this.getAssets().open("Data/models/Porsche_911_GT3.mtl"),
                    0.03f);

            car = Object3D.mergeAll(arrayObj);
            car.setName("porsche");
            //car.setCollisionMode(Object3D.COLLISION_CHECK_OTHERS);
        } catch (Exception e) {
            car = null;
            Log.e(TAG, String.format("Error al cargar modelo 3D PORSCHE", e.toString()), e);
        }
        return car;
    }

    private Object3D getFerrari() {
        Object3D car = null;
        try {
            Object3D[] arrayObj = Loader.loadOBJ(this.getAssets().open("Data/models/Ferrari_Modena_Spider.obj"),
                    this.getAssets().open("Data/models/Ferrari_Modena_Spider.mtl"),
                    0.03f);

            car = Object3D.mergeAll(arrayObj);
            car.setName("ferrari");
        } catch (Exception e) {
            car = null;
            Log.e(TAG, String.format("Error al cargar modelo 3D FERRARI", e.toString()), e);
        }
        return car;
    }

    protected void populateTrackableObjects(List<TrackableObject3d> list) {
        // Model A
        TrackableObject3d modelA = new TrackableObject3d("single;Data/markers/a.patt;10", getPorsche());
        list.add(modelA);

        /*
        // Model B
        TrackableObject3d modelB = new TrackableObject3d("single;Data/markers/b.patt;10", getFerrari());
        list.add(modelB);

        // Model C
        TrackableObject3d modelC = new TrackableObject3d("single;Data/markers/c.patt;10", getFerrari());
        list.add(modelC);

        // Model D
        TrackableObject3d modelD = new TrackableObject3d("single;Data/markers/d.patt;10", getFerrari());
        list.add(modelD);

        // Model F
        TrackableObject3d modelF = new TrackableObject3d("single;Data/markers/f.patt;10", getFerrari());
        list.add(modelF);

        // Model G
        TrackableObject3d modelG = new TrackableObject3d("single;Data/markers/g.patt;10", getFerrari());
        list.add(modelG);
        */
    }

    @Override
    protected HaunterRender supplyRenderer() {
        return new HaunterRender(this);
    }
}