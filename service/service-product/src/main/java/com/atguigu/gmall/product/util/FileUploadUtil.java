package com.atguigu.gmall.product.util;

import com.atguigu.gmall.common.util.Result;
import lombok.SneakyThrows;
import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.StorageClient;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerServer;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RequestMapping("admin/product")
@RestController
@CrossOrigin
public class FileUploadUtil {

    @PostMapping("/fileUpload")
    @SneakyThrows
    public Result fileUpload(@RequestParam("file") MultipartFile multipartFile){
        if (multipartFile!=null){
            String fileUrl = "http://192.168.200.128:8080";
            String path = this.getClass().getClassLoader().getResource("tracker.conf").getPath();
            System.out.println(path);
            ClientGlobal.init(path);
            TrackerServer connection = new TrackerClient().getConnection();
            StorageClient storageClient = new StorageClient(connection,null);
            String[] urls = storageClient.upload_file(multipartFile.getBytes(),
                    StringUtils.getFilenameExtension(multipartFile.getOriginalFilename()),
                    null);
            for (String url : urls) {
                fileUrl = fileUrl + "/" + url;
            }
            System.out.println(fileUrl);
            return Result.ok(fileUrl);
        }else {
            return Result.fail();
        }
    }
}
