package com.fantasy.football.englishpremierleague.factories.clients;

import com.amazonaws.auth.EnvironmentVariableCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.sns.AmazonSNSAsync;
import com.amazonaws.services.sns.AmazonSNSAsyncClientBuilder;
import com.fantasy.football.englishpremierleague.datasource.FantasyFootballDataSource;
import com.fantasy.football.englishpremierleague.factories.clients.model.LeagueSummary;
import com.google.gson.Gson;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.ws.rs.core.MediaType;

@Configuration
public class ClientFactory {

    @Bean()
    public FantasyFootballDataSource createFantasyFootballDataSource() {
        String host = "https://fantasy.premierleague.com/api/";
        Client client = Client.create();
        Gson gson = new Gson();

        return new FantasyFootballDataSource() {
            @Override
            public LeagueSummary retrieveLeagueDetails() {
                ClientResponse response =
                        client.resource(host).path("bootstrap-static/").accept(MediaType.APPLICATION_JSON_TYPE).get(ClientResponse.class);
                System.out.println(response.getStatus());

                if (response.getStatus() == 200) {
                    return gson.fromJson(response.getEntity(String.class), LeagueSummary.class);
                }

                System.err.printf("The remote interface responded with code: %d\n", response.getStatus());
                throw new RuntimeException(String.format("The remote interface responded with code: %d\n", response.getStatus()));
            }
        };
    }

    @Bean()
    @Profile("!dev")
    public DynamoDB createDynamoDbClient() {
        AmazonDynamoDB client =
                AmazonDynamoDBClientBuilder.standard()
                        // .withCredentials(new EnvironmentVariableCredentialsProvider())
                        .withRegion(Regions.US_EAST_2)
                        .build();

        return new DynamoDB(client);
    }

    @Bean()
    @Profile("!dev")
    public AmazonSNSAsync createSnsClient() {
        return AmazonSNSAsyncClientBuilder.standard()
                //.withCredentials(new EnvironmentVariableCredentialsProvider())
                .withRegion(Regions.US_EAST_2)
                .build();
    }

    @Bean()
    @Profile("!dev")
    public AmazonS3 createStorageClient() {
        return AmazonS3ClientBuilder.standard()
                .withRegion(Regions.US_EAST_2)
                .build();
    }

    @Bean()
    @Profile("dev")
    public AmazonSNSAsync createSnsClientInDevEnvironment() {
        return AmazonSNSAsyncClientBuilder.standard()
                .withCredentials(new EnvironmentVariableCredentialsProvider())
                .withRegion(Regions.US_EAST_2)
                .build();
    }

    @Bean()
    @Profile("dev")
    public DynamoDB createDynamoDbClientInDevEnvironment() {
        AmazonDynamoDB client =
                AmazonDynamoDBClientBuilder.standard()
                        .withCredentials(new EnvironmentVariableCredentialsProvider())
                        .withRegion(Regions.US_EAST_2)
                        .build();

        return new DynamoDB(client);
    }

    @Bean()
    @Profile("dev")
    public AmazonS3 createStorageClientInDevEnvironment() {
        return AmazonS3ClientBuilder.standard()
                .withCredentials(new EnvironmentVariableCredentialsProvider())
                .withRegion(Regions.US_EAST_2)
                .build();
    }
}
