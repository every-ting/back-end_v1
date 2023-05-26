package com.ting.ting.util;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.DeleteObjectsRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.util.List;

@RequiredArgsConstructor
@Service
public class S3StorageManager {

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

    private final AmazonS3 amazonS3;

    public String uploadByteArrayToS3WithKey(byte[] imageBytes, String key) {
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType("image/png");
        metadata.setContentLength(imageBytes.length);

        ByteArrayInputStream inputStream = new ByteArrayInputStream(imageBytes);

        amazonS3.putObject(bucketName, key, inputStream, metadata);

        return getImageUrlForKey(key);
    }

    public void deleteImageByKey(String key) throws AmazonS3Exception {
        amazonS3.deleteObject(bucketName, key);
    }

    public void deleteImagesByKeys(List<String> keys) throws AmazonS3Exception {
        if (!keys.isEmpty()) {
            DeleteObjectsRequest deleteObjectsRequest = new DeleteObjectsRequest(bucketName)
                    .withKeys(keys.toArray(new String[0]));

            amazonS3.deleteObjects(deleteObjectsRequest);
        }
    }

    public String getImageUrlForKey(String key){
        return amazonS3.getUrl(bucketName, key).toString();
    }
}
