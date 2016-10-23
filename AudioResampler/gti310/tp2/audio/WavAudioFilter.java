package gti310.tp2.audio;

import java.io.FileNotFoundException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import gti310.tp2.io.FileSink;
import gti310.tp2.io.FileSource;

public class WavAudioFilter implements AudioFilter {

	// Files
	private String inputFilePath;
	private String outputFilePath;

	private FileSource wavFileIn;
	private FileSink wavFileOut;

	// Resampling
	private Integer resampleRate = 8000;

	// WAV decoding
	private String chunkId; // big endian
	private Integer chunkSize; // little endian
	private String format; // big endian
	private String subchunk1Id; // big endian
	private Integer subchunk1Size; // little endian
	private Short audioFormat; // little endian
	private Short numChannels; // little endian
	private Integer sampleRate; // little endian
	private Integer byteRate; // little endian
	private Short blockAlign; // little endian
	private Short bitsPerSample; // little endian
	private String subchunk2Id; // big endian
	private Integer subchunk2Size; // little endian
	private ByteBuffer data; // little endian

	private byte[] chunkId_bytes; // big endian
	private byte[] chunkSize_bytes; // little endian
	private byte[] format_bytes; // big endian
	private byte[] subchunk1Id_bytes; // big endian
	private byte[] subchunk1Size_bytes; // little endian
	private byte[] audioFormat_bytes; // little endian
	private byte[] numChannels_bytes; // little endian
	private byte[] sampleRate_bytes; // little endian
	private byte[] byteRate_bytes; // little endian
	private byte[] blockAlign_bytes; // little endian
	private byte[] bitsPerSample_bytes; // little endian
	private byte[] subchunk2Id_bytes; // big endian
	private byte[] subchunk2Size_bytes; // little endian
	private byte[] data_bytes; // little endian

	public WavAudioFilter(String inputFilePath, String outputFilePath) {
		// TODO Auto-generated constructor stub
		this.inputFilePath = inputFilePath;
		this.outputFilePath = outputFilePath;
	}

	@Override
	public void process() {
		// TODO Auto-generated method stub
		System.out.println("[INFO] Checking file before processing...");
		try {
			// This FileSource will be tested before processing.
			wavFileIn = new FileSource(inputFilePath);
			if (isValid()) {
				wavFileIn.close();
				readAudioFile();
				compressAudioFile();
			} else {
				System.err.println("[ERROR] The file is not valid. Exiting.");
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void readAudioFile() {
		try {
			// If the format is valid, we recreate the FileSource to its
			// original.
			wavFileIn = new FileSource(inputFilePath);
			// This will be the output file
			wavFileOut = new FileSink(outputFilePath);

			System.out.println("[INFO] Processing audio...");

			// ChunkId
			chunkId_bytes = wavFileIn.pop(4);
			chunkId = new String(chunkId_bytes);
			System.out.println("ChunkId: " + chunkId);

			// ChunkSize
			/**
			 * 
			 * Conversion help for little-endian src:
			 * http://stackoverflow.com/questions/5616052/how-can-i-convert-a-4-byte-array-to-an-integer
			 * 
			 **/
			chunkSize_bytes = wavFileIn.pop(4);
			chunkSize = read_littleEndian(chunkSize_bytes).getInt();
			System.out.println("ChunkSize: " + chunkSize);

			// Format
			format_bytes = wavFileIn.pop(4);
			format = new String(format_bytes);
			System.out.println("Format: " + format);

			// Subchunk1Id
			subchunk1Id_bytes = wavFileIn.pop(4);
			subchunk1Id = new String(subchunk1Id_bytes);
			System.out.println("Subchunk1Id: " + subchunk1Id);

			// Subchunk1Size
			subchunk1Size_bytes = wavFileIn.pop(4);
			subchunk1Size = read_littleEndian(subchunk1Size_bytes).getInt();
			System.out.println("Subchunk1Size: " + subchunk1Size);

			// AudioFormat
			audioFormat_bytes = wavFileIn.pop(2);
			audioFormat = read_littleEndian(audioFormat_bytes).getShort();
			System.out.println("AudioFormat: " + audioFormat);

			// NumChannels
			numChannels_bytes = wavFileIn.pop(2);
			numChannels = read_littleEndian(numChannels_bytes).getShort();
			System.out.println("NumChannels: " + numChannels);

			// SampleRate
			sampleRate_bytes = wavFileIn.pop(4);
			sampleRate = read_littleEndian(sampleRate_bytes).getInt();
			System.out.println("SampleRate: " + sampleRate);

			// ByteRate
			byteRate_bytes = wavFileIn.pop(4);
			byteRate = read_littleEndian(byteRate_bytes).getInt();
			System.out.println("ByteRate: " + byteRate);

			// BlockAlign
			blockAlign_bytes = wavFileIn.pop(2);
			blockAlign = read_littleEndian(blockAlign_bytes).getShort();
			System.out.println("BlockAlign: " + blockAlign);

			// BitsPerSample
			bitsPerSample_bytes = wavFileIn.pop(2);
			bitsPerSample = read_littleEndian(bitsPerSample_bytes).getShort();
			System.out.println("BitsPerSample: " + bitsPerSample);

			// Subchunk2Id
			subchunk2Id_bytes = wavFileIn.pop(4);
			subchunk2Id = new String(subchunk2Id_bytes);
			System.out.println("Subchunk2Id: " + subchunk2Id);

			// Subchunk2Size
			subchunk2Size_bytes = wavFileIn.pop(4);
			subchunk2Size = read_littleEndian(subchunk2Size_bytes).getInt();
			System.out.println("Subchunk2Size: " + subchunk2Size);

			// Data
			data_bytes = wavFileIn.pop(subchunk2Size);
			data = read_littleEndian(data_bytes);
			System.out.println("Data: " + data);

			// Done, closing file
			wavFileIn.close();

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void compressAudioFile() {

		// Testing purpose
		// byte[] newChunkSize_bytes = chunkSize_bytes;
		// byte[] newSampleRate_bytes = sampleRate_bytes;
		// byte[] newByteRate_bytes = byteRate_bytes;
		// byte[] newBlockAlign_bytes = blockAlign_bytes;
		// byte[] newSubchunk2Size_bytes = subchunk2Size_bytes;
		// byte[] newData_bytes = data_bytes;

		byte[] newChunkSize_bytes;
		byte[] newSampleRate_bytes;
		byte[] newByteRate_bytes;
		byte[] newBlockAlign_bytes;
		byte[] newSubchunk2Size_bytes;
		/*
		 * Setting the new size of the file (44100Hz -> 8000Hz makes a factor of
		 * 5.5125
		 */
		Double resampleFactor = sampleRate.doubleValue() / resampleRate.doubleValue();
		Double newSubchunk2SizeDouble = (Double) (subchunk2Size / resampleFactor);
		/* We round the double to closest int */
		Integer newSubchunk2Size = (int) Math.round(newSubchunk2SizeDouble);
		byte[] newData_bytes = new byte[newSubchunk2Size];
		/* Setting file parameters for 8000Hz */
		newSampleRate_bytes = create_littleEndian(4, resampleRate);
		newByteRate_bytes = create_littleEndian(4, resampleRate * numChannels * bitsPerSample / 8);
		newBlockAlign_bytes = create_littleEndian(2, numChannels * bitsPerSample / 8);
		newSubchunk2Size_bytes = create_littleEndian(4, newSubchunk2Size);
		newChunkSize_bytes = create_littleEndian(4, 36 + newSubchunk2Size);

		/* Resampling function */
		resample(resampleFactor, newSubchunk2Size, newData_bytes);

		wavFileOut.push(chunkId_bytes); // ok
		wavFileOut.push(newChunkSize_bytes);
		wavFileOut.push(format_bytes); // ok
		wavFileOut.push(subchunk1Id_bytes); // ok
		wavFileOut.push(subchunk1Size_bytes); // ok
		wavFileOut.push(audioFormat_bytes); // ok
		wavFileOut.push(numChannels_bytes); // ok
		wavFileOut.push(newSampleRate_bytes);
		wavFileOut.push(newByteRate_bytes);
		wavFileOut.push(newBlockAlign_bytes);
		wavFileOut.push(bitsPerSample_bytes); // ok
		wavFileOut.push(subchunk2Id_bytes); // ok
		wavFileOut.push(newSubchunk2Size_bytes);
		wavFileOut.push(newData_bytes);
		// Done, closing file
		wavFileOut.close();

		System.out.println("[INFO] Done compressing!");
	}

	/**
	 * Checks if the file is valid for compression
	 * 
	 * @return true or false
	 */
	private boolean isValid() {
		byte[] buffer;

		// Format
		wavFileIn.skip(8); // Skipping bytes not useful for check-up
		buffer = wavFileIn.pop(4);
		format = new String(buffer);
		System.out.println("Format: " + format);

		// NumChannels
		wavFileIn.skip(10); // Skipping bytes not useful for check-up
		buffer = wavFileIn.pop(2);
		numChannels = read_littleEndian(buffer).getShort();
		System.out.println("NumChannels: " + numChannels);

		// SampleRate
		buffer = wavFileIn.pop(4);
		sampleRate = read_littleEndian(buffer).getInt();
		System.out.println("SampleRate: " + sampleRate);

		// BitsPerSample
		wavFileIn.skip(6); // Skipping bytes not useful for check-up
		buffer = wavFileIn.pop(2);
		bitsPerSample = read_littleEndian(buffer).getShort();
		System.out.println("BitsPerSample: " + bitsPerSample);

		if (format.equals("WAVE") && (numChannels == 1 || numChannels == 2) && sampleRate == 44100
				&& (bitsPerSample == 8 || bitsPerSample == 16)) {
			System.out.println("[INFO] The file is valid.\n");
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Returns the byte array of a little endian value
	 */
	private byte[] create_littleEndian(int byteArraySize, Integer value) {
		byte[] bytes = null;
		if (byteArraySize == 4) {
			bytes = ByteBuffer.allocate(byteArraySize).order(ByteOrder.LITTLE_ENDIAN).putInt(value).array();
		}
		if (byteArraySize == 2) {
			bytes = ByteBuffer.allocate(byteArraySize).order(ByteOrder.LITTLE_ENDIAN).putShort(value.shortValue())
					.array();
		}
		return bytes;

	}

	/**
	 * Returns the ByteBuffer of a little endian
	 */
	private ByteBuffer read_littleEndian(byte[] buffer) {
		return ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN);
	}

	/**
	 * Linear interpolation function a_inf = a b_inf = f(a) a_sup = b b_sup =
	 * f(b) a_int = c Returns f(c)
	 */
	private byte interpolate(int a_inf, int a_sup, byte b_inf, byte b_sup, double a_int) {
//		System.out.println("f(a): " + b_inf);
//		System.out.println("f(b): " + b_sup);
//		System.out.println((b_sup - b_inf));
//		System.out.println((a_int - a_inf));
//		System.out.println((a_sup - a_inf));
		byte result = (byte) (b_inf + (((b_sup - b_inf) * (a_int - a_inf)) / (a_sup - a_inf)));
		return result;
	}

	private void resample(Double resampleFactor, Integer newSubchunk2Size, byte[] newData_bytes) {
		int j = 0;
		int bytePosToRead = 0;
		if (numChannels == 1) {
			if (bitsPerSample == 8) {
				// Reading 1 byte (1*8 bits - 1 byte per sample)
				for (double i = 0; i < subchunk2Size; i += resampleFactor) {
					bytePosToRead = (int) Math.round(i);
					if (j <= newSubchunk2Size) {
						System.arraycopy(data_bytes, bytePosToRead, newData_bytes, j, 1);
						j++;
					}
				}
				/*
				 * DEBUG PURPOSE
				 */
				System.out.println(newSubchunk2Size);
				System.out.println(j);
				System.out.println(subchunk2Size);
				System.out.println(bytePosToRead);
				System.out.println(bytePosToRead / j);
				System.out.println(data_bytes[bytePosToRead]);
				System.out.println(newData_bytes[j - 1]);
			}
			if (bitsPerSample == 16) {
				// Reading 2 bytes (1*16 bits - 2 bytes per sample)
				for (double i = 0; i < subchunk2Size; i += resampleFactor) {
					bytePosToRead = (int) Math.round(i);
					if (bytePosToRead == 0) {
						System.arraycopy(data_bytes, bytePosToRead, newData_bytes, 0, 2);
					} else if (bytePosToRead % 2 == 0) {
						byte[] sampleBytes = new byte[2];
						sampleBytes[0] = interpolate(bytePosToRead - 1, bytePosToRead, data_bytes[bytePosToRead - 1],
								data_bytes[bytePosToRead], i);
						sampleBytes[1] = interpolate(bytePosToRead, bytePosToRead + 1, data_bytes[bytePosToRead],
								data_bytes[bytePosToRead + 1], i+1);
						if (j <= newSubchunk2Size) {
							if (j == 0)
								j = 2;
							System.arraycopy(sampleBytes, 0, newData_bytes, j, 2);
							j = j + 2;
						}
					}
				}
				System.out.println(newSubchunk2Size);
				System.out.println(j);
			}
		}
		if (numChannels == 2) {
			if (bitsPerSample == 8) {
				// Reading 2 bytes (2*8 bits - 1 byte on the left & 1 byte on
				// the right)
				for (double i = 0; i < subchunk2Size; i += resampleFactor) {
					bytePosToRead = (int) Math.round(i);
					if (bytePosToRead % 2 == 0) {
						byte[] sampleBytes = new byte[2];
						sampleBytes[0] = data_bytes[bytePosToRead];
						sampleBytes[1] = data_bytes[bytePosToRead + 1];
						if (j <= newSubchunk2Size) {
							System.arraycopy(sampleBytes, 0, newData_bytes, j, 2);
							j = j + 2;
						}
					}
				}
				System.out.println(newSubchunk2Size);
				System.out.println(j);
			}
			if (bitsPerSample == 16) {
				// Reading 4 bytes (2*16 bits - 2 bytes on the left & 2 bytes on
				// the right)
				for (double i = 0; i < subchunk2Size; i += resampleFactor * 2) {
					bytePosToRead = (int) Math.round(i);
					if (bytePosToRead % 2 == 0 && (subchunk2Size - bytePosToRead) >= 4) {
						byte[] sampleBytes = new byte[4];
						sampleBytes[0] = data_bytes[bytePosToRead];
						sampleBytes[1] = data_bytes[bytePosToRead + 1];
						sampleBytes[2] = data_bytes[bytePosToRead + 2];
						sampleBytes[3] = data_bytes[bytePosToRead + 3];
						if (j <= newSubchunk2Size - 4) {
							System.arraycopy(sampleBytes, 0, newData_bytes, j, 4);
							j = j + 4;
						}
					}
				}
				System.out.println(newSubchunk2Size);
				System.out.println(j);
			}
		}
	}
}
