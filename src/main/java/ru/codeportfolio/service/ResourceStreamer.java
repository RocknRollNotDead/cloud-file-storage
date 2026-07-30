package ru.codeportfolio.service;

import io.minio.Result;
import io.minio.messages.Item;
import org.springframework.stereotype.Component;
import ru.codeportfolio.dao.minio.FileRepository;
import ru.codeportfolio.dao.minio.FolderRepository;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Component
public class ResourceStreamer {
    private final FileRepository fileRepository;
    private final FolderRepository folderRepository;

    public ResourceStreamer(FileRepository fileRepository, FolderRepository folderRepository) {
        this.fileRepository = fileRepository;
        this.folderRepository = folderRepository;
    }

    /*package private*/ void streamFile(String path, OutputStream outputStream) {
        try (InputStream fileStream = fileRepository.getStream(path)) {

            fileStream.transferTo(outputStream);

        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("File output error! ", e);
        }
    }


    /*package private*/ void streamFolderAsZipFile(String path, OutputStream outputStream) {
        try (ZipOutputStream zipOut = new ZipOutputStream(outputStream)) {

            Iterable<Result<Item>> objects = folderRepository.getItemsFiles(path);

            for (Result<Item> res : objects) {
                Item item = res.get();

                if (item.isDir()) {
                    continue;
                }

                String entryName = item.objectName().substring(path.length());

                try (InputStream fileStream = fileRepository.getStream(item.objectName())) {

                    zipOut.putNextEntry(new ZipEntry(entryName));
                    fileStream.transferTo(zipOut);
                    zipOut.closeEntry();
                }
            }

        } catch (Exception e) {
            throw new RuntimeException("Archive folder error!", e);
        }
    }


}
