package gti310.tp2.audio;

import java.io.FileNotFoundException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import gti310.tp2.io.FileSink;
import gti310.tp2.io.FileSource;

public class WavAudioFilter implements AudioFilter {

	// Files
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
		System.out.println(inputFilePath);
		try {
			this.wavFileIn = new FileSource(inputFilePath);
			this.wavFileOut = new FileSink(outputFilePath);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void process() {
		// TODO Auto-generated method stub
		System.out.println("Processing audio...");
		
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
		chunkSize = ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN).getInt();
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
		subchunk1Size = ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN).getInt();
		System.out.println("Subchunk1Size: " + subchunk1Size);
		wavFileOut.push(buffer);
		
		// AudioFormat
		buffer = wavFileIn.pop(2);
		audioFormat = ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN).getShort();
		System.out.println("AudioFormat: " + audioFormat);
		wavFileOut.push(buffer);
		
		// NumChannels
		buffer = wavFileIn.pop(2);
		numChannels = ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN).getShort();
		System.out.println("NumChannels: " + numChannels);
		wavFileOut.push(buffer);
		
		// SampleRate
		buffer = wavFileIn.pop(4);
		sampleRate = ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN).getInt();
		System.out.println("SampleRate: " + sampleRate);
		wavFileOut.push(buffer);
		
		// ByteRate
		buffer = wavFileIn.pop(4);
		byteRate = ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN).getInt();
		System.out.println("ByteRate: " + byteRate);
		wavFileOut.push(buffer);
		
		// BlockAlign
		buffer = wavFileIn.pop(2);
		blockAlign = ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN).getShort();
		System.out.println("BlockAlign: " + blockAlign);
		wavFileOut.push(buffer);
		
		// BitsPerSample
		buffer = wavFileIn.pop(2);
		bitsPerSample = ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN).getShort();
		System.out.println("BitsPerSample: " + bitsPerSample);
		wavFileOut.push(buffer);
		
		// Subchunk2Id
		buffer = wavFileIn.pop(4);
		subchunk2Id = new String(buffer);
		System.out.println("Subchunk2Id: " + subchunk2Id);
		wavFileOut.push(buffer);
		
		// Subchunk2Size
		buffer = wavFileIn.pop(4);
		subchunk2Size = ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN).getInt();
		System.out.println("Subchunk2Size: " + subchunk2Size);
		wavFileOut.push(buffer);
		
		// Data
		buffer = wavFileIn.pop(subchunk2Size);
		data = ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN);
		System.out.println("Data: " + data);
		wavFileOut.push(buffer);

		
		// Done, closing files
		wavFileIn.close();
		wavFileOut.close();
	}

}
