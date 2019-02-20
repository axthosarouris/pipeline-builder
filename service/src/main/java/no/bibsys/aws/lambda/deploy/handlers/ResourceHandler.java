package no.bibsys.aws.lambda.deploy.handlers;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.apigateway.AmazonApiGateway;
import com.amazonaws.services.apigateway.AmazonApiGatewayClientBuilder;
import com.amazonaws.services.cloudformation.AmazonCloudFormation;
import com.amazonaws.services.cloudformation.AmazonCloudFormationClientBuilder;
import no.bibsys.aws.cloudformation.Stage;
import no.bibsys.aws.git.github.BranchInfo;
import no.bibsys.aws.lambda.EnvironmentConstants;
import no.bibsys.aws.lambda.handlers.templates.CodePipelineCommunicator;
import no.bibsys.aws.lambda.handlers.templates.CodePipelineFunctionHandlerTemplate;
import no.bibsys.aws.lambda.responses.SimpleResponse;
import no.bibsys.aws.route53.StaticUrlInfo;
import no.bibsys.aws.secrets.AwsSecretsReader;
import no.bibsys.aws.secrets.SecretsReader;
import no.bibsys.aws.swaggerhub.SwaggerHubInfo;
import no.bibsys.aws.tools.Environment;

public abstract class ResourceHandler extends CodePipelineFunctionHandlerTemplate<SimpleResponse> {

    private final transient String swagerApiId;
    private final transient String swagerApiVersion;
    private final transient String swagerApiOwner;
    protected final transient Stage stage;
    protected final transient String stackName;
    private final transient String zoneName;

    private final transient String applicationUrl;
    protected final transient String branch;

    protected final transient Environment environment;
    protected final transient AmazonCloudFormation cloudFormationClient;
    protected final transient AmazonApiGateway apiGatewayClient;
    protected final transient SecretsReader swaggerHubSecretsReader;

    public ResourceHandler(Environment environment,
        CodePipelineCommunicator codePipelineCommunicator) {
        super(codePipelineCommunicator);
        this.environment = environment;

        this.branch = environment.readEnv(EnvironmentConstants.BRANCH);
        this.applicationUrl = environment.readEnv(EnvironmentConstants.APPLICATION_URL);

        this.swagerApiId = environment.readEnv(EnvironmentConstants.SWAGGER_API_ID);
        this.swagerApiVersion = environment.readEnv(EnvironmentConstants.SWAGGER_API_VERSION);
        this.swagerApiOwner = environment.readEnv(EnvironmentConstants.SWAGGER_API_OWNER);

        this.zoneName = environment.readEnv(EnvironmentConstants.ZONE_NAME_ENV);
        this.stackName = environment.readEnv(EnvironmentConstants.STACK_NAME);

        this.stage = Stage.fromString(environment.readEnv(EnvironmentConstants.STAGE));

        this.cloudFormationClient = AmazonCloudFormationClientBuilder.defaultClient();
        this.apiGatewayClient = AmazonApiGatewayClientBuilder.defaultClient();

        String swaggerHubApiKeySecretsName = environment
            .readEnv(EnvironmentConstants.ACCESS_SWAGGERHUB_SECRET_NAME);
        String swaggerHubApiKeySecretsKey = environment
            .readEnv(EnvironmentConstants.ACCESS_SWAGGERHUB_SECRET_KEY);
        Region region = Region
            .getRegion(Regions.fromName(environment.readEnv(EnvironmentConstants.AWS_REGION)));
        this.swaggerHubSecretsReader = new AwsSecretsReader(swaggerHubApiKeySecretsName,
            swaggerHubApiKeySecretsKey, region);
    }

    protected SwaggerHubInfo initializeSwaggerHubInfo() {

        return new SwaggerHubInfo(swagerApiId, swagerApiVersion, swagerApiOwner,
            swaggerHubSecretsReader);
    }

    protected StaticUrlInfo initializeStaticUrlInfo() {
        return new StaticUrlInfo(zoneName, applicationUrl, stage);
    }

    protected BranchInfo initalizeBranchInfo() {
        BranchInfo branchInfo = new BranchInfo();
        branchInfo.setBranch(branch);
        return branchInfo;
    }
}
