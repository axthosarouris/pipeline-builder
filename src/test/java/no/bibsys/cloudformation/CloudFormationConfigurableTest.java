package no.bibsys.cloudformation;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.Arrays;
import java.util.regex.Matcher;
import org.hamcrest.MatcherAssert;
import org.junit.Test;

public class CloudFormationConfigurableTest extends ConfigurationTests {


    public CloudFormationConfigurableTest() throws IOException {
        super();
    }


    @Test
    public void format_strings_stringWithDashes() {
        String output = conf.format("hello", "worl_d");
        assertThat(output, is(equalTo("hello-worl_d")));
    }


    @Test
    public void initNormalizedBranchName_branchName_normalizedBranchNameUnderSpecifiedLength() {
        assertThat(conf.getNormalizedBranchName().length(),
            is(not(greaterThan(CloudFormationConfigurable.NORMALIZED_BRANCH_MAX_LENGTH))));
    }


    @Test
    public void initProjectId_repositoryName_projectIdWithoutUnderscores() {
        assertThat(repoName, containsString("_"));
        assertThat(repoName, is(equalTo("REPOSITORY_NAME_ENV_VAR")));

        assertThat(conf.getProjectId(), not(containsString("_")));

    }


    @Test
    public void initProjectId_repositoryName_projectIdWithTokensNotLongerThanSpecified() {

        String[] tokens = projectId.split("-");
        Arrays.stream(tokens).forEach(token -> {
            assertThat(token.length(),
                is(not(greaterThan(CloudFormationConfigurable.MAX_PROJECT_WORD_LENGTH))));
        });

    }


    @Test
    public void initNormalizedBranch_branchName_compliesToAmazonRestrctricions() {
        Matcher matcher = amazonPattern.matcher(conf.getNormalizedBranchName());
        MatcherAssert.assertThat(matcher.matches(), is(equalTo(true)));

    }
}
