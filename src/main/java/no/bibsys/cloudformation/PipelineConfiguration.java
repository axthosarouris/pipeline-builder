package no.bibsys.cloudformation;

public class PipelineConfiguration extends CloudFormationConfigurable {


    private final String sourceOutputArtifactName;
    private final String testServiceStack;
    private final String finalServiceStack;
    private final String pipelineName;


    public PipelineConfiguration(String repositoryName, String branchName) {
        super(repositoryName, branchName);

        this.sourceOutputArtifactName = initSourceOutputArtifactName();
        this.testServiceStack = initServiceStack(Stage.TEST);
        this.finalServiceStack = initServiceStack(Stage.PROD);
        this.pipelineName = initializePipelineName();


    }


    private String initializePipelineName() {
        return format(projectId, normalizedBranchName, "pipeline");
    }

    private String initServiceStack(String postifx) {
        return format(projectId, normalizedBranchName, "service-stack", postifx);
    }

    private String initSourceOutputArtifactName() {
        return format(projectId, normalizedBranchName, "sourceOutput");
    }


    public String getPipelineName() {
        return pipelineName;
    }


    public String getSourceOutputArtifactName() {
        return sourceOutputArtifactName;
    }


    public String getTestServiceStack() {
        return testServiceStack;
    }

    public String getFinalServiceStack() {
        return finalServiceStack;
    }




}
