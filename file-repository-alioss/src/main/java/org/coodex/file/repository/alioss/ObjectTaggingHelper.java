package org.coodex.file.repository.alioss;

import com.aliyun.oss.model.TagSet;
import org.coodex.filerepository.api.FileMetaInf;
import org.coodex.filerepository.api.StoredFileMetaInf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class ObjectTaggingHelper {
    private static Logger log = LoggerFactory.getLogger(ObjectTaggingHelper.class);

    public static String OBJECT_TAGGING_FILE_NAME = "OBJECT_TAGGING_FILE_NAME";
    public static String OBJECT_TAGGING_EXT_NAME = "OBJECT_TAGGING_EXT_NAME";
    public static String OBJECT_TAGGING_FILE_SIZE = "OBJECT_TAGGING_FILE_SIZE";
    public static String OBJECT_TAGGING_CLIENT_ID = "OBJECT_TAGGING_CLIENT_ID";

    public static Map<String, String> buildTags(FileMetaInf fileMetaInf) {
        Map<String, String> tags = new HashMap<>();
        tags.put(OBJECT_TAGGING_FILE_NAME, fileMetaInf.getFileName());
        tags.put(OBJECT_TAGGING_EXT_NAME, fileMetaInf.getExtName());
        tags.put(OBJECT_TAGGING_FILE_SIZE, String.valueOf(fileMetaInf.getFileSize()));
        tags.put(OBJECT_TAGGING_CLIENT_ID, fileMetaInf.getClientId());
        return tags;
    }

    public static FileMetaInf parseFileMetaInf(TagSet tagSet) {
        FileMetaInf fileMetaInf = new StoredFileMetaInf();
        fileMetaInf.setFileName(getTaggingFileName(tagSet));
        fileMetaInf.setExtName(getTaggingExtName(tagSet));
        fileMetaInf.setClientId(getTaggingClientId(tagSet));
        fileMetaInf.setFileSize(getTaggingFileSize(tagSet));
        return fileMetaInf;
    }

    public static String getTaggingValue(TagSet tagSet, String key) {
        return tagSet.getTag(key);
    }

    public static String getTaggingFileName(TagSet tagSet) {
        return getTaggingValue(tagSet, OBJECT_TAGGING_FILE_NAME);
    }

    public static String getTaggingExtName(TagSet tagSet) {
        return getTaggingValue(tagSet, OBJECT_TAGGING_EXT_NAME);
    }

    public static long getTaggingFileSize(TagSet tagSet) {
        String fileSize = getTaggingValue(tagSet, OBJECT_TAGGING_FILE_SIZE);
        try {
            return Long.parseLong(fileSize);
        } catch (NumberFormatException e) {
            log.warn(e.getLocalizedMessage(), e);
            return 0L;
        }
    }

    public static String getTaggingClientId(TagSet tagSet) {
        return getTaggingValue(tagSet, OBJECT_TAGGING_CLIENT_ID);
    }
}
