/*
 * Copyright 2007 Alin Dreghiciu.
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ops4j.pax.exam.forked.provision;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;

import org.ops4j.lang.NullArgumentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Stream related utilities. TODO add units tests
 * 
 * @author Alin Dreghiciu
 * @since August 19, 2007
 */
public class StreamUtils {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(StreamUtils.class);

    /**
     * Utility class. Ment to be used via static methods.
     */
    private StreamUtils() {
        // utility class
    }

    /**
     * Copy a stream to a destination. It does not close the streams.
     * 
     * @param in
     *            the stream to copy from
     * @param out
     *            the stream to copy to
     * @param progressBar
     *            download progress feedback. Can be null.
     * 
     * @throws IOException
     *             re-thrown
     */
    public static void streamCopy(final InputStream in, final FileChannel out,
        final ProgressBar progressBar) throws IOException {
        NullArgumentException.validateNotNull(in, "Input stream");
        NullArgumentException.validateNotNull(out, "Output stream");
        final long start = System.currentTimeMillis();
        long bytes = 0;
        ProgressBar feedbackBar = progressBar;
        if (feedbackBar == null) {
            feedbackBar = new NullProgressBar();
        }
        try {
            ReadableByteChannel inChannel = Channels.newChannel(in);
            bytes = out.transferFrom(inChannel, 0, Integer.MAX_VALUE);
            inChannel.close();
        }
        finally {
            feedbackBar.increment(bytes, bytes / Math.max(System.currentTimeMillis() - start, 1));
            feedbackBar.stop();
        }
    }

    /**
     * Copy a stream from an urlto a destination.
     * 
     * @param url
     *            the url to copy from
     * @param out
     *            the stream to copy to
     * @param progressBar
     *            download progress feedback. Can be null.
     * 
     * @throws IOException
     *             re-thrown
     */
    public static void streamCopy(final URL url, final FileChannel out,
        final ProgressBar progressBar) throws IOException {
        NullArgumentException.validateNotNull(url, "URL");
        InputStream is = null;
        try {
            is = url.openStream();
            streamCopy(is, out, progressBar);
        }
        finally {
            if (is != null) {
                is.close();
            }
        }

    }

    /**
     * Feddback for downloading process.
     */
    public static interface ProgressBar {

        /**
         * Callback on download progress.
         * 
         * @param bytes
         *            download size from when the download started
         * @param kbps
         *            download speed
         */
        void increment(long bytes, long kbps);

        /**
         * Callback when download finished.
         */
        void stop();
    }

    /**
     * A progress bar that does nothing = does not display anything on console.
     */
    public static class NullProgressBar implements ProgressBar {

        public void increment(long bytes, long kbps) {
            // does nothing
        }

        public void stop() {
            // does nothing
        }

    }

    /**
     * A progress bar that displayed detailed information about downloading of an artifact
     */
    public static class FineGrainedProgressBar implements ProgressBar {

        int counter = 0;
        /**
         * Name of the downloaded artifact.
         */
        private final String downloadTargetName;

        public FineGrainedProgressBar(final String downloadTargetName) {
            this.downloadTargetName = downloadTargetName;
            Info.print(downloadTargetName + " : connecting...\r");
        }

        public void increment(final long bytes, final long kbps) {
            Info.print(downloadTargetName + " : " + bytes + " bytes @ [ " + kbps + "kBps ]\r");
            counter++;
        }

        public void stop() {
            Info.println();
        }

    }

    /**
     * A progress bar that displayed corse grained information about downloading of an artifact
     */
    public static class CoarseGrainedProgressBar implements ProgressBar {

        /**
         * Name of the downloaded artifact.
         */
        private final String downloadTargetName;
        private long bytes;
        private long kbps;

        public CoarseGrainedProgressBar(final String downloadTargetName) {
            this.downloadTargetName = downloadTargetName;
            LOGGER.debug(downloadTargetName + " : downloading...");
        }

        public void increment(final long bytes, final long kbps) {
            this.bytes = bytes;
            this.kbps = kbps;
        }

        public void stop() {
            LOGGER.debug(downloadTargetName + " : " + bytes + " bytes @ [ " + kbps + "kBps ]");
        }

    }

}
