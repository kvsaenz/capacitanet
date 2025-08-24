package co.com.capacitanet.aws.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.net.URI;

@Configuration
public class AWSConfig {

    @Bean
    public DynamoDbClient amazonDynamoDBLocal(@Value("${aws.region}") String region,
                                              @Value("${aws.dynamodb.endpoint}") String endpoint) {
        return DynamoDbClient.builder()
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(System.getenv("AWS_ACCESS_KEY_ID_DYNAMO"),
                                System.getenv("AWS_SECRET_ACCESS_KEY_DYNAMO"))
                ))
                .region(Region.of(region))
                .endpointOverride(URI.create(endpoint))
                .build();
    }

    @Bean
    public S3Client s3Client(@Value("${aws.region}") String region) {
        return S3Client.builder()
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(System.getenv("AWS_ACCESS_KEY_ID_S3"),
                                System.getenv("AWS_SECRET_ACCESS_KEY_S3"))
                ))
                .region(Region.of(region))
                .build();
    }

    @Bean
    public S3Presigner s3Presigner(@Value("${aws.region}") String region) {
        return S3Presigner.builder()
                .region(Region.US_EAST_1)
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(System.getenv("AWS_ACCESS_KEY_ID_S3"),
                                System.getenv("AWS_SECRET_ACCESS_KEY_S3"))
                ))
                .build();
    }

}
