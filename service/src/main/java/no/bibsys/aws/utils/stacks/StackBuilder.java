package no.bibsys.aws.utils.stacks;

import com.amazonaws.services.cloudformation.AmazonCloudFormation;
import com.amazonaws.services.cloudformation.model.AmazonCloudFormationException;
import com.amazonaws.services.cloudformation.model.Capability;
import com.amazonaws.services.cloudformation.model.CreateStackRequest;
import com.amazonaws.services.cloudformation.model.Parameter;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import no.bibsys.aws.cloudformation.PipelineStackConfiguration;
import no.bibsys.aws.cloudformation.Stage;
import no.bibsys.aws.tools.IoUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StackBuilder {

    private static final Logger log = LoggerFactory.getLogger(StackBuilder.class);

    private static final String CLOUDFORMATION_TEMPLATE_PARAMETER_GITHUB_OWNER = "GithubOwner";
    private static final String CLOUD_FORMATION_TEMPLATE_PARAMETER_GITHUB_REPO = "GithubRepo";
    private static final String CLOUD_FORMATION_TEMPLATE_PARAMETER_GITHUB_AUTH = "GithubAuth";
    private static final String CLOUD_FORMATION_TEMPLATE_PARAMETER_PIPELINE_NAME = "PipelineName";
    private static final String CLOUD_FORMATION_TEMPLATE_PARAMETER_PIPELINE_BUCKETNAME =
            "PipelineBucketname";
    private static final String CLOUD_FORMATION_TEMPLATE_PARAMETER_PIPELINE_ROLENAME =
            "PipelineRolename";
    private static final String CLOUD_FORMATION_TEMPLATE_PARAMETER_CREATE_STACK_ROLENAME =
            "CreateStackRolename";
    private static final String CLOUD_FORMATION_TEMPLATE_PARAMETER_SOURCE_STAGE_OUTPUT_ARTIFACT =
            "SourceStageOutputArtifact";
    private static final String CLOUD_FORMATION_TEMPLATE_PARAMETER_PROJECT_ID = "ProjectId";
    private static final String CLOUD_FORMATION_TEMPLATE_PARAMETER_PROJECT_BRANCH = "ProjectBranch";
    private static final String CLOUD_FORMATION_TEMPLATE_PARAMETER_NORMALIZED_BRANCH_NAME = "NormalizedBranchName";
    private static final String CLOUD_FORMATION_TEMPLATE_PARAMETER_CODEBUILD_OUTPUT_ARTIFACT =
            "CodebuildOutputArtifact";
    private static final String CLOUD_FORMATION_TEMPLATE_PARAMETER_CODEBUILD_PROJECTNAME = "CodebuildProjectname";
    private static final String CLOUD_FORMATION_TEMPLATE_PARAMETER_EXECUTE_TESTS_PROJECTNAME =
            "ExecuteTestsProjectname";
    private static final String CLOUD_FORMATION_TEMPLATE_PARAMETER_PIPELINE_TEST_SERVICE_STACK_NAME =
            "PipelineTestServiceStackName";
    private static final String CLOUD_FORMATION_TEMPLATE_PARAMETER_PIPELINE_FINAL_SERVICE_STACK_NAME =
            "PipelineFinalServiceStackName";
    private static final String CLOUD_FORMATION_TEMPLATE_PARAMETER_INIT_FUNCTION_NAME = "InitFunctionName";
    private static final String CLOUD_FORMATION_TEMPLATE_PARAMETER_DESTROY_FUNCTION_NAME = "DestroyFunctionName";
    private static final String CLOUD_FORMATION_TEMPLATE_PARAMETER_TEST_PHASE_NAME = "TestPhaseName";
    private static final String CLOUD_FORMATION_TEMPLATE_PARAMETER_FINAL_PHASE_NAME = "FinalPhaseName";
    private static final String STACK_DOES_NOT_EXIST_WARNING = "Stack does not exist";
    public static final String TEMPLATES_RESOURCE_DIRECTORY = "templates";
    public static final String PIPELINE_TEMPLATE = "pipelineTemplate.yaml";

    private final transient StackWiper stackWiper;

    private final transient PipelineStackConfiguration pipelineStackConfiguration;
    private final transient AmazonCloudFormation cloudFormationClient;

    public StackBuilder(
            StackWiper wiper,
            PipelineStackConfiguration pipelineStackConfiguration,
            AmazonCloudFormation cloudFormationClient
    ) {
        this.cloudFormationClient = cloudFormationClient;
        this.stackWiper = wiper;
        this.pipelineStackConfiguration = pipelineStackConfiguration;
    }

    public void createStacks() throws IOException {
        try {
            stackWiper.wipeStacks();
        } catch (AmazonCloudFormationException e) {
            log.warn(STACK_DOES_NOT_EXIST_WARNING);
        }
        createPipelineStack(pipelineStackConfiguration);
    }

    private void createPipelineStack(PipelineStackConfiguration pipelineStackConfiguration)
            throws IOException {
        CreateStackRequest createStackRequest = createStackRequest(pipelineStackConfiguration);
        cloudFormationClient.createStack(createStackRequest);
    }

    private CreateStackRequest createStackRequest(PipelineStackConfiguration pipelineStackConfiguration)
            throws IOException {
        CreateStackRequest createStackRequest = new CreateStackRequest();
        setBasicStackRequestParameters(createStackRequest, pipelineStackConfiguration);
        setPipelineStackTemplate(createStackRequest);
        setTemplateParamaters(createStackRequest, pipelineStackConfiguration);

        return createStackRequest;
    }

    private void setBasicStackRequestParameters(CreateStackRequest createStackRequest,
                                                PipelineStackConfiguration pipelineStackConfiguration) {
        createStackRequest.setStackName(pipelineStackConfiguration.getPipelineStackName());
        createStackRequest.withCapabilities(Capability.CAPABILITY_NAMED_IAM);
    }

    private void setTemplateParamaters(CreateStackRequest createStackRequest, PipelineStackConfiguration pipelineStack)
            throws IOException {

        List<Parameter> parameters = new ArrayList<>();

        parameters.add(newParameter(
                CLOUDFORMATION_TEMPLATE_PARAMETER_GITHUB_OWNER,
                pipelineStack.getGithubConf().getOwner()));
        parameters.add(newParameter(
                CLOUD_FORMATION_TEMPLATE_PARAMETER_GITHUB_REPO,
                pipelineStack.getGithubConf().getRepository()));
        parameters.add(newParameter(
                CLOUD_FORMATION_TEMPLATE_PARAMETER_GITHUB_AUTH,
                pipelineStack.getGithubConf().getOauth()));

        parameters.add(newParameter(CLOUD_FORMATION_TEMPLATE_PARAMETER_PIPELINE_NAME,
                pipelineStack.getPipelineConfiguration().getPipelineName()));

        parameters.add(newParameter(CLOUD_FORMATION_TEMPLATE_PARAMETER_PIPELINE_BUCKETNAME,
                pipelineStack.getBucketName()));

        parameters.add(newParameter(CLOUD_FORMATION_TEMPLATE_PARAMETER_PIPELINE_ROLENAME,
                pipelineStack.getPipelineRoleName()));

        parameters.add(newParameter(CLOUD_FORMATION_TEMPLATE_PARAMETER_CREATE_STACK_ROLENAME,
                pipelineStack.getCreateStackRoleName()));

        parameters.add(newParameter(CLOUD_FORMATION_TEMPLATE_PARAMETER_SOURCE_STAGE_OUTPUT_ARTIFACT,
                pipelineStack.getPipelineConfiguration().getSourceOutputArtifactName()));

        parameters.add(newParameter(CLOUD_FORMATION_TEMPLATE_PARAMETER_PROJECT_ID,
                pipelineStack.getProjectId()));
        parameters.add(newParameter(CLOUD_FORMATION_TEMPLATE_PARAMETER_PROJECT_BRANCH,
                pipelineStack.getBranchName()));
        parameters
                .add(newParameter(CLOUD_FORMATION_TEMPLATE_PARAMETER_NORMALIZED_BRANCH_NAME,
                        pipelineStack.getNormalizedBranchName()));

        parameters.add(
                newParameter(CLOUD_FORMATION_TEMPLATE_PARAMETER_CODEBUILD_OUTPUT_ARTIFACT,
                        pipelineStack.getCodeBuildConfiguration().getOutputArtifact()));
        parameters.add(
                newParameter(CLOUD_FORMATION_TEMPLATE_PARAMETER_CODEBUILD_PROJECTNAME,
                        pipelineStack.getCodeBuildConfiguration().getBuildProjectName()));

        parameters.add(
                newParameter(CLOUD_FORMATION_TEMPLATE_PARAMETER_EXECUTE_TESTS_PROJECTNAME,
                        pipelineStack.getCodeBuildConfiguration().getExecuteTestsProjectName()));

        parameters.add(newParameter(
                CLOUD_FORMATION_TEMPLATE_PARAMETER_PIPELINE_TEST_SERVICE_STACK_NAME,
                pipelineStack.getPipelineConfiguration().getTestServiceStack()));

        parameters.add(newParameter(
                CLOUD_FORMATION_TEMPLATE_PARAMETER_PIPELINE_FINAL_SERVICE_STACK_NAME,
                pipelineStack.getPipelineConfiguration().getFinalServiceStack()));

        parameters.add(
                newParameter(CLOUD_FORMATION_TEMPLATE_PARAMETER_INIT_FUNCTION_NAME,
                        pipelineStack.getPipelineConfiguration().getInitLambdaFunctionName()));
        parameters.add(newParameter(CLOUD_FORMATION_TEMPLATE_PARAMETER_DESTROY_FUNCTION_NAME,
                pipelineStack.getPipelineConfiguration().getDestroyLambdaFunctionName()));

        parameters.add(newParameter(CLOUD_FORMATION_TEMPLATE_PARAMETER_TEST_PHASE_NAME,
                Stage.TEST.toString()));
        parameters.add(newParameter(CLOUD_FORMATION_TEMPLATE_PARAMETER_FINAL_PHASE_NAME,
                Stage.FINAL.toString()));

        createStackRequest.setParameters(parameters);
    }

    private void setPipelineStackTemplate(CreateStackRequest createStackRequest) throws IOException {
        String templateBody = IoUtils
                .resourceAsString(Paths.get(TEMPLATES_RESOURCE_DIRECTORY, PIPELINE_TEMPLATE));
        createStackRequest.setTemplateBody(templateBody);
    }

    private Parameter newParameter(String key, String value) {
        return new Parameter().withParameterKey(key).withParameterValue(value);
    }
}
