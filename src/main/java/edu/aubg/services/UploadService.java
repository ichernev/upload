package edu.aubg.services;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * To test with curl:
 * 
 * <pre>
 * curl --header "Content-Type:application/octet-stream" --data-binary @test.txt http://localhost:9093/services/upload
 * </pre>
 */
@Path("/services")
public class UploadService {

	private static final java.nio.file.Path inDir = Paths.get("/srv/fixed/in");
	private static final java.nio.file.Path tmpDir = Paths.get("/srv/fixed/tmp");

	private static final Logger logger = LoggerFactory.getLogger(UploadService.class);

	@POST
	@Path("upload")
	@Consumes(MediaType.APPLICATION_OCTET_STREAM)
	@Produces(MediaType.APPLICATION_JSON)
	public Response uploadFile(@Context HttpHeaders headers, InputStream in) {

		File tempFile = tmpDir.resolve(UUID.randomUUID().toString()).toFile();

		try (OutputStream out = new FileOutputStream(tempFile)) {

			FileUtils.copyInputStreamToFile(in, tempFile);

			UploadServiceResponse resp = new UploadServiceResponse();
			resp.setDocId(UUID.randomUUID().toString());
			resp.setFileSize(tempFile.length());

			Files.move(tempFile.toPath(), inDir.resolve(resp.getDocId()), StandardCopyOption.ATOMIC_MOVE);

			return Response.ok(resp).build();

		} catch (Exception ex) {

			logger.error("Upload failed", ex);

			return Response.status(Status.INTERNAL_SERVER_ERROR).build();

		} finally {

			FileUtils.deleteQuietly(tempFile);
		}
	}
}