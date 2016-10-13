package gti310.tp2.audio;

import java.io.FileNotFoundException;
import java.nio.ByteBuffer;

import gti310.tp2.io.FileSink;
import gti310.tp2.io.FileSource;

public class WavAudioFilter implements AudioFilter {

	// Files
	private String inputFilePath;
	private String outputFilePath;

	private FileSource wavFileIn;
	private FileSink wavFileOut;

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
				try {
					// If the format is valid, we recreate the FileSource to its
					// original.
					wavFileIn = new FileSource(inputFilePath);
					// This will be the output file
					wavFileOut = new FileSink(outputFilePath);

					System.out.println("[INFO] Processing audio...");
					byte[] buffer;

					// ChunkId
					buffer = wavFileIn.pop(4);
					chunkId = new String(buffer);
					System.out.println("ChunkId: " + chunkId);
					wavFileOut.push(buffer);

					// ChunkSize
					/**
					 * 
					 * Conversion help for little-endian src:
					 * http://stackoverflow.com/questions/5616052/how-can-i-convert-a-4-byte-array-to-an-integer
					 * 
					 **/
					buffer = wavFileIn.pop(4);
					chunkSize = wavFileIn.read_littleEndian(buffer).getInt();
					System.out.println("ChunkSize: " + chunkSize);
					wavFileOut.push(buffer);

					// Format
					buffer = wavFileIn.pop(4);
					format = new String(buffer);
					System.out.println("Format: " + format);
					wavFileOut.push(buffer);

					// Subchunk1Id
					buffer = wavFileIn.pop(4);
					subchunk1Id = new String(buffer);
					System.out.println("Subchunk1Id: " + subchunk1Id);
					wavFileOut.push(buffer);

					// Subchunk1Size
					buffer = wavFileIn.pop(4);
					subchunk1Size = wavFileIn.read_littleEndian(buffer).getInt();
					System.out.println("Subchunk1Size: " + subchunk1Size);
					wavFileOut.push(buffer);

					// AudioFormat
					buffer = wavFileIn.pop(2);
					audioFormat = wavFileIn.read_littleEndian(buffer).getShort();
					System.out.println("AudioFormat: " + audioFormat);
					wavFileOut.push(buffer);

					// NumChannels
					buffer = wavFileIn.pop(2);
					numChannels = wavFileIn.read_littleEndian(buffer).getShort();
					System.out.println("NumChannels: " + numChannels);
					wavFileOut.push(buffer);

					// SampleRate
					buffer = wavFileIn.pop(4);
					sampleRate = wavFileIn.read_littleEndian(buffer).getInt();
					System.out.println("SampleRate: " + sampleRate);
					wavFileOut.push(buffer);

					// ByteRate
					buffer = wavFileIn.pop(4);
					byteRate = wavFileIn.read_littleEndian(buffer).getInt();
					System.out.println("ByteRate: " + byteRate);
					
					/*
					 * We set the byteRate to 8000 in the output file
					 * 
					 */
					byte[] newByteRate = new byte[4];
					ByteBuffer newBuffer = ByteBuffer.wrap(newByteRate);
					newBuffer.putInt(8000);
					//wavFileOut.push(newBuffer.array());
					wavFileOut.push(newBuffer.array());
					
					// BlockAlign
					buffer = wavFileIn.pop(2);
					blockAlign = wavFileIn.read_littleEndian(buffer).getShort();
					System.out.println("BlockAlign: " + blockAlign);
					wavFileOut.push(buffer);

					// BitsPerSample
					buffer = wavFileIn.pop(2);
					bitsPerSample = wavFileIn.read_littleEndian(buffer).getShort();
					System.out.println("BitsPerSample: " + bitsPerSample);
					wavFileOut.push(buffer);

					// Subchunk2Id
					buffer = wavFileIn.pop(4);
					subchunk2Id = new String(buffer);
					System.out.println("Subchunk2Id: " + subchunk2Id);
					wavFileOut.push(buffer);

					// Subchunk2Size
					buffer = wavFileIn.pop(4);
					subchunk2Size = wavFileIn.read_littleEndian(buffer).getInt();
					System.out.println("Subchunk2Size: " + subchunk2Size);
					wavFileOut.push(buffer);

					// Data
					buffer = wavFileIn.pop(subchunk2Size);
					data = wavFileIn.read_littleEndian(buffer);
					System.out.println("Data: " + data);
					
					/*
					 * We convert the data to 8 kHz and store it in the output file
					 * 
					 * https://en.wikipedia.org/wiki/Decimation_%28signal_processing%29
					 * 
					 */
					// Create a ByteBuffer using a byte array of subchunk2Size
					byte[] bytes = new byte[subchunk2Size];
					ByteBuffer newDataBuffer = ByteBuffer.wrap(bytes);
					System.out.println(newDataBuffer);
//					for( byte aByte : bytes){
//						System.out.println(aByte);
//					}
					wavFileOut.push(buffer);

					// Done, closing files
					wavFileIn.close();
					wavFileOut.close();

					System.out.println("[INFO] Done compressing!");
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
				System.err.println("[ERROR] The file is not valid. Exiting.");
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
		numChannels = wavFileIn.read_littleEndian(buffer).getShort();
		System.out.println("NumChannels: " + numChannels);

		// SampleRate
		buffer = wavFileIn.pop(4);
		sampleRate = wavFileIn.read_littleEndian(buffer).getInt();
		System.out.println("SampleRate: " + sampleRate);

		// BitsPerSample
		wavFileIn.skip(6); // Skipping bytes not useful for check-up
		buffer = wavFileIn.pop(2);
		bitsPerSample = wavFileIn.read_littleEndian(buffer).getShort();
		System.out.println("BitsPerSample: " + bitsPerSample);

		if (format.equals("WAVE") && (numChannels == 1 || numChannels == 2) && sampleRate == 44100
				&& (bitsPerSample == 8 || bitsPerSample == 16)) {
			System.out.println("[INFO] The file is valid.\n");
			return true;
		} else {
			return false;
		}
	}
}
