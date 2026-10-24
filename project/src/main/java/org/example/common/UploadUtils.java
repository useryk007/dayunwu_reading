package org.example.common;

import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.UUID;

public final class UploadUtils {

    public static final String UPLOAD_DIR = System.getProperty("user.dir") + File.separator + "uploads";
    public static final String URL_PREFIX = "/uploads/";

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            ".png", ".jpg", ".jpeg", ".gif", ".bmp", ".webp"
    );

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/png", "image/jpeg", "image/gif",
            "image/bmp", "image/webp", "image/pjpeg"
    );

    private UploadUtils() {}

    public static String save(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            return null;
        }

        String filename = file.getOriginalFilename();
        if (filename == null || filename.isBlank()) {
            throw new IOException("文件名无效");
        }

        String ext = extractExtension(filename);
        if (ext == null || !ALLOWED_EXTENSIONS.contains(ext.toLowerCase())) {
            throw new IOException("不支持的文件类型: " + ext);
        }

        String contentType = file.getContentType();
        if (contentType != null && !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new IOException("不支持的 Content-Type: " + contentType);
        }

        if (file.getSize() > 10 * 1024 * 1024) {
            throw new IOException("文件超出 10MB 限制");
        }

        File dir = new File(UPLOAD_DIR);
        if (!dir.exists() && !dir.mkdirs()) {
            throw new IOException("无法创建上传目录: " + UPLOAD_DIR);
        }

        String name = UUID.randomUUID().toString().replace("-", "") + ext;
        File dest = new File(dir, name);
        file.transferTo(dest);

        return URL_PREFIX + name;
    }

    public static boolean deleteByUrl(String url) {
        if (url == null || !url.startsWith(URL_PREFIX)) return false;
        String name = url.substring(URL_PREFIX.length());
        File file = new File(UPLOAD_DIR, name);
        return file.exists() && file.delete();
    }

    private static String extractExtension(String filename) {
        int idx = filename.lastIndexOf('.');
        if (idx == -1 || idx == filename.length() - 1) return null;
        return filename.substring(idx).toLowerCase();
    }
}