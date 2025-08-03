package com.faforever.api.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.net.URI;

@Configuration
@RequiredArgsConstructor
public class S3Config {

  private final FafApiProperties properties;

  @Bean
  public S3Client s3Client() {
    return S3Client.builder()
      .endpointOverride(URI.create(properties.getS3().getEndpoint()))
      .region(Region.EU_CENTRAL_1) // region must be non-null but is ignored by some S3-compatible services
      .credentialsProvider(StaticCredentialsProvider.create(
        AwsBasicCredentials.create(properties.getS3().getAccessKey(), properties.getS3().getSecretKey())
      ))
      .serviceConfiguration(S3Configuration.builder()
        .pathStyleAccessEnabled(true) // prevents putting the bucket name as subdomain
        .build())
      .build();
  }

  @Bean
  public S3Presigner s3Presigner() {
    return S3Presigner.builder()
      .endpointOverride(URI.create(properties.getS3().getEndpoint()))
      // Cloudflare has different regions as AWS: wnam, enam, weur, eeur, apac, oc, auto
      .region(Region.of(properties.getS3().getRegion())) // region must be non-null but is ignored by some S3-compatible services
      .credentialsProvider(StaticCredentialsProvider.create(
        AwsBasicCredentials.create(properties.getS3().getAccessKey(), properties.getS3().getSecretKey())
      ))
      .serviceConfiguration(S3Configuration.builder()
        .pathStyleAccessEnabled(true) // prevents putting the bucket name as subdomain
        .build())
      .build();
  }
}
