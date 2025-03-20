package hu.akarnokd.minio;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.util.Map;

import io.minio.*;

// build.gradle
// dependencies {
//     implementation 'io.minio:minio:8.5.17'
// }


public class MinioUploader {

    public static void main(String[] args) throws Throwable {
        System.out.println("Building client");
        MinioClient mc = MinioClient.builder()
                .endpoint("http://localhost:9000")
                .credentials("tvOQtMStTt2fnXUSGNop", "N5ZdlScWHTQsGuX7D3Rd8xn0qizZyUIJyE8IYX7y")
                .build();

        System.out.println("Checking bucket uc1-in");
        var bucketExists = mc.bucketExists(BucketExistsArgs.builder().bucket("uc1-in").build());
        System.out.println("  " + bucketExists);

        System.out.println("Buckets:");
        for (var b : mc.listBuckets()) {
            System.out.println("  " + b.name());
        }
        
        var dt = ZonedDateTime.now(ZoneOffset.UTC);
        
        
        var ins = new ByteArrayInputStream("1,1\r\n2,2\r\n3,3\r\n4,4".getBytes(StandardCharsets.UTF_8));
        
        System.out.println("Uploading from stream");
        mc.putObject(PutObjectArgs.builder()
                .bucket("uc1-in")
                .object(dt.toString().replace(':', '-') + "/input_alt.csv")
                //.object("input_alt.csv")
                .userMetadata(Map.of("meta-attribute", "meta-value-2"))
                .stream(ins, ins.available(), -1)
                .build()
                );

        System.out.println("Uploading from file");
        mc.uploadObject(UploadObjectArgs.builder()
                .bucket("uc1-in")
                .object(dt.toString().replace(':', '-') + "/input.csv")
                .userMetadata(Map.of("meta-attribute", "meta-value"))
                .filename("c:/users/akarnokd/digrees_datalake/input.csv")
                .build()
                );
        
        System.out.println("Listing objects");
        for (var f : mc.listObjects(ListObjectsArgs.builder().bucket("uc1-in").recursive(true).build())) {
            System.out.println("  " + f.get().objectName() + " / " + f.get().size());

            var resp = mc.getObject(GetObjectArgs.builder()
                    .bucket("uc1-in")
                    .object(f.get().objectName())
                    .build()
            );
            
            System.out.println(new String(resp.readAllBytes(), StandardCharsets.UTF_8));
        }
        
        /*
        System.out.println("Listing objects 2");
        for (var f : mc.listObjects(ListObjectsArgs.builder().bucket("uc2-in").recursive(true).build())) {
            System.out.println("  " + f.get().objectName() + " / " + f.get().size());
            
        }
        */
    }
}
