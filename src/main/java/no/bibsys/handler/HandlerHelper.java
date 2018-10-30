package no.bibsys.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Optional;
import no.bibsys.handler.responses.GatewayResponse;
import no.bibsys.utils.ApiMessageParser;
import no.bibsys.utils.IoUtils;
import org.apache.http.HttpStatus;


public abstract class HandlerHelper<I, O> implements RequestStreamHandler {


    private final transient Class<I> iclass;
    private final transient ApiMessageParser<I> inputParser = new ApiMessageParser<>();
    private final transient ObjectMapper objectMapper = new ObjectMapper();
    protected transient LambdaLogger logger;
    private transient OutputStream outputStream;
    private transient Context context;

    public HandlerHelper(Class<I> iclass) {
        this.iclass = iclass;

    }


    private void init(OutputStream outputStream, Context context) {
        this.outputStream = outputStream;
        this.context = context;
        this.logger = context.getLogger();
    }

    public I parseInput(InputStream inputStream)
        throws IOException {
        String inputString = IoUtils.streamToString(inputStream);
        I input = inputParser.getBodyElementFromJson(inputString, iclass);
        return input;

    }

    protected abstract O processInput(I input, Context context) throws IOException;

    public void writeOutput(O output) throws IOException {
        String outputString = objectMapper.writeValueAsString(output);
        GatewayResponse gatewayResponse = new GatewayResponse(outputString);
        String responseJson = objectMapper.writeValueAsString(gatewayResponse);
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream));
        writer.write(responseJson);
        writer.close();

    }


    public void writerFailure(Throwable error) throws IOException {
        String outputString = Optional.ofNullable(error.getMessage())
            .orElse("Unknown error. Check stacktrace.");

        GatewayResponse gatewayResponse = new GatewayResponse(outputString,
            GatewayResponse.defaultHeaders(), HttpStatus.SC_INTERNAL_SERVER_ERROR);
        gatewayResponse.setBody(outputString);
        String gateWayResponseJson = objectMapper.writeValueAsString(gatewayResponse);

        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream));
        writer.write(gateWayResponseJson);
        writer.close();
    }


    protected Context getContext() {
        return this.context;
    }


    @Override
    public void handleRequest(InputStream input, OutputStream output, Context context)
        throws IOException {
        init(output, context);
        I inputObject = parseInput(input);
        O response = null;
        try {
            response = processInput(inputObject, context);
            writeOutput(response);
        } catch (Exception e) {
            logger.log(e.getMessage());
            writerFailure(e);
        }
    }


}
