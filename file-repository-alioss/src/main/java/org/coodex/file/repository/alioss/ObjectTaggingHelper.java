package org.coodex.file.repository.alioss;

import com.aliyun.oss.model.TagSet;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.PropertyUtils;
import org.coodex.filerepository.api.FileMetaInf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

public class ObjectTaggingHelper {
    private static Logger log = LoggerFactory.getLogger(ObjectTaggingHelper.class);

    public static <T extends FileMetaInf> Map<String, String> toTags(T fileMetaInf) {
        Map<String, String> tags = new HashMap<>();
        for (PropertyDescriptor propertyDescriptor : PropertyUtils.getPropertyDescriptors(fileMetaInf)) {
            String key = propertyDescriptor.getName();
            try {
                String value = BeanUtils.getProperty(fileMetaInf, key);
                tags.put(key, value);
            } catch (IllegalAccessException e) {
                log.warn(e.getLocalizedMessage(), e);
            } catch (InvocationTargetException e) {
                log.warn(e.getLocalizedMessage(), e);
            } catch (NoSuchMethodException e) {
                log.warn(e.getLocalizedMessage(), e);
            }
        }
        return tags;
    }

    public static <T extends FileMetaInf> T parseTags(TagSet tagSet, Class<T> clazz) {
        try {
            T fileMetaInf = clazz.getConstructor().newInstance();
            tagSet.getAllTags().forEach((key, value) -> {
                try {
                    BeanUtils.setProperty(fileMetaInf, key, value);
                } catch (IllegalAccessException e) {
                } catch (InvocationTargetException e) {
                }
            });
            return fileMetaInf;
        } catch (InstantiationException e) {
            log.warn(e.getLocalizedMessage(), e);
        } catch (IllegalAccessException e) {
            log.warn(e.getLocalizedMessage(), e);
        } catch (InvocationTargetException e) {
            log.warn(e.getLocalizedMessage(), e);
        } catch (NoSuchMethodException e) {
            log.warn(e.getLocalizedMessage(), e);
        }
        return null;
    }

}
