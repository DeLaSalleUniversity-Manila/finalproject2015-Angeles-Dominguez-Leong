package com.cv.christian.imag;

import android.graphics.Bitmap;

public class FinalScreen {
	int original_key1;
	int original_key2;	
	int original_matches;
	int dist_matches;
	int homo_matches;
	long elapsed;
	int idx;
	char letter;
	Bitmap bmp;
	
	@Override
	public String toString() {
		String result = "";

		switch(idx){
			case 0:
				letter = 'a';
				break;
			case 1:
				letter = 'b';
				break;
			case 2:
				letter = 'c';
				break;
			case 3:
				letter = 'd';
				break;
			case 4:
				letter = 'e';
				break;
			case 5:
				letter = 'f';
				break;
			case 6:
				letter = 'g';
				break;
			case 7:
				letter = 'h';
				break;
			case 8:
				letter = 'i';
				break;
			case 9:
				letter = 'j';
				break;
		}
		result +=   "Matched Image: " + letter;
		result += "\nTotal Matches: " + original_matches;
		result += "\nDistance Filtered Matches: " + dist_matches;
		result += "\nHomography Filtered Matches: " + homo_matches;
		result += "\nElapsed: (" + elapsed + " ms.)";
		return result;
	}
}
