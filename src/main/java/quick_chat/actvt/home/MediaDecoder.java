package quick_chat.actvt.home;

/* MediaDecoder

   Author: Andrew Stubbs (based on some examples from the docs)

   This class opens a file, reads the first audio channel it finds, and returns raw audio data.

   Usage:
      MediaDecoder decoder = new MediaDecoder("myfile.m4a");
      short[] data;
      while ((data = decoder.readShortData()) != null) {
         // process data here
      }
  */

import android.media.MediaCodec;
import android.media.MediaCodec.BufferInfo;
import android.media.MediaExtractor;
import android.media.MediaFormat;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class MediaDecoder
{
    private MediaExtractor extractor = new MediaExtractor();
    private MediaCodec decoder;

    private MediaFormat inputFormat;

    private ByteBuffer[] inputBuffers;
    private boolean end_of_input_file;

    private ByteBuffer[] outputBuffers;
    private int outputBufferIndex = -1;

    public MediaDecoder( String inputFilename ) throws IOException
    {
        extractor.setDataSource( inputFilename );

        // Select the first audio track we find.
        int numTracks = extractor.getTrackCount();

        for (int i = 0; i < numTracks; ++i)
        {
            MediaFormat format = extractor.getTrackFormat(i);

            String mime = format.getString(MediaFormat.KEY_MIME);

            if (mime.startsWith("audio/"))
            {
                extractor.selectTrack(i);
                decoder = MediaCodec.createDecoderByType( mime );
                decoder.configure(format, null, null, 0);
                inputFormat = format;
                break;
            }
        }

        if (decoder == null)
        {
            throw new IllegalArgumentException("No decoder for file format");
        }

        decoder.start();

        inputBuffers = decoder.getInputBuffers();

        outputBuffers = decoder.getOutputBuffers();

        end_of_input_file = false;
    }

    // Read the raw data from MediaCodec.
    // The caller should copy the data out of the ByteBuffer before calling this again
    // or else it may get overwritten.
    private ByteBuffer readData(BufferInfo info)
    {
        if (decoder == null)
            return null;

        for (;;)
        {
            // Read data from the file into the codec.
            if (!end_of_input_file)
            {
                int inputBufferIndex = decoder.dequeueInputBuffer(10000);
                if (inputBufferIndex >= 0)
                {
                    int size = extractor.readSampleData(inputBuffers[inputBufferIndex], 0);
                    if (size < 0)
                    {
                        // End Of File
                        decoder.queueInputBuffer(inputBufferIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                        end_of_input_file = true;
                    }
                    else
                        {
                        decoder.queueInputBuffer(inputBufferIndex, 0, size, extractor.getSampleTime(), 0);
                        extractor.advance();
                    }
                }
            }

            // Read the output from the codec.
            if (outputBufferIndex >= 0)
                // Ensure that the data is placed at the start of the buffer
                outputBuffers[outputBufferIndex].position(0);

            int result = decoder.dequeueOutputBuffer(info, 10000);

            outputBufferIndex = result;

            if (outputBufferIndex >= 0)
            {
                // Handle EOF
                if (info.flags != 0)
                {
                    decoder.stop();
                    decoder.release();
                    decoder = null;
                    return null;
                }

                // Release the buffer so MediaCodec can use it again.
                // The data should stay there until the next time we are called.
                decoder.releaseOutputBuffer(outputBufferIndex, false);

                return outputBuffers[outputBufferIndex];

            }
            else if (outputBufferIndex == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED)
            {
                // This usually happens once at the start of the file.
                outputBuffers = decoder.getOutputBuffers();
            }
        }
    }

    // Return the Audio sample rate, in samples/sec.
    public int getSampleRate()
    {
        return inputFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE);
    }

    // Read the raw audio data in 16-bit format
    // Returns null on EOF
    public short[] readShortData()
    {
        BufferInfo info = new BufferInfo();

        ByteBuffer data = readData(info);

        if (data == null)
            return null;

        int samplesRead = info.size/2;

        short[] returnData = new short[samplesRead];

        outputBuffers[outputBufferIndex].order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(returnData);

        //System.arraycopy(data.asShortBuffer().array(), 0, returnData, 0, samplesRead);

        return returnData;
    }
}
