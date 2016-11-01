package gti310.tp2.audio;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import gti310.tp2.io.FileSink;
import gti310.tp2.io.FileSource;

/***
 * 
 * @author 
 *
 */
public class WavAudioFilter implements AudioFilter {

	// Files
	private String inputFilePath;
	private String outputFilePath;

	private FileSource wavFileIn;
	private FileSink wavFileOut;

	private static final int MASK = 0xFF;
	private static final int RESAMPLE_RATE = 8000;
	private double resampleFactor = 0;
	private Integer newSubchunk2Size = 0;
	
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
	private ByteBuffer data_buffer; // little endian

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
	
	private ByteBuffer bytesBuffer; // little endian
	
	public WavAudioFilter(String inputFilePath, String outputFilePath) {
		// TODO Auto-generated constructor stub
		this.inputFilePath = inputFilePath;
		this.outputFilePath = outputFilePath;
	}

	@Override
	/***
	 *  
	 */
	public void process() {
		// TODO Auto-generated method stub
		System.out.println("[INFO] Checking file before processing...");
		try {
			// This file source will be tested before processing.
			wavFileIn = new FileSource(inputFilePath);
			wavFileOut = new FileSink(outputFilePath);
			readDataFormat();  // O(1)
			
			// TODO : Test file is valid.
			if (isFileSourceValid()) {
				System.out.println("[INFO] The file is valid.\n");
				calculateResampleRatioValues();	// O(1)
				writeNewFileDataFormat();		// O(1)
				resampleAudioFile(); 			// O(N)
			} 
			else {
				System.err.println("[ERROR] The file is not valid. The application will not close.");
			}
		} catch (FileNotFoundException e) {
			System.err.println("[ERROR] File not found. The application will not close.");
		}
		finally {
			// TODO test exception
			wavFileIn.close();
			wavFileOut.close();
		}
	}

	/***
	 * O(1)
	 */
	private void calculateResampleRatioValues() {
		 resampleFactor = sampleRate.doubleValue() / (double)RESAMPLE_RATE;
		 newSubchunk2Size = (int) Math.ceil( (double)(subchunk2Size / resampleFactor) );
	}

	/***
	 * O(1)
	 */
	private void writeNewFileDataFormat() {
		// TODO exception
		byte[] newChunkSize_bytes;
		byte[] newSampleRate_bytes;
		byte[] newByteRate_bytes;
		byte[] newBlockAlign_bytes;
		byte[] newSubchunk2Size_bytes;
		
		/* Setting file parameters for 8000Hz */
		newSampleRate_bytes = create_littleEndian(4, RESAMPLE_RATE);
		newByteRate_bytes = create_littleEndian(4, RESAMPLE_RATE * numChannels * bitsPerSample / 8);
		newBlockAlign_bytes = create_littleEndian(2, numChannels * bitsPerSample / 8);
		newSubchunk2Size_bytes = create_littleEndian(4, newSubchunk2Size);
		newChunkSize_bytes = create_littleEndian(4, 36 + newSubchunk2Size);

		wavFileOut.push(chunkId_bytes); 
		wavFileOut.push(newChunkSize_bytes);
		wavFileOut.push(format_bytes); 
		wavFileOut.push(subchunk1Id_bytes); 
		wavFileOut.push(subchunk1Size_bytes); 
		wavFileOut.push(audioFormat_bytes); 
		wavFileOut.push(numChannels_bytes); 
		wavFileOut.push(newSampleRate_bytes);
		wavFileOut.push(newByteRate_bytes);
		wavFileOut.push(newBlockAlign_bytes);
		wavFileOut.push(bitsPerSample_bytes); 
		wavFileOut.push(subchunk2Id_bytes); 
		wavFileOut.push(newSubchunk2Size_bytes);
	}
	
	/***
	 * Read Wav data format and output the information.
	 * O(1)
	 */
	private void readDataFormat() {
		try {
			wavFileIn = new FileSource(inputFilePath);
			System.out.println("[INFO] Processing audio...");

			// ChunkId
			chunkId_bytes = wavFileIn.pop(4);
			chunkId = new String(chunkId_bytes);
			System.out.println("ChunkId: " + chunkId);
			
			// ChunkSize
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


		} catch (FileNotFoundException e) {
			// TODO : test exception.
			System.err.println("[ERROR] The file header is not valid. The application will not close.");
		}
	}

	/***
	 * 
	 * O(N)
	 */
	private void resampleAudioFile() {
		int iBufferPosToRead = 0;	   // Index of where to read samples on the memory buffer.
		int iCptBufferFrame = 0;	   // Counter of the number of frame process for current the memory buffer.
		int iCptFrame = 0;			   // Counter of the total number of frame process.

		// Temporary memory buffers. For the buffer size, we read from the source the number of frames for each second.
		//  ex: If the source is 44.1kHz 8bits Mono, we will have a buffer size of 8000 possible frames.
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		
		try {
			// Initial values.
			int iBufferSize = this.RESAMPLE_RATE * this.blockAlign * this.numChannels;
			bytesBuffer =  ByteBuffer.wrap(wavFileIn.pop(iBufferSize));
			bytesBuffer.order(ByteOrder.LITTLE_ENDIAN);
			
			for (int i = 0; i < (subchunk2Size - blockAlign); i = (int) Math.round(iCptFrame * resampleFactor) * blockAlign) {
				// Frame index of the temporary memory buffer.
				iBufferPosToRead =  (int) Math.round(iCptBufferFrame * resampleFactor) * blockAlign;
		
				// Bytes frame always start at an even index when blockAlign > 1.
				if (blockAlign > 1 && iBufferPosToRead % 2 != 0) ++iBufferPosToRead;
				
				// Once the temporary memory buffer is full, we push the frames to the sink 
				//  and start over with a new set of frames from source.
				if (iBufferPosToRead >= iBufferSize) {
					this.wavFileOut.push(outputStream.toByteArray());
					outputStream.reset();
					bytesBuffer = ByteBuffer.wrap(wavFileIn.pop(iBufferSize)); // 1 second of frames
					iBufferPosToRead = 0;
					iCptBufferFrame= 0;
				}
					
				byte[] aFrameResult;
		
				// No interpolation for the initial frame.
				aFrameResult = (i == 0 ) ? this.getSoundFrame(0) : 
										   GetLinearInterpolationFrameValues(iCptBufferFrame, iBufferPosToRead);
					
				// Append the frame values for the new sampling.
				outputStream.write(aFrameResult);
					
				++iCptFrame;
				++iCptBufferFrame;
			}
		
			// Append the remaining frame values.
			this.wavFileOut.push(outputStream.toByteArray());
		}
		catch (Exception e) {
			System.err.println("[ERROR] Error while resampling the audio file.");
		}
		finally {
			try {
				outputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		System.out.println("New Subchunk2Size: " + newSubchunk2Size);
		System.out.println("Number of sound frame inserted: " + iCptFrame);
	}

	// O(2)
	private byte[] GetLinearInterpolationFrameValues(int iCptBufferFrame, int iBufferPosToRead) {
		byte[] frameResult = new byte[this.blockAlign];
		double iPosX = iCptBufferFrame * resampleFactor * this.blockAlign;
		int iPosX1 = 0;
		int iPosX2 = 0;
		
		// Adjustment of the interpolation range values (X1 and X2).
		if (iBufferPosToRead - iPosX > 0) {
			iPosX1 = iBufferPosToRead - this.blockAlign;
			iPosX2 = iBufferPosToRead;
		}
		else {
			iPosX1 = iBufferPosToRead;
			iPosX2 = iBufferPosToRead + this.blockAlign;
		}

		byte[] soundFrameY1 = getSoundFrame(iPosX1);
		byte[] soundFrameY2 = getSoundFrame(iPosX2);
		
		// O(2), maximum of 2 channels.
		for (short noChannel = 0; noChannel < this.numChannels; ++noChannel){
			// 8 bits
			if (this.bitsPerSample == 8) {
			    short Y1_leftFrameValue = (short) (soundFrameY1[noChannel] & MASK);
			    short Y2_rightFrameValue = (short) (soundFrameY2[noChannel] & MASK);
			    frameResult[noChannel] = (byte)applyLinearInterpolation(iPosX, iPosX1, iPosX2, Y1_leftFrameValue, Y2_rightFrameValue);
			}
			// 16 bits
			else {
				// The right side index start at 2 for 16 bits stereo file.
				if (noChannel == 1) noChannel = 2; 
				
				short Y1_leftFrameValue = convertBytesToShortLittleEndian(soundFrameY1[noChannel], soundFrameY1[noChannel + 1]);
			    short Y2_rightFrameValue = convertBytesToShortLittleEndian(soundFrameY2[noChannel], soundFrameY2[noChannel + 1]);
			    short interpolatedValue = applyLinearInterpolation(iPosX, iPosX1, iPosX2, Y1_leftFrameValue, Y2_rightFrameValue);

			    byte[] newFrameValues =  convertShortSamplesToBytes(interpolatedValue);
			    frameResult[noChannel] = newFrameValues[0];
			    frameResult[noChannel + 1] = newFrameValues[1];
			}
		}
		return frameResult;
	}

	//http://www.blueleafsoftware.com/Products/Dagra/LinearInterpolationExcel.php
	// O(1)
	private short applyLinearInterpolation(double x, int x1, int x2, short y1, short y2) {
		// TODO : check if x, x1, x2 based on time-position instead make more sense.
		return (short) (y1 + (x - x1) * ( (y2 - y1) / (x2 - x1 ) ));
	}

	/***
	 * Get the sound frame values at a specified position of the memory buffer.
	 * @param Memory buffer frame position
	 * @return byte[] Sound frame information
	 * O(1)
	 */
	private byte[] getSoundFrame(int framePosition) {
		// TODO Handle exception
		byte[] bFrame = null;
		
		if (this.bitsPerSample == 8 && this.numChannels == 1) {
			// Mono-8bits
			bFrame = new byte[1];
			bFrame[0] = bytesBuffer.get(framePosition);
		}
		else if ((this.bitsPerSample == 8 && this.numChannels == 2) || 
				 (this.bitsPerSample == 16 && this.numChannels == 1)) {
			// Stereo-8bits or Mono-16bits 
			bFrame = new byte[2];
			bFrame[0] = bytesBuffer.get(framePosition);
			bFrame[1] = bytesBuffer.get(framePosition + 1);
		}
		else {
			// Stereo-16 bits
			bFrame = new byte[4];
			bFrame[0] = bytesBuffer.get(framePosition);
			bFrame[1] = bytesBuffer.get(framePosition + 1);
			bFrame[2] = bytesBuffer.get(framePosition + 2);
			bFrame[3] = bytesBuffer.get(framePosition + 3);
		}
			
		return bFrame;
	}

	/**
	 * Checks if the file is valid for compression
	 * 
	 * @return true or false
	 * O(1)
	 */
	private boolean isFileSourceValid() {
		if (format.equals("WAVE") && (numChannels == 1 || numChannels == 2) && sampleRate == 44100
				&& (bitsPerSample == 8 || bitsPerSample == 16)) {
			return true;
		} else {
			return false;
		}
	}

	/***
	 * 
	 * @param sampleValue
	 * O(1)
	 */
	private byte[] convertShortSamplesToBytes(short sampleValue) {
		return new byte[] { (byte) (sampleValue & 0xFF), (byte) ((sampleValue >> 8) & 0xFF) };
	}
	
	/***
	 * 
	 * @param values
	 * @return
	 * O(1)
	 */
	private short convertBytesToShortLittleEndian(byte leftByte, byte rightByte) {
		return (short)( (rightByte << 8) | leftByte & 0xff);
	}

	
	/**
	 * Returns the byte array of a little endian value
	 * O(1)
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
	 * Returns a ByteBuffer with little endian byte order.
	 * http://stackoverflow.com/questions/5616052/how-can-i-convert-a-4-byte-array-to-an-integer
	 * O(1)
	 */
	private ByteBuffer read_littleEndian(byte[] buffer) {
		return ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN);
	}
}
