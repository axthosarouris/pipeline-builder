package no.bibsys.swaggerhub;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.junit.MatcherAssert.assertThat;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpPost;
import org.junit.Test;

public class SwaggerDriverTest {


    private final transient String organization="unit";
    private final transient String apiId="api-id";
    private final transient String apiKey="ApIKeY";
    private final transient String apiVersion="2.1";
    private final transient SwaggerDriver driver=new SwaggerDriver(apiKey,organization,apiId);


    @Test
    public void UpdateRequestURLPathShouldIncludeOnlyOrganizationAndApiId()
        throws URISyntaxException, MalformedURLException {
        HttpPost post = postRequest();
        String path = post.getURI().toURL().getPath();
        assertThat(path,(containsString(organization)));
        assertThat(path,(containsString(apiId)));

    }

    @Test
    public void UpdateRequestURLPathShouldNotIncludeApiVersion()
        throws URISyntaxException, MalformedURLException {
        HttpPost post = postRequest();
        String path = post.getURI().toURL().getPath();
        assertThat(path,not(containsString(apiVersion)));
    }


    @Test
    public void UpdateRequestURLParametersShouldIncludeApiVersion()
        throws URISyntaxException {

        HttpPost post = postRequest();
        String parameters = post.getURI().getQuery();
        assertThat(parameters,containsString("version"));
        assertThat(parameters,containsString(apiVersion));
    }



    @Test
    public void UpdateRequestURLParametersShouldIncludeForceParamater()
        throws URISyntaxException {

        HttpPost post = postRequest();
        String parameters = post.getURI().getQuery();
        assertThat(parameters,containsString("force"));
    }


    @Test
    public void UpdateRequestURLParametersShouldIncludeIsPrivateParamater()
        throws URISyntaxException {

        HttpPost post = postRequest();
        String parameters = post.getURI().getQuery();
        assertThat(parameters,containsString("isPrivate"));
    }



    @Test
    public void DeleteVersionRequestURLPathShouldIncludeOrganizationApiIdAndVersion()
        throws URISyntaxException, MalformedURLException {
        HttpDelete delete= deleteVersionRequest();
        assertThat(delete.getURI().toURL().toString(),containsString(organization));
        assertThat(delete.getURI().toURL().toString(),containsString(apiId));
        assertThat(delete.getURI().toURL().toString(),containsString(apiVersion));
        assertThat(delete.getURI().toURL().getQuery(),is(equalTo(null)));
    }


    @Test
    public void DeleteRequestURLPathShouldIncludeOrganizationApiIdButNotVersion()
        throws URISyntaxException, MalformedURLException {
        HttpDelete delete= deleteApiRequest();
        assertThat(delete.getURI().toURL().toString(),containsString(organization));
        assertThat(delete.getURI().toURL().toString(),containsString(apiId));
        assertThat(delete.getURI().toURL().toString(),not(containsString(apiVersion)));
        assertThat(delete.getURI().toURL().getQuery(),is(equalTo(null)));
    }



    private HttpDelete deleteApiRequest() throws URISyntaxException {
        return driver.createDeleteApiRequest();
    }

    private HttpDelete deleteVersionRequest() throws URISyntaxException {
        return driver.createDeleteVersionRequest(apiVersion);
    }


    private HttpPost postRequest() throws URISyntaxException {
        return driver
            .createUpdateRequest("jsonString", apiVersion);
    }


}
