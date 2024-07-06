package com.project.legendsofleague.util;


import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.project.legendsofleague.domain.item.domain.ItemImage;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class S3Util {

  private final AmazonS3 amazonS3;

  @Value("${cloud.aws.s3.bucketName}")
  private String bucket;


  public List<ItemImage> saveFiles(List<String> images) {
    List<ItemImage> itemImages = new ArrayList<>();
    for (String image : images) {
      if (!image.isEmpty()) {
        itemImages.add(saveFile(image));
      }
    }
    return itemImages;
  }

  public ItemImage saveFile(String image) {
    String[] encodeImage = image.split(","); //data:image/jpeg;base64, 분리해줘야 함
    byte[] imageData = Base64.getDecoder().decode(encodeImage[1]);
    String savedFileName = createStoreFileName(encodeImage[0]);
    ObjectMetadata metadata = new ObjectMetadata();
    metadata.setContentLength(imageData.length);
    amazonS3.putObject(bucket, savedFileName, new ByteArrayInputStream(imageData),
        metadata);
    return new ItemImage(amazonS3.getUrl(bucket, savedFileName).toString());
  }

  private String createStoreFileName(String data) {
    String ext = extractExt(data);
    String uuid = UUID.randomUUID().toString();

    return uuid + "." + ext;
  }

  private String extractExt(String data) {
    String[] split = data.split("/");
    String[] ext = split[1].split(";");  // ext;base64,
    return ext[0];
  }
}
