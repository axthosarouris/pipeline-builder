package no.bibsys.handler.requests;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;

import java.io.IOException;
import java.nio.file.Paths;
import no.bibsys.utils.IoUtils;
import org.junit.Test;

public class PullRequestTests {


    private String pullRequestString;
    private PullRequest pullRequest;

    public PullRequestTests() throws IOException {
        pullRequestString = IoUtils.resourceAsString(Paths.get("github", "pullrequest.json"));
        pullRequest =  (PullRequest) PullRequest.create(pullRequestString).get();
    }

    //Assert that PullRequest ...

    @Test
    public void readsAction() {
        assertThat(pullRequest.getAction(), is(equalTo(PullRequest.ACTION_CLOSE)));
    }

    @Test
    public void readsBranch() {
        assertThat(pullRequest.getBranch(), is(equalTo("changes")));
    }

    @Test
    public void readsRepositoryName() {
        assertThat(pullRequest.getRepository(), is(equalTo("Hello-World")));
    }


}
