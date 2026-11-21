package com.proyecto.ecommerce.service;

import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UploadFileService {

  private final Path folder = Path.of("images").toAbsolutePath().normalize();

  public String saveImage(MultipartFile file) throws IOException {
    if (file.isEmpty()) return "default.jpg";
    if (file.getSize() > 5 * 1024 * 1024) throw new ResponseStatusException(
      HttpStatus.BAD_REQUEST,
      "La imagen supera 5 MB."
    );
    byte[] b = file.getBytes();
    String ext;
    if (
      b.length > 3 &&
      (b[0] & 255) == 255 &&
      (b[1] & 255) == 216 &&
      (b[2] & 255) == 255
    ) ext = "jpg";
    else if (
      b.length > 8 &&
      b[0] == (byte) 137 &&
      b[1] == 80 &&
      b[2] == 78 &&
      b[3] == 71
    ) ext = "png";
    else if (
      b.length > 12 &&
      new String(b, 0, 4, java.nio.charset.StandardCharsets.US_ASCII).equals(
        "RIFF"
      ) &&
      new String(b, 8, 4, java.nio.charset.StandardCharsets.US_ASCII).equals(
        "WEBP"
      )
    ) ext = "webp";
    else throw new ResponseStatusException(
      HttpStatus.BAD_REQUEST,
      "Selecciona una imagen JPG, PNG o WebP."
    );
    String name = "upload-" + UUID.randomUUID() + "." + ext;
    Files.createDirectories(folder);
    Files.write(folder.resolve(name), b);
    return name;
  }

  public void deleteImage(String name) {
    // Keep the repository's source photographs available for the demo and documentation.
    if (
      name == null || !name.matches("upload-[a-f0-9-]+[.](jpg|png|webp)")
    ) return;
    try {
      Files.deleteIfExists(folder.resolve(name));
    } catch (IOException e) {
      throw new java.io.UncheckedIOException(e);
    }
  }
}
