package com.ayushhgoyal.magnifier;

import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.ZoomControls;

import java.io.IOException;


public class MainActivity extends ActionBarActivity {
    //    private SurfaceView surface_view;
    private Camera mCamera;
    SurfaceHolder.Callback sh_ob = null;
    SurfaceHolder surface_holder = null;
    SurfaceHolder.Callback sh_callback = null;
    private SurfaceView surfaceView;
    private ZoomControls zoomControls;
    private Camera.Parameters params;
    private int currentZoomLevel = 0;
    SeekBar seekBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().setFormat(PixelFormat.TRANSLUCENT);


        surfaceView = (SurfaceView) findViewById(R.id.surface_view);
//        zoomControls = (ZoomControls) findViewById(R.id.CAMERA_ZOOM_CONTROLS);
        seekBar = (SeekBar) findViewById(R.id.CAMERA_ZOOM_CONTROLS);


        if (surface_holder == null) {
            surface_holder = surfaceView.getHolder();
        }

        sh_callback = my_callback();
        surface_holder.addCallback(sh_callback);
    }

    SurfaceHolder.Callback my_callback() {
        SurfaceHolder.Callback ob1 = new SurfaceHolder.Callback() {

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                mCamera.stopPreview();
                mCamera.release();
                mCamera = null;
            }

            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                mCamera = Camera.open();
                mCamera.setDisplayOrientation(90);
                //set camera to continually auto-focus
                params = mCamera.getParameters();
                params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
                params.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);

                mCamera.setParameters(params);

                // zoom stuff

                if (params.isZoomSupported()) {
                    final int maxZoomLevel = params.getMaxZoom();
                    Log.i("max ZOOM ", "is " + maxZoomLevel);
//                    zoomControls.setIsZoomInEnabled(true);
//                    zoomControls.setIsZoomOutEnabled(true);
                    seekBar.setMax(maxZoomLevel);


                    seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                        @Override
                        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                            if (fromUser) {
                                currentZoomLevel = progress;
                                params.setZoom(currentZoomLevel);
                                mCamera.setParameters(params);
                            }
                        }

                        @Override
                        public void onStartTrackingTouch(SeekBar seekBar) {
                        }

                        @Override
                        public void onStopTrackingTouch(SeekBar seekBar) {
                        }
                    });

//                    zoomControls.setOnZoomInClickListener(new View.OnClickListener() {
//                        public void onClick(View v) {
//                            if (currentZoomLevel < maxZoomLevel) {
//                                currentZoomLevel = currentZoomLevel + 2;
//                                //mCamera.startSmoothZoom(currentZoomLevel);
//                                params.setZoom(currentZoomLevel);
//                                mCamera.setParameters(params);
//                            }
//                        }
//                    });
//
//                    zoomControls.setOnZoomOutClickListener(new View.OnClickListener() {
//                        public void onClick(View v) {
//                            if (currentZoomLevel > 0) {
//                                currentZoomLevel = currentZoomLevel - 2;
//                                params.setZoom(currentZoomLevel);
//                                mCamera.setParameters(params);
//                            }
//                        }
//                    });
                } else
                    zoomControls.setVisibility(View.GONE);


                try {
                    mCamera.setPreviewDisplay(holder);
                } catch (IOException exception) {
                    mCamera.release();
                    mCamera = null;
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width,
                                       int height) {
                mCamera.startPreview();
            }
        };
        return ob1;
    }
}