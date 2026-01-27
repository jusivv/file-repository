package org.coodex.file.repository.s3;

import com.alibaba.fastjson.JSON;
import de.huxhorn.sulky.ulid.ULID;
import org.coodex.filerepository.api.AbstractFileRepository;
import org.coodex.filerepository.api.FileMetaInf;
import org.coodex.filerepository.api.RepositoryNotifyCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.async.AsyncRequestBody;
import software.amazon.awssdk.core.async.AsyncResponseTransformer;
import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration;
import software.amazon.awssdk.core.retry.RetryMode;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.utils.StringUtils;
import software.amazon.awssdk.utils.Validate;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class S3FileRepository extends AbstractFileRepository implements AutoCloseable {
    private static final String FILE_META_INFO = "x-file-meta-info";
    private static final ULID ULID_GENERATOR = new ULID();
    private static final Logger log = LoggerFactory.getLogger(S3FileRepository.class);
    private S3AsyncClient s3AsyncClient;
    private String bucketName;
    private ExecutorService executor;

    public S3FileRepository(String endpointUrl, String accessKeyId, String secretAccessKey, String bucketName) {
        createAsyncClient(endpointUrl, accessKeyId, secretAccessKey, bucketName);
    }

    private void createAsyncClient(String endpointUrl, String accessKeyId, String secretAccessKey, String bucketName) {
        ClientOverrideConfiguration configuration = ClientOverrideConfiguration.builder()
            .apiCallTimeout(Duration.ofMinutes(2))
            .apiCallAttemptTimeout(Duration.ofSeconds(90))
            .retryStrategy(RetryMode.STANDARD)
            .build();
        s3AsyncClient = S3AsyncClient.builder().endpointOverride(URI.create(endpointUrl)).region(Region.AP_EAST_2)
            .credentialsProvider(StaticCredentialsProvider.create(
                AwsBasicCredentials.builder().accessKeyId(accessKeyId).secretAccessKey(secretAccessKey)
                    .build()
            )).overrideConfiguration(configuration)
            .serviceConfiguration(builder -> builder.pathStyleAccessEnabled(true).build())
            .multipartEnabled(true)
            .build();
        this.bucketName = createBucket(s3AsyncClient, bucketName);
        executor = Executors.newCachedThreadPool();
    }

    private String createBucket(S3AsyncClient s3AsyncClient, String bucketName) {
        Validate.notBlank(bucketName, "Illegal bucket name: %s", bucketName);
        for (Bucket bucket : s3AsyncClient.listBuckets().join().buckets()) {
            String name = bucket.name();
            if (bucketName.equals(name)) {
                log.info("use bucket: {}", name);
                return name;
            }
        }
        s3AsyncClient.createBucket(CreateBucketRequest.builder().bucket(bucketName).build()).join();
        log.info("create new bucket: {}", bucketName);
        return bucketName;
    }

    @Override
    protected String generateFileId(String clientId) {
        return (StringUtils.isBlank(clientId) ? "" : clientId + "$") + ULID_GENERATOR.nextULID();
    }

    @Override
    protected <T extends FileMetaInf> void saveFile(String fileId, InputStream inputStream, T fileMetaInf) throws Throwable {
        Map<String, String> metaData = new HashMap<>();
        metaData.put(FILE_META_INFO, JSON.toJSONString(fileMetaInf));
        log.debug("save file, fileId: {}, bucket name: {}", fileId, bucketName);
        s3AsyncClient.putObject(
            PutObjectRequest.builder()
                .bucket(bucketName)
                .key(fileId)
                .metadata(metaData)
                .build(),
            AsyncRequestBody.fromInputStream(inputStream, null, executor)
        ).join();
    }

    @Override
    public void get(String fileId, long offset, int length, OutputStream outputStream) throws Throwable {
        CompletableFuture<ResponseInputStream<GetObjectResponse>> responseFuture = s3AsyncClient.getObject(
            GetObjectRequest.builder().bucket(bucketName).key(fileId).build(),
            AsyncResponseTransformer.toBlockingInputStream()
        );
        byte[] buff = new byte[4 * 1024];
        try (ResponseInputStream<GetObjectResponse> responseStream = responseFuture.join()) {
            long skiped = responseStream.skip(offset);
            if (skiped < offset) {
                throw new RuntimeException("No data remained after skipping offset " + offset);
            }
            int remained = length, len;
            if (length == 0) {
                while ((len = responseStream.read(buff)) != -1) {
                    outputStream.write(buff, 0, len);
                }
            } else {
                while (remained > 0 && (len = responseStream.read(buff)) != -1) {
                    outputStream.write(buff, 0, Math.min(len, remained));
                    remained -= len;
                }
            }
            outputStream.flush();
        }
    }

    @Override
    public void delete(String fileId) throws Throwable {
        s3AsyncClient.deleteObject(
            DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(fileId)
                .build()
        ).join();
    }

    @Override
    public <T extends FileMetaInf> T getMetaInf(String fileId, Class<T> clazz) throws Throwable {
        Map<String, String> metaData  = s3AsyncClient.headObject(
            HeadObjectRequest.builder()
                .bucket(bucketName)
                .key(fileId)
                .build()
        ).join().metadata();
        String metaStr = metaData.get(FILE_META_INFO);
        if (!StringUtils.isBlank(metaStr)) {
            return JSON.parseObject(metaData.get(FILE_META_INFO), clazz);
        } else {
            return null;
        }
    }

    @Override
    public <T extends FileMetaInf> String asyncSave(InputStream inputStream, T fileMetaInf, RepositoryNotifyCallback notifyCallback) {
        String fileId = generateFileId(fileMetaInf.getClientId());
        Map<String, String> metaData = new HashMap<>();
        metaData.put(FILE_META_INFO, JSON.toJSONString(fileMetaInf));
        s3AsyncClient.putObject(
            PutObjectRequest.builder()
                .bucket(bucketName)
                .key(fileId)
                .contentLength(fileMetaInf.getFileSize())
                .metadata(metaData)
                .build(),
            AsyncRequestBody.fromInputStream(inputStream, null, executor)
        ).whenComplete((putObjectResponse, throwable) -> {
            if (throwable == null) {
                notifyCallback.complete(true, fileId, null);
            } else {
                notifyCallback.complete(false, null, throwable);
            }
        });
        return fileId;
    }

    @Override
    public void asyncDelete(String fileId, RepositoryNotifyCallback notifyCallback) {
        s3AsyncClient.deleteObject(
            DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(fileId)
                .build()
        ).whenComplete((deleteObjectResponse, throwable) -> {
            if (throwable == null) {
                notifyCallback.complete(true, fileId, null);
            } else {
                notifyCallback.complete(false, fileId, throwable);
            }
        });
    }

    public void shutdown() {
        if (s3AsyncClient != null) {
            s3AsyncClient.close();
        }
        if (executor != null) {
            executor.shutdown();
        }
    }

    @Override
    public void close() throws Exception {
        shutdown();
    }

    @Override
    protected void finalize() throws Throwable {
        this.close();
    }
}
