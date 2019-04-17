package com.hl.house.dao.service;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.io.Files;

@Service
public class FileService {

    private static final Logger logger = LoggerFactory.getLogger(FileService.class);

    @Value("${file.path:}")
    private String filePath;


    public List<String> getImgPaths(List<MultipartFile> files) {
        if (Strings.isNullOrEmpty(filePath)) {
            filePath = getResourcePath();
        }
        List<String> paths = Lists.newArrayList();
        files.forEach(file -> {
            File localFile = null;
            if (!file.isEmpty()) {
                try {
                    localFile = saveToLocal(file, filePath);
                    logger.info("localFile={}", localFile);

                    String path = StringUtils.substringAfterLast(localFile.getAbsolutePath(), filePath);
                    logger.info("path={}", path);
                    paths.add(path);
                } catch (IOException e) {
                    throw new IllegalArgumentException(e);
                }
            }
        });
        return paths;
    }

    public static String getResourcePath() {
        File file = new File(".\\house-web\\src\\main\\resources\\static\\imgs");
        String absolutePath = file.getAbsolutePath();

        logger.info("absolutePath={}", absolutePath);
        return absolutePath;
    }

    private File saveToLocal(MultipartFile file, String filePath2) throws IOException {
        File newFile = new File(filePath + "/" + Instant.now().getEpochSecond() + "/" + file.getOriginalFilename());

        logger.info("newFile.getAbsolutePath={}", newFile.getAbsolutePath());
        if (!newFile.exists()) {
            newFile.getParentFile().mkdirs();
            newFile.createNewFile();
        }
        Files.write(file.getBytes(), newFile);
        return newFile;
    }

}
