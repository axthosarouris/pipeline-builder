package no.bibsys.aws.lambda.deploy.handlers;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.junit.MatcherAssert.assertThat;

import java.io.IOException;
import no.bibsys.aws.cloudformation.Stage;
import no.bibsys.aws.git.github.BranchInfo;
import no.bibsys.aws.swaggerhub.SwaggerHubInfo;
import org.junit.Test;

public class SwaggerHubUpdaterTest {

    private static final String API_ID = "apiId";
    private static final String API_VERSION = "apiVersion";
    private static final String SWAGGER_ORG = "swaggerOrg";
    private static final String STACK_NAME = "stackName";


    @Test
    public void swaggerHubUpdater_notMasterBranch_restAPIWithStackName() throws IOException {
        SwaggerHubInfo swaggerHubInfo = new SwaggerHubInfo(API_ID, API_VERSION, SWAGGER_ORG);
        BranchInfo branchInfo = new BranchInfo(null, "notmaster");
        SwaggerHubUpdater swaggerHubUpdater = new SwaggerHubUpdater(
            null,
            null,
            swaggerHubInfo,
            Stage.FINAL,
            STACK_NAME,
            branchInfo);

        SwaggerHubInfo newInfo = swaggerHubUpdater.getSwaggerHubInfo();
        assertThat(newInfo.getApiId(), is(equalTo(STACK_NAME)));


    }


    @Test
    public void swaggerHubUpdater_masterBranch_restAPIWithStackName() throws IOException {
        SwaggerHubInfo swaggerHubInfo = new SwaggerHubInfo(API_ID, API_VERSION, SWAGGER_ORG);
        BranchInfo branchInfo = new BranchInfo(null, "master");
        SwaggerHubUpdater swaggerHubUpdater = new SwaggerHubUpdater(
            null,
            null,
            swaggerHubInfo,
            Stage.FINAL,
            STACK_NAME,
            branchInfo);

        SwaggerHubInfo newInfo = swaggerHubUpdater.getSwaggerHubInfo();
        assertThat(newInfo.getApiId(), is(equalTo(API_ID)));


    }

}
