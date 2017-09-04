package com.electromuis.smdl.Processing;

import com.sun.org.apache.xpath.internal.operations.Bool;
import net.sf.sevenzipjbinding.*;
import net.sf.sevenzipjbinding.impl.RandomAccessFileInStream;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Created by Electromuis on 15-5-2016.
 */
public class Extractor {
    static class ExtractionException extends Exception {
        private static final long serialVersionUID = -5108931481040742838L;

        ExtractionException(String msg) {
            super(msg);
        }

        public ExtractionException(String msg, Exception e) {
            super(msg, e);
        }
    }

    class ExtractCallback implements IArchiveExtractCallback {
        private IInArchive inArchive;
        private int index;
        private OutputStream outputStream;
        private File file;
        private ExtractAskMode extractAskMode;
        private boolean isFolder;
        private String packRoot;

        ExtractCallback(IInArchive inArchive, String packRoot) {
            this.inArchive = inArchive;
            this.packRoot = packRoot;
        }

        public void setTotal(long total) throws SevenZipException {

        }

        public void setCompleted(long completeValue) throws SevenZipException {

        }

        public ISequentialOutStream getStream(int index,
                                              ExtractAskMode extractAskMode) throws SevenZipException {
            closeOutputStream();

            this.index = index;
            this.extractAskMode = extractAskMode;
            this.isFolder = (Boolean) inArchive.getProperty(index,
                    PropID.IS_FOLDER);

            packDownloader.getBar().setProgress(((float)index) / inArchive.getNumberOfItems());

            if (extractAskMode != ExtractAskMode.EXTRACT) {
                // Skipped files or files being tested
                return null;
            }

            String path = (String) inArchive.getProperty(index, PropID.PATH);
            if(!path.startsWith(packRoot)){
                return null;
            }
            path = path.substring(packRoot.length()+(packRoot.contains(File.separator)?1:0), path.length());


            file = new File(outputDirectoryFile, path);
            if (isFolder) {
                createDirectory(file);
                return null;
            }

            createDirectory(file.getParentFile());

            try {
                outputStream = new FileOutputStream(file);
            } catch (FileNotFoundException e) {
                throw new SevenZipException("Error opening file: "
                        + file.getAbsolutePath(), e);
            }

            return new ISequentialOutStream() {
                public int write(byte[] data) throws SevenZipException {
                    try {
                        outputStream.write(data);
                    } catch (IOException e) {
                        throw new SevenZipException("Error writing to file: "
                                + file.getAbsolutePath());
                    }
                    return data.length; // Return amount of consumed data
                }
            };
        }

        private void createDirectory(File parentFile) throws SevenZipException {
            if (!parentFile.exists()) {
                if (!parentFile.mkdirs()) {
                    throw new SevenZipException("Error creating directory: "
                            + parentFile.getAbsolutePath());
                }
            }
        }

        private void closeOutputStream() throws SevenZipException {
            if (outputStream != null) {
                try {
                    outputStream.close();
                    outputStream = null;
                } catch (IOException e) {
                    throw new SevenZipException("Error closing file: "
                            + file.getAbsolutePath());
                }
            }
        }

        public void prepareOperation(ExtractAskMode extractAskMode)
                throws SevenZipException {

        }

        public void setOperationResult(
                ExtractOperationResult extractOperationResult)
                throws SevenZipException {
            closeOutputStream();
            String path = (String) inArchive.getProperty(index, PropID.PATH);
            if (extractOperationResult != ExtractOperationResult.OK) {
                throw new SevenZipException("Invalid file: " + path);
            }

            if (!isFolder) {
                switch (extractAskMode) {
                    case EXTRACT:
                        System.out.println("Extracted " + path);
                        break;
                    case TEST:
                        System.out.println("Tested " + path);

                    default:
                }
            }
        }

    }

    private String archive;
    private String outputDirectory;
    private File outputDirectoryFile;
    private PackRow packDownloader;

    Extractor(String archive, String outputDirectory, PackRow packDownloader) {
        this.archive = archive;
        this.outputDirectory = outputDirectory;
        this.packDownloader = packDownloader;
    }

    void extract() throws ExtractionException {
        checkArchiveFile();
        prepareOutputDirectory();
        extractArchive();
    }



    private static String getPackRoot(IInArchive inArchive) throws SevenZipException, ExtractionException {
        List<Integer> idList = new ArrayList<Integer>();
        int numberOfItems = inArchive.getNumberOfItems();

        boolean multiRoot = false;

        String lastFolder = null;
        for (int i = 0; i < numberOfItems; i++) {
            String path = (String) inArchive.getProperty(i, PropID.PATH);
            boolean isDir = (Boolean) inArchive.getProperty(i, PropID.IS_FOLDER);

            String found = path.contains(File.separator)?path.split(Pattern.quote(File.separator))[0]:path;
            if(lastFolder == null)
                lastFolder = found;
            else if(found.equals(lastFolder));
            else {
                multiRoot = true;
                break;
            }
        }

        if(multiRoot){
            //determine largest root folder
            Map<String, Long> rootSizes = new HashMap<String, Long>();
            for (int i = 0; i < numberOfItems; i++) {
                if(!(Boolean) inArchive.getProperty(i, PropID.IS_FOLDER)){
                    String path = (String) inArchive.getProperty(i, PropID.PATH);
                    String found[] = path.split(Pattern.quote(File.separator), 2);
                    if(found.length>1){
                        String foundRoot = found[0];
                        long size = (Long) inArchive.getProperty(i, PropID.SIZE);
                        if(!rootSizes.containsKey(foundRoot))
                            rootSizes.put(foundRoot, size);
                        else
                            rootSizes.put(foundRoot, size+rootSizes.get(foundRoot));
                    }
                }
            }

            Map.Entry<String, Long> maxEntry = null;

            for (Map.Entry<String, Long> entry : rootSizes.entrySet())
            {
                if (maxEntry == null || entry.getValue().compareTo(maxEntry.getValue()) > 0)
                {
                    maxEntry = entry;
                }
            }

            return maxEntry.getKey();

        } else {
            return lastFolder;
        }
    }

    private void prepareOutputDirectory() throws ExtractionException {
        outputDirectoryFile = new File(outputDirectory);
        if (!outputDirectoryFile.exists()) {
            outputDirectoryFile.mkdirs();
        } else {
            if (outputDirectoryFile.list().length != 0) {
                throw new ExtractionException("Output directory not empty: "
                        + outputDirectory);
            }
        }
    }

    private void checkArchiveFile() throws ExtractionException {
        if (!new File(archive).exists()) {
            throw new ExtractionException("Archive file not found: " + archive);
        }
        if (!new File(archive).canRead()) {
            System.out.println("Can't read archive file: " + archive);
        }
    }

    public void extractArchive() throws ExtractionException {
        RandomAccessFile randomAccessFile;
        boolean ok = false;
        try {
            randomAccessFile = new RandomAccessFile(archive, "r");
        } catch (FileNotFoundException e) {
            throw new ExtractionException("File not found", e);
        }
        try {
            extractArchive(randomAccessFile);
            ok = true;
        } finally {
            try {
                randomAccessFile.close();
            } catch (Exception e) {
                if (ok) {
                    throw new ExtractionException("Error closing archive file",
                            e);
                }
            }
        }
    }

    private void extractArchive(RandomAccessFile file)
            throws ExtractionException {
        IInArchive inArchive;
        boolean ok = false;
        try {
            inArchive = SevenZip.openInArchive(null,
                    new RandomAccessFileInStream(file));
        } catch (SevenZipException e) {
            throw new ExtractionException("Error opening archive", e);
        }
        try {
            String root = getPackRoot(inArchive);
            if(root==null)
                throw new ExtractionException("No root found!");

            inArchive.extract(null, false, new ExtractCallback(inArchive, root));
            ok = true;
        } catch (SevenZipException e) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Error extracting archive '");
            stringBuilder.append(archive);
            stringBuilder.append("': ");
            stringBuilder.append(e.getMessage());
            if (e.getCause() != null) {
                stringBuilder.append(" (");
                stringBuilder.append(e.getCause().getMessage());
                stringBuilder.append(')');
            }
            String message = stringBuilder.toString();

            throw new ExtractionException(message, e);
        } finally {
            try {
                inArchive.close();
            } catch (SevenZipException e) {
                if (ok) {
                    throw new ExtractionException("Error closing archive", e);
                }
            }
        }
    }
}
