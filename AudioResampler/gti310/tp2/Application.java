package gti310.tp2;

import gti310.tp2.audio.WavAudioFilter;

public class Application {

	/**
	 * Launch the application
	 * 
	 * @param args
	 *            This parameter is ignored
	 */
	public static void main(String args[]) {
		System.out.println("Audio Resample project!");

		new WavAudioFilter(args[0], args[1]).process();
	}
}
