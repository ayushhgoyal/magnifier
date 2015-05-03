package com.ayushhgoyal.magnifier;

import android.content.Intent;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.ZoomControls;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class MainActivity extends ActionBarActivity implements MediaScannerConnection.MediaScannerConnectionClient {
    private static final String TAG = "Magnifier";
    private static File mediaStorageDir;
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
    private Camera.PictureCallback mPicture;

    public static final int MEDIA_TYPE_IMAGE = 1;
    Button gallery_button;


    public String[] allFiles;
    private String SCAN_PATH;
    private static final String FILE_TYPE = "*/*";
    private MediaScannerConnection conn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().setFormat(PixelFormat.TRANSLUCENT);
        gallery_button = (Button) findViewById(R.id.button);
        mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "Magnifi");
        gallery_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });


        surfaceView = (SurfaceView) findViewById(R.id.surface_view);
//        zoomControls = (ZoomControls) findViewById(R.id.CAMERA_ZOOM_CONTROLS);
        seekBar = (SeekBar) findViewById(R.id.CAMERA_ZOOM_CONTROLS);


        if (surface_holder == null) {
            surface_holder = surfaceView.getHolder();
        }

        sh_callback = my_callback();
        surface_holder.addCallback(sh_callback);

        mPicture = new Camera.PictureCallback() {

            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
                Log.e(TAG, "Photo taken");

                try {
                    camera.startPreview();
                } catch (Exception e) {
                    e.printStackTrace();
                }


                File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
                if (pictureFile == null) {
                    Log.d(TAG, "Error creating media file, check storage permissions: ");
                    return;
                }

                try {
                    Log.e(TAG, "Photo starting save");
                    FileOutputStream fos = new FileOutputStream(pictureFile);
                    fos.write(data);
                    fos.close();
                    Log.e(TAG, "Photo saved");
                } catch (FileNotFoundException e) {
                    Log.d(TAG, "File not found: " + e.getMessage());
                } catch (IOException e) {
                    Log.d(TAG, "Error accessing file: " + e.getMessage());
                }
            }
        };

        try {
            surfaceView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mCamera.takePicture(null, null, mPicture);

                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            surfaceView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {

                    if (params.getFlashMode().equals(Camera.Parameters.FLASH_MODE_OFF)) {
                        params.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                    } else {
                        params.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                    }

                    mCamera.setParameters(params);
                    return true;
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        gallery_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                galleryCLickListener();
            }
        });


    }

    private void galleryCLickListener() {
        File folder = mediaStorageDir;
//                new File("/sdcard/Magnifi/");
        allFiles = folder.list();

        SCAN_PATH = mediaStorageDir.getPath() + File.separator + allFiles[0];

        if (conn != null) {
            conn.disconnect();
        }

        conn = new MediaScannerConnection(this, this);
        conn.connect();

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
//                params.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);

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

    /**
     * Create a file Uri for saving an image or video
     */
    private static Uri getOutputMediaFileUri(int type) {
        return Uri.fromFile(getOutputMediaFile(type));
    }


    /**
     * Create a File for saving an image or video
     */
    private static File getOutputMediaFile(int type) {
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

//        mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
//                Environment.DIRECTORY_PICTURES), "Magnifi");
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d("Magnifier", "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_" + timeStamp + ".jpg");
        }
        // BECAUSE we are not doing videos..but we will keep..just in case
//        else if(type == MEDIA_TYPE_VIDEO) {
//            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
//                    "VID_"+ timeStamp + ".mp4");
//        }
        else {
            return null;
        }

        return mediaFile;
    }


    @Override
    public void onMediaScannerConnected() {
        conn.scanFile(SCAN_PATH, FILE_TYPE);
    }


    @Override
    public void onScanCompleted(String path, Uri uri) {
        try {
            if (uri != null) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
//                intent.setData(uri);
                intent.setDataAndType(uri, "image/*");
                startActivity(intent);
            }
        } finally {
            conn.disconnect();
            conn = null;
        }
    }
}