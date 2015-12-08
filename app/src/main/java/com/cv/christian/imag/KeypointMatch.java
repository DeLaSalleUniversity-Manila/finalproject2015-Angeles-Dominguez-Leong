package com.cv.christian.imag;

import org.opencv.core.Mat;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;

import android.graphics.Bitmap;

public class KeypointMatch {

	final Mat image;
	final Mat descriptors = new Mat();
	final MatOfKeyPoint keypoints = new MatOfKeyPoint();
	boolean firstTime = true;

	public KeypointMatch(Mat image) {
		this.image = image.clone();
	}

	public void preCompute() {
		if (firstTime) {
			Detector.analyze(image, keypoints, descriptors);
			firstTime = false;
		}
	}

	public FinalScreen compare(KeypointMatch frame, boolean isHomogrpahy, boolean imageOnly) {

		FinalScreen s = new FinalScreen();


		MatOfKeyPoint f_keypoints = frame.keypoints;
		Mat f_descriptors = frame.descriptors;

		this.preCompute();
		frame.preCompute();


		// Compute matches
		MatOfDMatch matches = Detector.match(descriptors, f_descriptors);

		// Filter matches by distance

		MatOfDMatch filtered = Detector.filterMatchesByDistance(matches);

		// If count of matches is OK, apply homography check

		s.original_key1 = (int) descriptors.size().height;
		s.original_key2 = (int) f_descriptors.size().height;

		s.original_matches = (int) matches.size().height;
		s.dist_matches = (int) filtered.size().height;

		if (isHomogrpahy) {
			MatOfDMatch homo = Detector.filterMatchesByHomography(
					keypoints, f_keypoints, filtered);
			Bitmap bmp = Detector.drawMatches(image, keypoints,
					frame.image, f_keypoints, homo, imageOnly);
			s.bmp = bmp;
			s.homo_matches = (int) homo.size().height;
			return s;
		} else {
			Bitmap bmp = Detector.drawMatches(image, keypoints,
					frame.image, f_keypoints, filtered, imageOnly);
			s.bmp = bmp;
			s.homo_matches = -1;
			return s;

		}

	}
}
