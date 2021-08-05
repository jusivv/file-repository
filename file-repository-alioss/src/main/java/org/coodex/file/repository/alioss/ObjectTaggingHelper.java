package org.coodex.file.repository.alioss;

import com.aliyun.oss.model.TagSet;
import org.coodex.filerepository.api.FileMetaInf;
import org.coodex.filerepository.api.StoredFileMetaInf;
import org.coodex.util.Common;
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
    public static String OBJECT_TAGGING_STORE_TIME = "OBJECT_TAGGING_STORE_TIME";
    public static String OBJECT_TAGGING_HASH_VALUE = "OBJECT_TAGGING_HASH_VALUE";
    public static String OBJECT_TAGGING_HASH_ALGORITHM = "OBJECT_TAGGING_HASH_ALGORITHM";

    public static Map<String, String> buildTags(FileMetaInf fileMetaInf) {
        Map<String, String> tags = new HashMap<>();
        tags.put(OBJECT_TAGGING_FILE_NAME, fileMetaInf.getFileName());
        tags.put(OBJECT_TAGGING_EXT_NAME, fileMetaInf.getExtName());
        tags.put(OBJECT_TAGGING_FILE_SIZE, String.valueOf(fileMetaInf.getFileSize()));
        tags.put(OBJECT_TAGGING_CLIENT_ID, fileMetaInf.getClientId());
        if (fileMetaInf instanceof StoredFileMetaInf) {
            StoredFileMetaInf storedFileMetaInf = (StoredFileMetaInf) fileMetaInf;
            tags.put(OBJECT_TAGGING_STORE_TIME, String.valueOf(storedFileMetaInf.getStoreTime()));
            tags.put(OBJECT_TAGGING_HASH_VALUE,
                    Common.isBlank(storedFileMetaInf.getHashValue()) ? "N/A" : storedFileMetaInf.getHashValue());
            tags.put(OBJECT_TAGGING_HASH_ALGORITHM,
                    Common.isBlank(storedFileMetaInf.getHashAlgorithm()) ? "N/A" : storedFileMetaInf.getHashAlgorithm());
        }
        return tags;
    }

    public static StoredFileMetaInf parseFileMetaInf(TagSet tagSet) {
        StoredFileMetaInf storedFileMetaInf = new StoredFileMetaInf();
        storedFileMetaInf.setFileName(getTaggingFileName(tagSet));
        storedFileMetaInf.setExtName(getTaggingExtName(tagSet));
        storedFileMetaInf.setClientId(getTaggingClientId(tagSet));
        storedFileMetaInf.setFileSize(getTaggingFileSize(tagSet));
        storedFileMetaInf.setStoreTime(getTaggingStoreTime(tagSet));
        storedFileMetaInf.setHashAlgorithm(getTaggingHashAlgorithm(tagSet));
        storedFileMetaInf.setHashValue(getTaggingHashValue(tagSet));
        return storedFileMetaInf;
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

    public static long getTaggingStoreTime(TagSet tagSet) {
        String storeTime = getTaggingValue(tagSet, OBJECT_TAGGING_STORE_TIME);
        try {
            return Long.parseLong(storeTime);
        } catch (NumberFormatException e) {
            log.warn(e.getLocalizedMessage(), e);
            return 0L;
        }
    }

    public static String getTaggingHashAlgorithm(TagSet tagSet) {
        return getTaggingValue(tagSet, OBJECT_TAGGING_HASH_ALGORITHM);
    }

    public static String getTaggingHashValue(TagSet tagSet) {
        return getTaggingValue(tagSet, OBJECT_TAGGING_HASH_VALUE);
    }

}
