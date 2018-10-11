package no.bibsys.cloudformation;

import com.amazonaws.services.s3.model.Region;
import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.AWSSecretsManagerClientBuilder;
import com.amazonaws.services.secretsmanager.model.GetSecretValueRequest;
import com.amazonaws.services.secretsmanager.model.GetSecretValueResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Optional;
import no.bibsys.utils.EnvUtils;

public class GithubConf extends EnvUtils {

    private final String owner;
    private final String repo;

    private  String oauth;


    public GithubConf(String owner, String repo) throws IOException {

        this.owner = initOwner(owner);
        this.repo = initRepo(repo);
    }

    public void setOAuth() throws IOException {
        this.oauth = initOAuth();
    }

    private String initRepo(String repo) {
        return repo;
    }

    private String initOwner(String owner) {
        return owner;
    }

    private String initOAuth() throws IOException {
        Optional<String> env = readEnvOpt("GITHUBAUTH");
        if (env.isPresent()) {
            return env.get();
        }
        AWSSecretsManager client = AWSSecretsManagerClientBuilder.standard()
            .withRegion(Region.EU_Ireland.toString())
            .build();
        return readAuthFromSecrets(client);
    }


    public String getOwner() {
        return owner;
    }

    public String getRepo() {
        return repo;
    }

    public String getOauth() {
        return oauth;
    }

    private String readAuthFromSecrets(AWSSecretsManager client) throws IOException {
        ObjectMapper mapper=new ObjectMapper();

        GetSecretValueRequest getSecretValueRequest = new GetSecretValueRequest()
            .withSecretId("githubauth");
        GetSecretValueResult getSecretValueResult = client
            .getSecretValue(getSecretValueRequest);

        if (getSecretValueResult.getSecretString() != null) {
            String secret = getSecretValueResult.getSecretString();
            String value= mapper.readTree(secret)
                .findValuesAsText("githubauth").stream().findFirst().orElse(null);
            return value;
        }
        return null;
    }


}
