package org.coodex.file.repository.alioss;

/**
 * Ali OSS configuration
 */
public class AliOssConfig {
    private String endpoint;
    private String accessKeyId;
    private String accessKeySecret;
    private String bucketName;

    public AliOssConfig build(String endpoint, String accessKeyId, String accessKeySecret, String bucketName) {
        AliOssConfig config = new AliOssConfig();
        config.endpoint = endpoint;
        config.accessKeyId = accessKeyId;
        config.accessKeySecret = accessKeySecret;
        config.bucketName = bucketName;
        return config;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getAccessKeyId() {
        return accessKeyId;
    }

    public void setAccessKeyId(String accessKeyId) {
        this.accessKeyId = accessKeyId;
    }

    public String getAccessKeySecret() {
        return accessKeySecret;
    }

    public void setAccessKeySecret(String accessKeySecret) {
        this.accessKeySecret = accessKeySecret;
    }

    public String getBucketName() {
        return bucketName;
    }

    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }
}
