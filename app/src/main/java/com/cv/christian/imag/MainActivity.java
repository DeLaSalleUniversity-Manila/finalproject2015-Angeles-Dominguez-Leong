package com.cv.christian.imag;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

public class MainActivity extends Activity implements CvCameraViewListener2 {
	private CameraBridgeViewBase cameraview;
	Mat last;
	ArrayList<KeypointMatch> ref = new ArrayList<KeypointMatch>();
	KeypointMatch taken;
	ProgressDialog progress;
	ImageView matchDrawArea;
	Button addBtn;
	boolean ransacEnabled = true;
	Bitmap bmp;
	private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
		@Override
		public void onManagerConnected(int status) {
			switch (status) {
			case LoaderCallbackInterface.SUCCESS: {

				cameraview.enableView();
			}
				break;
			default: {
				super.onManagerConnected(status);
			}
				break;
			}
		}
	};

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		setContentView(R.layout.main_layout);
		matchDrawArea = (ImageView) findViewById(R.id.ImageView);
		addBtn = (Button) findViewById(R.id.button1);

		cameraview = (CameraBridgeViewBase) findViewById(R.id.camera);
		cameraview.setVisibility(SurfaceView.VISIBLE);

		cameraview.setCvCameraViewListener(this);
		cameraview.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				return false;
			}
		});

		ToggleButton ransacSwitch = (ToggleButton) findViewById(R.id.toggleButton1);
		ransacEnabled = true;
		ransacSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton arg0,
										 boolean arg1) {
				imageshow = !arg1;
			}
		});

		((ToggleButton) findViewById(R.id.toggleButton2))
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(CompoundButton arg0,
												 boolean arg1) {
						imageshow = !arg1;
					}
				});
	}

	boolean resize = false;
	boolean imageshow = true;

	@Override
	public void onPause() {
		super.onPause();
		if (cameraview != null)
			cameraview.disableView();
	}

	@Override
	public void onResume() {
		super.onResume();
		OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this,
				mLoaderCallback);
	}

	public void onDestroy() {
		super.onDestroy();
		if (cameraview != null)
			cameraview.disableView();
	}

	public void onCameraViewStarted(int width, int height) {

	}

	public void onCameraViewStopped() {
	}

	boolean showOriginal = true;

	public void cameraclick(View w) {
		showOriginal = !showOriginal;
	}
	public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
		last = inputFrame.rgba();
		return last;
	}

////
	////
	//////
	public Mat shapeDetect(Mat original, Mat gray) {
		Mat bw = new Mat();
		Imgproc.Canny(gray, bw, 0, 30);

		Mat draw = new Mat(bw.size(), bw.type());

		List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
		Imgproc.findContours(bw, contours, new Mat(), Imgproc.RETR_LIST,
				Imgproc.CHAIN_APPROX_SIMPLE);
		for (int i = 0; i < contours.size(); i++) {
			MatOfPoint kontor = contours.get(i);
			MatOfPoint2f kontor2f = new MatOfPoint2f(kontor.toArray());
			MatOfPoint2f approx2f = new MatOfPoint2f();
			Imgproc.approxPolyDP(kontor2f, approx2f,
					Imgproc.arcLength(kontor2f, true) * 0.02, true);

			double area = Math.abs(Imgproc.contourArea(approx2f));
			double points = approx2f.size().height;
			if (points >= 3 && points <= 6 && area > 500) {
				Imgproc.drawContours(draw, contours, i, new Scalar(255));
				String text = "A: " + area + ", " + points;
				ArrayList<Point> corner = new ArrayList<Point>();
				double[] pixels = new double[2];
				for (int i1 = 0; i1 < points; i1++) {
					double[] pix = approx2f.get(i1, 0);
					pixels[0] += pix[0];
					pixels[1] += pix[1];
					Point p = new Point(pix);
					corner.add(p);
				}

				Collections.sort(corner, new Comparator<Point>() {
					@Override
					public int compare(Point arg0, Point arg1) {
						return (int) (arg1.y - arg0.y);
					}
				});
				Point smallestY = corner.get(0);
				Point secSmallestY = corner.get(1);
				boolean left = smallestY.x < secSmallestY.x;

				Imgproc.putText(draw, "X", smallestY, Core.FONT_HERSHEY_PLAIN, 3,
						new Scalar(255));
				Imgproc.putText(draw, left ? "LEFT" : "RIGHT",
						new Point(550, 400), Core.FONT_HERSHEY_PLAIN, 2,
						new Scalar(255));

				pixels[0] = pixels[0] / points;
				pixels[1] = pixels[1] / points;

				Point p = new Point(pixels);

				Imgproc.putText(draw, "*", p, Core.FONT_HERSHEY_PLAIN, 2,
						new Scalar(255));
				Imgproc.line(draw, new Point(500, 0), new Point(500, 480),
						new Scalar(255));

				String t = (int) p.x + "-" + (int) p.y;
				boolean decision;
				if (left) {
					if (p.x < 500) {
						decision = true;
					} else {
						decision = false;
					}
				} else {
					if (p.x > 500) {
						decision = true;
					} else {
						decision = false;
					}
				}
				t = t + (decision ? "LEFT" : "RIGHT");

				Imgproc.putText(draw, t, new Point(20, 400),
						Core.FONT_HERSHEY_PLAIN, 2, new Scalar(255));

			}
		}
		return draw;
	}
/////
/////
///////
	public void loadref(View w) {
		Drawable myD = getResources().getDrawable(R.drawable.newa);
		for(int i = 0; i < 10; i++){
			switch(i) {
				case 0:
					myD = getResources().getDrawable(R.drawable.newa);
					break;
				case 1:
					myD=getResources().getDrawable(R.drawable.newb);
					break;
				case 2:
					myD=getResources().getDrawable(R.drawable.newc);
					break;
				case 3:
					myD=getResources().getDrawable(R.drawable.newd);
					break;
				case 4:
					myD=getResources().getDrawable(R.drawable.newe);
					break;
				case 5:
					myD=getResources().getDrawable(R.drawable.newf);
					break;
				case 6:
					myD=getResources().getDrawable(R.drawable.newg);
					break;
				case 7:
					myD=getResources().getDrawable(R.drawable.newh);
					break;
				case 8:
					myD=getResources().getDrawable(R.drawable.newi);
					break;
				case 9:
					myD=getResources().getDrawable(R.drawable.newj);
					break;
				/*case 10:
					myD=getResources().getDrawable(R.drawable.k);
					break;
				case 11:
					myD=getResources().getDrawable(R.drawable.l);
					break;
				case 12:
					myD=getResources().getDrawable(R.drawable.m);
					break;
				case 13:
					myD=getResources().getDrawable(R.drawable.n);
					break;
				case 14:
					myD=getResources().getDrawable(R.drawable.o);
					break;
				case 15:
					myD=getResources().getDrawable(R.drawable.p);
					break;
				case 16:
					myD=getResources().getDrawable(R.drawable.q);
					break;
				case 17:
					myD=getResources().getDrawable(R.drawable.r);
					break;
				case 18:
					myD=getResources().getDrawable(R.drawable.s);
					break;
				case 19:
					myD=getResources().getDrawable(R.drawable.t);
					break;
				case 20:
					myD=getResources().getDrawable(R.drawable.u);
					break;
				case 21:
					myD=getResources().getDrawable(R.drawable.v);
					break;
				case 22:
					myD=getResources().getDrawable(R.drawable.w);
					break;
				case 23:
					myD=getResources().getDrawable(R.drawable.x);
					break;
				case 24:
					myD=getResources().getDrawable(R.drawable.y);
					break;
				case 25:
					myD=getResources().getDrawable(R.drawable.z);
					break;*/
			}
		Bitmap bmp = ((BitmapDrawable)myD).getBitmap();
		matchDrawArea.setImageBitmap(bmp);
		Mat im = new Mat (bmp.getWidth(), bmp.getHeight(), CvType.CV_8UC1);
		Utils.bitmapToMat(bmp, im);
		KeypointMatch keypointMatch = new KeypointMatch(im);
		ref.add(keypointMatch);
		addBtn.setText("Load Reference (" + ref.size() + ")");
		}
	}
	public void takePic(View w) {
		Mat im = last.clone();
		Bitmap bmp = Bitmap.createBitmap(im.cols(), im.rows(),
				Bitmap.Config.ARGB_8888);
		Utils.matToBitmap(im, bmp);
		matchDrawArea.setImageBitmap(bmp);
		taken = new KeypointMatch(last);
	}

	public void compare(View w) {
		if (ref.size() == 0) {
			AlertDialog error = new AlertDialog.Builder(this).create();
			error.setTitle("Reference Images missing");
			error.setMessage("Please load the reference images by clicking the Load References button");
			error.setButton(AlertDialog.BUTTON_POSITIVE, "OK",
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
						}
					});
			error.show();
		} else if (taken == null) {
			AlertDialog error = new AlertDialog.Builder(this).create();
			error.setTitle("Take a picture");
			error.setMessage("Please take a picture by clicking on Take a picture button");
			error.setButton(AlertDialog.BUTTON_POSITIVE, "OK",
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
						}
					});
			error.show();
		} else {
			new BetterComparePics(this).execute();
		}
	}

	public void clear(View w) {
		ref.clear();
		addBtn.setText("Load References ( " + ref.size() + ")");
	}

	class BetterComparePics extends AsyncTask<Void, Integer, FinalScreen> {
		Context context;

		public BetterComparePics(Context context) {
			this.context = context;
		}

		@Override
		protected void onPreExecute() {
			progress = new ProgressDialog(context);
			progress.setCancelable(false);
			progress.setMessage("Starting to Compare Images");
			progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			progress.setProgress(1);
			progress.setMax(ref.size());
			progress.show();
			super.onPreExecute();
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			progress.setProgress(values[0]);
			super.onProgressUpdate(values);
		}

		@Override
		protected FinalScreen doInBackground(Void... params) {
			long s = System.currentTimeMillis();
			KeypointMatch max = null;
			FinalScreen detect = null;
			int maxD = -1;
			int index = -1;
			for (int i = 0; i < ref.size(); i++) {
				KeypointMatch scn = ref.get(i);
				FinalScreen data = taken.compare(scn, ransacEnabled,
						imageshow);
				int current;
				if (ransacEnabled) {
					current = data.homo_matches;
				} else {
					current = data.dist_matches;
				}

				if (current > maxD) {
					max = scn;
					detect = data;
					maxD = current;
					index = i;
				}
				this.publishProgress(i + 1);
			}

			bmp = detect.bmp;
			long e = System.currentTimeMillis();
			detect.elapsed = e - s;
			detect.idx = index;

			return detect;
		}

		@Override
		protected void onPostExecute(FinalScreen maximumData) {

			progress.dismiss();
			final Dialog fwindow = new Dialog(context);
			fwindow.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
			fwindow.setContentView(getLayoutInflater().inflate(
					R.layout.image_layout, null));
			ImageView im = (ImageView) fwindow
					.findViewById(R.id.imagespop);
			Button dismiss = (Button) fwindow
					.findViewById(R.id.dismissBtn);
			TextView info = (TextView) fwindow
					.findViewById(R.id.infoText);

			im.setImageBitmap(bmp);
			dismiss.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					fwindow.dismiss();
				}
			});

			info.setText(maximumData.toString());

			fwindow.show();

			super.onPostExecute(maximumData);
		}
	}





}
