package no.bibsys.aws;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.cloudformation.AmazonCloudFormation;
import com.amazonaws.services.cloudformation.AmazonCloudFormationClientBuilder;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagement;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagementClientBuilder;
import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.lambda.AWSLambdaClientBuilder;
import com.amazonaws.services.logs.AWSLogs;
import com.amazonaws.services.logs.AWSLogsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.google.common.base.Preconditions;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import no.bibsys.aws.cloudformation.PipelineStackConfiguration;
import no.bibsys.aws.git.github.GithubConf;
import no.bibsys.aws.lambda.api.utils.Action;
import no.bibsys.aws.secrets.AwsSecretsReader;
import no.bibsys.aws.secrets.SecretsReader;
import no.bibsys.aws.utils.github.GithubReader;
import no.bibsys.aws.utils.stacks.StackBuilder;
import no.bibsys.aws.utils.stacks.StackWiper;
import no.bibsys.aws.utils.stacks.StackWiperImpl;
import org.apache.http.impl.client.HttpClients;

public class Application {

    private static final String AWS_REGION = "awsRegion";
    private static final String GITHUB_OWNER_PROPERTY = "owner";
    private static final String GITHUB_REPOSITORY_PROPERTY = "repository";
    private static final String GIT_BRANCH_PROPERTY = "branch";
    private static final String CODEPIEPINE_ACTION = "action";
    private static final String ABSENT_OWNER_ERROR_MEESSAGE = "System property \"owner\" is not set";
    private static final String ABSENT_REPOSITORY_MESSAGE = "System property \"repository\" is not set";
    private static final String ABSENT_BRANCH_ERROR_MESSAGE = "System property \"branch\" is not set";
    private static final String VALID_VALUES_FOR_ACTION_MESSAGE = "Valid values: create,delete";
    private static final String INVALID_ACTION_VALUE_MESSAGE = "System property \"action\" is not set\n";
    private static final String ABSENT_ACTION_VALUE_MESSAGE1 = INVALID_ACTION_VALUE_MESSAGE;
    private static final String CONFIGURATION_GITHUB_SECRET_NAME = "github.read_from_github_secret_name";
    private static final String CONFIGURATION_GITHUB_SECRET_KEY = "github.read_from_github_secret_key";
    private final transient StackWiper wiper;

    private final transient String repoName;
    private final transient String branch;

    private final transient PipelineStackConfiguration pipelineStackConfiguration;


    public Application(GithubConf gitInfo,
        AmazonCloudFormation acf,
        AmazonS3 s3Client,
        AWSLambda lambdaClient,
        AWSLogs logsClient,
        AmazonIdentityManagement amazonIdentityManagement) {
        this.pipelineStackConfiguration = new PipelineStackConfiguration(gitInfo);
        this.repoName = gitInfo.getRepository();
        this.branch = gitInfo.getBranch();

        wiper = new StackWiperImpl(pipelineStackConfiguration, acf, s3Client, lambdaClient, logsClient,
            amazonIdentityManagement);
        checkNulls();
    }

    private static void run(String repoOwner,
        String repository,
        String branch,
        String action,
        SecretsReader secretsReader
    )
        throws Exception {

        GithubConf gitInfo = new GithubConf(repoOwner, repository, branch, secretsReader);
        GithubReader githubReader = new GithubReader(HttpClients.createMinimal())
            .setGitHubConf(gitInfo);

        AmazonCloudFormation cloudFormation = AmazonCloudFormationClientBuilder.defaultClient();
        AmazonS3 s3Client = AmazonS3ClientBuilder.defaultClient();
        AWSLambda lambdaClient = AWSLambdaClientBuilder.defaultClient();
        AWSLogs logsClient = AWSLogsClientBuilder.defaultClient();
        AmazonIdentityManagement amazonIdentityManagement = AmazonIdentityManagementClientBuilder.defaultClient();


        Application application = new Application(gitInfo, cloudFormation, s3Client, lambdaClient,
            logsClient, amazonIdentityManagement);
        if (Action.CREATE.equals(Action.fromString(action))) {
            application
                .createStacks(cloudFormation, AmazonIdentityManagementClientBuilder.defaultClient(),
                    githubReader);
        } else if (Action.DELETE.equals(Action.fromString(action))) {
            application.wipeStacks();
        }
    }

    @SuppressWarnings("PMD")
    public static void main(String... args) throws Exception {

        Config config = ConfigFactory.defaultReference().resolve();
        final String readFromGithubSecretName = config.getString(CONFIGURATION_GITHUB_SECRET_NAME);
        final String readFromGithubSecretKey = config.getString(CONFIGURATION_GITHUB_SECRET_KEY);

        String repoOwner = System.getProperty(GITHUB_OWNER_PROPERTY);
        Preconditions.checkNotNull(repoOwner, ABSENT_OWNER_ERROR_MEESSAGE);
        String repository = System.getProperty(GITHUB_REPOSITORY_PROPERTY);
        Preconditions.checkNotNull(repository, ABSENT_REPOSITORY_MESSAGE);
        String branch = System.getProperty(GIT_BRANCH_PROPERTY);
        Preconditions.checkNotNull(branch, ABSENT_BRANCH_ERROR_MESSAGE);
        String action = System.getProperty(CODEPIEPINE_ACTION);
        String awsRegion = System.getProperty(AWS_REGION);
        String message = ABSENT_ACTION_VALUE_MESSAGE1 + VALID_VALUES_FOR_ACTION_MESSAGE;
        Preconditions.checkNotNull(action, message);

        Region region = Region.getRegion(Regions.fromName(awsRegion));

        System.out.println(String.format("Secrets key: %s - Secrets name: %s", readFromGithubSecretKey, readFromGithubSecretName));
        SecretsReader secretsReader = new AwsSecretsReader(readFromGithubSecretName,
            readFromGithubSecretKey, region);
        Application.run(repoOwner, repository, branch, action, secretsReader);
    }

    public PipelineStackConfiguration getPipelineStackConfiguration() {
        return pipelineStackConfiguration;
    }

    public void createStacks(AmazonCloudFormation cloudFormation,
        AmazonIdentityManagement amazonIdentityManagement, GithubReader githubReader)
        throws Exception {
        StackBuilder stackBuilder = new StackBuilder(wiper,
            pipelineStackConfiguration,
            cloudFormation,
            amazonIdentityManagement,
            githubReader
        );
        stackBuilder.createStacks();
    }

    public void wipeStacks() {
        checkNulls();
        wiper.wipeStacks();
    }

    private void checkNulls() {
        Preconditions.checkNotNull(repoName);
        Preconditions.checkNotNull(branch);
    }
}
