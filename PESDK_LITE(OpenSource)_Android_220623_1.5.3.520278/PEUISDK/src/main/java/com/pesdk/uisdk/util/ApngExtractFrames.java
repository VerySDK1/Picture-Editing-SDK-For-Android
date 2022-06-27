package com.pesdk.uisdk.util;

import com.vecore.base.lib.utils.FileUtils;

import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Locale;

import ar.com.hjg.pngj.ChunkReader;
import ar.com.hjg.pngj.ChunkSeqReaderPng;
import ar.com.hjg.pngj.ImageInfo;
import ar.com.hjg.pngj.PngHelperInternal;
import ar.com.hjg.pngj.PngReader;
import ar.com.hjg.pngj.PngjException;
import ar.com.hjg.pngj.chunks.ChunkHelper;
import ar.com.hjg.pngj.chunks.ChunkRaw;
import ar.com.hjg.pngj.chunks.PngChunk;
import ar.com.hjg.pngj.chunks.PngChunkACTL;
import ar.com.hjg.pngj.chunks.PngChunkFCTL;
import ar.com.hjg.pngj.chunks.PngChunkFDAT;
import ar.com.hjg.pngj.chunks.PngChunkIDAT;
import ar.com.hjg.pngj.chunks.PngChunkIEND;
import ar.com.hjg.pngj.chunks.PngChunkIHDR;

/**
 * Source are taken from: https://github.com/leonbloy/pngj
 * <p>
 * This is low level, it does not use PngReaderApng
 * Extracts animation frames from APGN file to several PNG files<br>
 * Low level, very efficient. Does not compose frames<br>
 * Warning: this writes lots of files, in the same dir as the original PNGs.<br>
 * Options:<br>
 * -q: quiet mode<br>
 * Accepts paths in the form 'mypath/*' (all pngs in dir) or 'mypath/**' (idem recursive)<br>
 */
public class ApngExtractFrames {
    static String TAG = "ApngExtractFrames";

    static class PngReaderBuffered extends PngReader {
        private File orig;

        public PngReaderBuffered(File file) {
            super(file);
            this.orig = file;
        }

        FileOutputStream fo = null;
        File dest;
        ImageInfo frameInfo;
        int frameIndex = -1;

        @Override
        protected ChunkSeqReaderPng createChunkSeqReader() {
            return new ChunkSeqReaderPng(false) {
                @Override
                public boolean shouldSkipContent(int len, String id) {
                    return false; // we dont skip anything!
                }

                @Override
                protected boolean isIdatKind(String id) {
                    return false; // dont treat idat as special, jsut buffer it as is
                }

                @Override
                protected void postProcessChunk(ChunkReader chunkR) {
                    super.postProcessChunk(chunkR);
                    try {
                        String id = chunkR.getChunkRaw().id;
                        if (id.equals(PngChunkFCTL.ID)) {
                            frameIndex++;
                            PngChunk lastChunk = chunksList.getChunks().get(chunksList.getChunks().size() - 1);
                            frameInfo = ((PngChunkFCTL) lastChunk).getEquivImageInfo();
                            startNewFile();
                        }
                        if (id.equals(PngChunkFDAT.ID) || id.equals(PngChunkIDAT.ID)) {
                            if (fo != null) { //已存在则不需要创建帧文件
                                if (id.equals(PngChunkIDAT.ID)) {
                                    // copy IDAT as is (only if file is open == if FCTL previous == if IDAT is part of the animation
                                    chunkR.getChunkRaw().writeChunk(fo);
                                } else {
                                    // copy fDAT as IDAT, trimming the first 4 bytes
                                    ChunkRaw crawi =
                                            new ChunkRaw(chunkR.getChunkRaw().len - 4, ChunkHelper.b_IDAT, true);
                                    System.arraycopy(chunkR.getChunkRaw().data, 4, crawi.data, 0, crawi.data.length);
                                    crawi.writeChunk(fo);
                                }
                            }
                            chunkR.getChunkRaw().data = null; // be kind, release memory
                        }
                        if (id.equals(PngChunkIEND.ID)) {
                            if (fo != null)
                                endFile(); // end last file
                        }
                    } catch (Exception e) {
                        throw new PngjException(e);
                    }
                }
            };
        }

        private void startNewFile() throws Exception {
            if (fo != null) endFile();
            dest = createOutputName();
//            Log.e(TAG, "startNewFile: " + dest.getAbsolutePath() + ">>" + frameIndex + " >" + FileUtils.isExist(dest));
            if (!FileUtils.isExist(dest)) {  //原始帧文件不存在， 创建帧文件
                fo = new FileOutputStream(dest);
                fo.write(PngHelperInternal.getPngIdSignature());
                PngChunkIHDR ihdr = new PngChunkIHDR(frameInfo);
                ihdr.createRawChunk().writeChunk(fo);
                for (PngChunk chunk : getChunksList(false).getChunks()) {// copy all except actl and fctl, until IDAT
                    String id = chunk.id;
                    if (id.equals(PngChunkIHDR.ID) || id.equals(PngChunkFCTL.ID) || id.equals(PngChunkACTL.ID)) {
                        continue;
                    }
                    if (id.equals(PngChunkIDAT.ID)) {
                        break;
                    }
                    chunk.getRaw().writeChunk(fo);
                }
            }
        }

        private void endFile() throws IOException {
            new PngChunkIEND(null).createRawChunk().writeChunk(fo);
            fo.close();
            fo = null;
        }

        private File createOutputName() {
            return new File(orig.getParent(), getFileName(orig, frameIndex));
        }
    }

    /**
     * Get a formatted file name for a PNG file, which is extracted from the source at a specific frame index
     *
     * @param sourceFile Source file
     * @param frameIndex Position of the frame
     * @return File name
     */
    public static String getFileName(File sourceFile, int frameIndex) {
        String filename = sourceFile.getName();
        String baseName = FilenameUtils.getBaseName(filename);
        String extension = FilenameUtils.getExtension(filename);
        return String.format(Locale.ENGLISH, "%s_src_%03d.%s", baseName, frameIndex, extension);
    }

    /**
     * Reads a APNG file and tries to split it into its frames - low level! Returns number of animation frames extracted
     */
    public static int process(final File orig) {
        // we extend PngReader, to have a custom behavior: load all chunks opaquely, buffering all, and react to some
        // special chnks
        PngReaderBuffered pngr = new PngReaderBuffered(orig);
        pngr.end(); // read till end - this consumes all the input stream and does all!
        return pngr.frameIndex + 1;
    }


}
